package com.practice.nexora.catalog;


import com.practice.nexora.TestcontainersConfiguration;
import com.practice.nexora.common.ConflictException;
import com.practice.nexora.common.Money;
import org.flywaydb.core.internal.jdbc.Results;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class ConcurrentVariantUpdateTest {

    private static final String SKU = "NX-PH15-128-BLK";

    @Autowired
    private AdminCatalogService service;

    @Autowired
    private ProductRepository products;

    @Autowired
    private BrandRepository brands;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private PriceChangeRepository priceChanges;

    @BeforeEach
    void saveAVariantWithStock10() {
        Brand nexora = brands.save(new Brand("Nexora"));
        Phone phone = new Phone("Nexora Phone 15", nexora, "A phone.", 6.1, 4500, true);
        phone.addVariant(new ProductVariant(new Sku(SKU), "Black", 128, Money.euros("799"), 10));
        products.save(phone);
    }

    @AfterEach
    void cleanUp() {
        products.deleteAll();
        brands.deleteAll();
    }

    @RepeatedTest(5)
    void twoAdminsSavingAtOnceExactlyOneWins() throws Exception {
        Results results = runAtTheSameTime(
                asAdmin(() -> service.updateVariant(SKU, new UpdateVariantRequest(null, 7, 0L))),
                asAdmin(() -> service.updateVariant(SKU, new UpdateVariantRequest(null, 15, 0L))));

        assertThat(results.successes()).isEqualTo(1);
        assertThat(results.failures()).singleElement()

                .isInstanceOfAny(ConflictException.class, OptimisticLockingFailureException.class);
        assertThat(currentVariant().getVersion()).isEqualTo(1);
    }

    @RepeatedTest(3)
    void onlyTheWinnersPriceChangeIsInTheHistory() throws Exception {
        Results results = runAtTheSameTime(
                asAdmin(() -> service.updateVariant(SKU, new UpdateVariantRequest(new BigDecimal("849"), null, 0L))),
                asAdmin(() -> service.updateVariant(SKU, new UpdateVariantRequest(new BigDecimal("899"), null, 0L)))
        );

        assertThat(results.successes()).isEqualTo(1);
        assertThat(priceChanges.countByVariant_Sku(new Sku(SKU))).isEqualTo(1);
    }

    @Test
    void hibernatesVersionCheckStopsTheLostUpdate() throws Exception {
        CyclicBarrier bothHaveRead = new CyclicBarrier(2);
        TransactionTemplate inATransaction = new TransactionTemplate(transactionManager);

        Results results = runAtTheSameTime(
                () -> inATransaction.execute(status -> {
                    ProductVariant variant = products.findVariantBySku(new Sku(SKU)).orElseThrow();
                    waitFor(bothHaveRead);
                    variant.removeFromStock(3);
                    return null;
                }),
                () -> inATransaction.execute(status -> {
                    ProductVariant variant = products.findVariantBySku(new Sku(SKU)).orElseThrow();
                    waitFor(bothHaveRead);
                    variant.restock(5);
                    return null;
                }));

        assertThat(results.successes()).isEqualTo(1);
        assertThat(results.failures()).singleElement().isInstanceOf(OptimisticLockingFailureException.class);

        ProductVariant afterwards = currentVariant();
        assertThat(afterwards.getVersion()).isEqualTo(1);
        assertThat(afterwards.getStock()).isIn(7, 15);
    }

    private ProductVariant currentVariant() {
        return products.findVariantBySku(new Sku(SKU)).orElseThrow();
    }

    private record Results(int successes, List<Throwable> failures) {

    }

    private static Results runAtTheSameTime(Callable<?>... tasks) throws InterruptedException {
        CountDownLatch startingGun = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        try (ExecutorService workers = Executors.newFixedThreadPool(tasks.length)) {
            for (Callable<?> task : tasks) {
                futures.add(workers.submit(() -> {
                    startingGun.await();
                    return task.call();
                }));
            }
            startingGun.countDown();


            int successes = 0;
            List<Throwable> failures = new ArrayList<>();
            for (Future<?> future : futures) {
                try {
                    future.get(30, TimeUnit.SECONDS);
                    successes++;
                } catch (ExecutionException failed) {
                    failures.add(failed.getCause());
                } catch (TimeoutException stuck) {
                    throw new AssertionError("A thread did not finish in time", stuck);
                }
            }
            return new Results(successes, failures);
        }
    }


    private static <T> Callable<T> asAdmin(Callable<T> task) {
        return () -> {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                    "admin@nexora.local", null, AuthorityUtils.createAuthorityList("ROLE_ADMIN")));
            SecurityContextHolder.setContext(context);
            try {
                return task.call();
            } finally {
                SecurityContextHolder.clearContext();
            }
        };
    }

    private static void waitFor(CyclicBarrier barrier) {
        try {
            barrier.await(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("The other thread never arrived", e);
        }
    }
}