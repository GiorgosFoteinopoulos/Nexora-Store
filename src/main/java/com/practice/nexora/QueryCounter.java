package com.practice.nexora;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

public final class QueryCounter {

    private final Statistics statistics;

    public QueryCounter(EntityManagerFactory entityManagerFactory)  {
        this.statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        this.statistics.setStatisticsEnabled(true);
    }

    public void reset() {
        statistics.clear();
    }

    public long statements() {
        return statistics.getPrepareStatementCount();
    }

    public long entityFetches() {
        return statistics.getEntityFetchCount();
    }

    public long collectionFetches() {
        return statistics.getCollectionFetchCount();
    }
}
