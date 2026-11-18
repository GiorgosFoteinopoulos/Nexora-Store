package com.practice.nexora.search;

import java.util.List;


public record GoldenSet(List<Question> questions) {
    
    public record Question(String question, List<String> expected) {
    }
}