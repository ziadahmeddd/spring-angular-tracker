package com.example.tracker.repository;

import com.example.tracker.model.Category;
import com.example.tracker.model.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // Every lookup is scoped to the owner. Without this an authenticated user
    // could read or mutate another user's rows by guessing an id.
    Page<Expense> findByOwnerEmail(String email, Pageable pageable);

    Page<Expense> findByOwnerEmailAndCategory(String email, Category category, Pageable pageable);

    Optional<Expense> findByIdAndOwnerEmail(Long id, String email);
}
