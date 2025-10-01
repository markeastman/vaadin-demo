package uk.me.eastmans.expensesmanagement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long>, JpaSpecificationExecutor<ExpenseCategory> {

 }