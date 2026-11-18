package com.practice.nexora.search;

import java.util.List;


public record EvaluationReport(
        String model,
        int questions,
        double hitAt1,
        double hitAt3,
        double meanReciprocalRank,
        List<QuestionResult> results) {

    public record QuestionResult(String question, List<String> expected, List<String> found, int rank) {
    }
}