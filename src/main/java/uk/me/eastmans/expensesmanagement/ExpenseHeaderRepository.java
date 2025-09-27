package uk.me.eastmans.expensesmanagement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uk.me.eastmans.security.User;

import java.util.List;

public interface ExpenseHeaderRepository extends JpaRepository<ExpenseHeader, Long>, JpaSpecificationExecutor<ExpenseHeader> {

    // If you don't need a total row count, Slice is better than Page as it only performs a select query.
    // Page performs both a select and a count query.
    List<ExpenseHeader> findAllByOwner(User user);
}