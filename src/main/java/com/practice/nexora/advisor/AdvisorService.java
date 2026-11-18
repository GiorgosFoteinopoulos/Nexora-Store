package com.practice.nexora.advisor;


import com.practice.nexora.catalog.ProductResponse;
import com.practice.nexora.catalog.ProductService;
import com.practice.nexora.common.NexoraProperties;
import com.practice.nexora.common.NotFoundException;
import com.practice.nexora.common.ServiceUnavailableException;
import com.practice.nexora.user.UserAccount;
import com.practice.nexora.user.UserAccountRepository;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.*;

@Service
public class AdvisorService {

    private static final Logger log = LoggerFactory.getLogger(AdvisorService.class);

    static final int MAX_RECOMMENDATIONS = 3;
    static final int HISTORY_WINDOW = 10;

    static final String TOO_SLOW = "Nexa is taking too long to answer. Please try again in a moment.";
    static final String FAILED = "Nexa could not answer right now. Please try again in a moment.";


    private final AdvisorClient advisor;
    private final ProductService products;
    private final AdvisorMessageRepository messages;
    private final UserAccountRepository users;
    private final Duration timeout;

    private final ExecutorService modelCalls = Executors.newVirtualThreadPerTaskExecutor();

    public AdvisorService(AdvisorClient advisor, ProductService products, AdvisorMessageRepository messages, UserAccountRepository users, NexoraProperties properties) {
        this.advisor = advisor;
        this.products = products;
        this.messages = messages;
        this.users = users;
        this.timeout = properties.advisor().timeout();
    }

    public ChatResponse chat(String email, String message) {
        UserAccount user = findUser(email);
        List<ChatTurn> history = messages.findByUserOrderByIdDesc(user, PageRequest.of(0, HISTORY_WINDOW))
                .reversed()
                .stream()
                .map(ChatTurn::from)
                .toList();

        NexaAnswer answer = askWithDeadline(history, message);

        messages.saveAll(List.of(
                new AdvisorMessage(user, ChatRole.USER, message),
                new AdvisorMessage(user, ChatRole.ASSISTANT, answer.reply())
        ));

        return new ChatResponse(answer.reply(), verifiedCards(answer));
    }

    public List<ChatTurnResponse> conversationOf(String email) {
        return messages.findByUserOrderByIdAsc(findUser(email)).stream()
                .map(ChatTurnResponse::from)
                .toList();
    }

    @Transactional
    public void forget(String email) {
        messages.deleteAllOf(findUser(email));
    }

    private NexaAnswer askWithDeadline(List<ChatTurn> history, String message) {
        CompletableFuture<NexaAnswer> call =
                CompletableFuture.supplyAsync(() -> advisor.reply(history, message), modelCalls);
        try {
            return call.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException tooSlow) {
            log.warn("Nexa did not answer within {}", timeout);
            throw new ServiceUnavailableException(TOO_SLOW);
        } catch (ExecutionException failed) {
            log.error("Nexa call failed", failed.getCause());
            throw new ServiceUnavailableException(FAILED);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException(FAILED);
        }
    }

    @PreDestroy
    void stopModelCalls() {
        modelCalls.close();
    }

    private List<ProductResponse> verifiedCards(NexaAnswer answer) {
        return answer.recommendedSlugs().stream()
                .map(slug -> slug == null ? "" : slug.strip().toLowerCase(Locale.ROOT))
                .filter(slug -> !slug.isEmpty())
                .distinct()
                .map(this::findRealProduct)
                .flatMap(Optional::stream)
                .limit(MAX_RECOMMENDATIONS)
                .toList();
    }


    private Optional<ProductResponse> findRealProduct(String slug) {
        try {
            return Optional.of(products.getProduct(slug));
        } catch (NotFoundException invented) {
            log.warn("Nexa recommended a product that does not exist: '{}' (dropped)", slug);
            return Optional.empty();
        }
    }

    private UserAccount findUser(String email) {
        return users.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Account not found"));
    }
}
