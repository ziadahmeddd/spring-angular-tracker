package com.example.tracker.dto;

import com.example.tracker.model.Category;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateExpenseRequest(
    @NotBlank(message = "Title cannot be blank")
    @Size(min = 2, max = 120, message = "Title must be between 2 and 120 characters")
    String title,

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    @Digits(integer = 8, fraction = 2, message = "Amount format must match standard currency (up to 8 integer digits, 2 decimals)")
    BigDecimal amount,

    @NotNull(message = "Category is required")
    Category category
) {}