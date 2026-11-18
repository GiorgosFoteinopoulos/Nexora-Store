package com.practice.nexora.search;

import com.practice.nexora.TestcontainersConfiguration;
import com.practice.nexora.catalog.ProductTextChanged;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

import static org.mockito.Mockito.after;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ReembedAfterProductChangeTest {

    @Autowired
    private ApplicationEventPublisher events;

    @Autowired
    private TransactionTemplate transaction;

    @MockitoBean
    private ProductEmbeddingService embeddings;

    @Test
    void aCommittedChangeTriggersAReembedding() {
        transaction.executeWithoutResult(status -> events.publishEvent(new ProductTextChanged(1L)));

        verify(embeddings, timeout(2_000)).refresh();
    }

    @Test
    void aRolledBackChangeTriggersNothing() {
        transaction.executeWithoutResult(status -> {
            events.publishEvent(new ProductTextChanged(1L));
            status.setRollbackOnly();
        });

        verify(embeddings, after(1_000).never()).refresh();
    }

    @Test
    void anAnnouncementOutsideATransactionIsIgnored() {
        events.publishEvent(new ProductTextChanged(1L)); // no transaction at all

        verify(embeddings, after(1_000).never()).refresh();
    }
}