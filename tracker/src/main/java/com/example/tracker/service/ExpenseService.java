package com.example.tracker.service;

import com.example.tracker.dto.CreateExpenseRequest;
import com.example.tracker.dto.ExpenseResponse;
import com.example.tracker.dto.PagedResponse;
import com.example.tracker.dto.UpdateExpenseRequest;
import com.example.tracker.exception.ResourceNotFoundException;
import com.example.tracker.model.Category;
import com.example.tracker.model.Expense;
import com.example.tracker.model.User;
import com.example.tracker.repository.ExpenseRepository;
import com.example.tracker.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public ExpenseService(ExpenseRepository expenseRepository, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ExpenseResponse createExpense(CreateExpenseRequest request, String ownerEmail) {
        User owner = requireUser(ownerEmail);

        Expense expense = new Expense(
            request.title(),
            request.amount(),
            request.category(),
            owner
        );

        Expense savedExpense = expenseRepository.save(expense);
        return ExpenseResponse.fromEntity(savedExpense);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ExpenseResponse> getAllExpenses(Category category, Pageable pageable, String ownerEmail) {
        Page<Expense> expensePage;

        if (category != null) {
            expensePage = expenseRepository.findByOwnerEmailAndCategory(ownerEmail, category, pageable);
        } else {
            expensePage = expenseRepository.findByOwnerEmail(ownerEmail, pageable);
        }

        Page<ExpenseResponse> responsePage = expensePage.map(ExpenseResponse::fromEntity);

        return PagedResponse.fromPage(responsePage);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long id, String ownerEmail) {
        return ExpenseResponse.fromEntity(requireOwnedExpense(id, ownerEmail));
    }

    @Transactional
    public ExpenseResponse updateExpense(Long id, UpdateExpenseRequest request, String ownerEmail) {
        Expense expense = requireOwnedExpense(id, ownerEmail);

        expense.setTitle(request.title());
        expense.setAmount(request.amount());
        expense.setCategory(request.category());

        return ExpenseResponse.fromEntity(expense);
    }

    @Transactional
    public void deleteExpense(Long id, String ownerEmail) {
        Expense expense = requireOwnedExpense(id, ownerEmail);
        expenseRepository.delete(expense);
    }

    /**
     * Returns 404 rather than 403 when the row exists but belongs to someone
     * else, so the response cannot be used to probe for other users' ids.
     */
    private Expense requireOwnedExpense(Long id, String ownerEmail) {
        return expenseRepository.findByIdAndOwnerEmail(id, ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Expense with id " + id + " not found"));
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No user for email " + email));
    }
}
