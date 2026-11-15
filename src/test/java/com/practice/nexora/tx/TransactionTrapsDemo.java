package com.practice.nexora.tx;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;

public class TransactionTrapsDemo {

    private static final String INSERT = "insert into tx_demo (note) values (?)";

    private final JdbcTemplate jdbc;

    public TransactionTrapsDemo(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public boolean isInTransaction() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }

    public boolean isInTransactionViaThis() {
        return this.isInTransaction();
    }

    @Transactional
    public void saveOrderThenFail() {
        jdbc.update(INSERT, "line 1");
        throw new IllegalStateException("payment failed before line 2");
    }

    public void saveOrderThenFailViaThis() {
        this.saveOrderThenFail();
    }

    @Transactional
    public void saveThenFailChecked() throws IOException {
        jdbc.update(INSERT, "line 1");
        throw new IOException("receipt printer offline");
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveThenFailCheckedWithRollbackFor() throws IOException {
        jdbc.update(INSERT, "line 1");
        throw new IOException("receipt printer offline");
    }
}
