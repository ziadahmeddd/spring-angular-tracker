package com.example.tracker.dto;

import com.example.tracker.model.Category;
import com.example.tracker.model.Expense;
import java.math.BigDecimal;
import java.time.Instant;

public record ExpenseResponse(
    Long id,
    String title,
    BigDecimal amount,
    Category category,
    Instant createdAt
) {
    // Factory method to map Entity -> DTO cleanly
    public static ExpenseResponse fromEntity(Expense expense) {
        return new ExpenseResponse(
            expense.getId(),
            expense.getTitle(),
            expense.getAmount(),
            expense.getCategory(),
            expense.getCreatedAt()
        );
    }
}