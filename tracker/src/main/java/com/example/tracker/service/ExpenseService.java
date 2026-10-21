package com.example.tracker.service;

import com.example.tracker.dto.CreateExpenseRequest;
import com.example.tracker.dto.ExpenseResponse;
import com.example.tracker.dto.PagedResponse;
import com.example.tracker.dto.UpdateExpenseRequest;
import com.example.tracker.exception.ResourceNotFoundException;
import com.example.tracker.model.Category;
import com.example.tracker.model.Expense;
import com.example.tracker.repository.ExpenseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public ExpenseResponse createExpense(CreateExpenseRequest request) {
        Expense expense = new Expense(
            request.title(),
            request.amount(),
            request.category()
        );

        Expense savedExpense = expenseRepository.save(expense);
        return ExpenseResponse.fromEntity(savedExpense);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ExpenseResponse> getAllExpenses(Category category, Pageable pageable) {
        Page<Expense> expensePage;

        if (category != null) {
            expensePage = expenseRepository.findByCategory(category, pageable);
        } else {
            expensePage = expenseRepository.findAll(pageable);
        }

        Page<ExpenseResponse> responsePage = expensePage.map(ExpenseResponse::fromEntity);

        return PagedResponse.fromPage(responsePage);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense with id " + id + " not found"));
        return ExpenseResponse.fromEntity(expense);
    }

    @Transactional
    public ExpenseResponse updateExpense(Long id, UpdateExpenseRequest request) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense with id " + id + " not found"));

        expense.setTitle(request.title());
        expense.setAmount(request.amount());
        expense.setCategory(request.category());

        return ExpenseResponse.fromEntity(expense);
    }

    @Transactional
    public void deleteExpense(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Expense with id " + id + " not found");
        }
        expenseRepository.deleteById(id);
    }
}