package com.spring.ai.springai.dto;

public record Joke(
        String text,
        String category,
        Double laughScore,
        Boolean isNSFW
) {
}
