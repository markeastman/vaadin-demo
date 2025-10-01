package uk.me.eastmans.expensesmanagement;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import uk.me.eastmans.security.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@PreAuthorize("isAuthenticated()")
public class ExpenseService {

    private final ExpenseHeaderRepository expenseHeaderRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;

    ExpenseService(ExpenseCategoryRepository expenseCategoryRepository,
            ExpenseHeaderRepository expenseHeaderRepository) {
        this.expenseCategoryRepository = expenseCategoryRepository;
        this.expenseHeaderRepository = expenseHeaderRepository;
    }

    @Transactional
    public Optional<ExpenseHeader> getExpense(long id) {
        return expenseHeaderRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<ExpenseHeader> listAll(User user) {
        return expenseHeaderRepository.findAllByOwner(user);
    }

    @Transactional
    public void deleteExpense(ExpenseHeader expenseHeader) {
        expenseHeaderRepository.delete(expenseHeader);
        expenseHeaderRepository.flush();
    }

    @Transactional
    public void saveOrCreate(ExpenseHeader expense) {
        // We need to calculate the total expense amount from the lines
        BigDecimal total = BigDecimal.ZERO;
        for (ExpenseLine line : expense.getExpenseLines()) {
            total = total.add(line.getBaseAmount());
        }
        expense.setTotalAmount(total);
        expenseHeaderRepository.saveAndFlush(expense);
    }

    @Transactional
    public void saveOrCreate(ExpenseCategory expenseCategory) {
        // We need to calculate the total expense amount from the lines
        expenseCategoryRepository.saveAndFlush(expenseCategory);
    }
}