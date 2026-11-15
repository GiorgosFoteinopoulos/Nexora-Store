package com.practice.nexora.tx;


import com.practice.nexora.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import({TestcontainersConfiguration.class, TransactionTrapsDemo.class})
class TransactionTrapsTest {

    @Autowired
    private TransactionTrapsDemo demo;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void createLabTable() {
        jdbc.execute("create table if not exists tx_demo (note text not null)");
    }

    @AfterEach
    void dropLabTable() {
        jdbc.execute("drop table if exists tx_demo");
    }

    private List<String> savedNotes() {
        return jdbc.queryForList("select note from tx_demo", String.class);
    }

    @Test
    void callingThroughSpringStartsATransaction() {
        assertThat(demo.isInTransaction()).isTrue();
    }

    @Test
    void selfCallSkipsTheTransaction() {
        assertThat(demo.isInTransactionViaThis()).isFalse();
    }

    @Test
    void throughSpring_aFailureRollsBackTheWholeOrder() {
        assertThatThrownBy(() -> demo.saveOrderThenFail()).isInstanceOf(IllegalStateException.class);

        assertThat(savedNotes()).isEmpty();
    }

    @Test
    void viaThis_aFailureLeavesHalfAnOrderInTheDatabase() {
        assertThatThrownBy(() -> demo.saveOrderThenFailViaThis()).isInstanceOf(IllegalStateException.class);
        assertThat(savedNotes()).containsExactly("line 1");
    }

    @Test
    void checkedException_commitsTheWorkDoneBeforeIt() {
        assertThatThrownBy(() -> demo.saveThenFailChecked()).isInstanceOf(IOException.class);
        assertThat(savedNotes()).containsExactly("line 1");
    }

    @Test
    void rollbackForException_makesCheckedExceptionRollback() {
        assertThatThrownBy(() -> demo.saveThenFailCheckedWithRollbackFor()).isInstanceOf(IOException.class);
        assertThat(savedNotes()).isEmpty();
    }
}
