package com.practice.nexora.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateDescriptionRequest(@NotBlank @Size(max = 2000) String description) {
}