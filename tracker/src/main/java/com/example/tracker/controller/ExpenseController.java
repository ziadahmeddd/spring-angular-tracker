package com.example.tracker.controller;

import com.example.tracker.dto.CreateExpenseRequest;
import com.example.tracker.dto.ExpenseResponse;
import com.example.tracker.dto.PagedResponse;
import com.example.tracker.dto.UpdateExpenseRequest;
import com.example.tracker.model.Category;
import com.example.tracker.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(
            @Valid @RequestBody CreateExpenseRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        ExpenseResponse response = expenseService.createExpense(request, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ExpenseResponse>> getAllExpenses(
            @RequestParam(required = false) Category category,
            @PageableDefault(page = 0, size = 10)
            @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @AuthenticationPrincipal UserDetails principal) {
        PagedResponse<ExpenseResponse> expenses =
                expenseService.getAllExpenses(category, pageable, principal.getUsername());
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        ExpenseResponse expense = expenseService.getExpenseById(id, principal.getUsername());
        return ResponseEntity.ok(expense);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody UpdateExpenseRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        ExpenseResponse updated = expenseService.updateExpense(id, request, principal.getUsername());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        expenseService.deleteExpense(id, principal.getUsername());
        return ResponseEntity.noContent().build();
    }
}
