package uk.me.eastmans.expensesmanagement;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import uk.me.eastmans.security.User;

import java.util.List;

@Service
@PreAuthorize("isAuthenticated()")
public class ExpenseService {

    private final ExpenseHeaderRepository expenseHeaderRepository;

    ExpenseService(ExpenseHeaderRepository expenseHeaderRepository) {
        this.expenseHeaderRepository = expenseHeaderRepository;
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
        expenseHeaderRepository.saveAndFlush(expense);
    }
}