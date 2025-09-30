package uk.me.eastmans.expensesmanagement;

import jakarta.persistence.*;
import uk.me.eastmans.security.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "expenseHeader")
public class ExpenseHeader {

    public static final int NAME_MAX_LENGTH = 50;
    public static final int DESCRIPTION_MAX_LENGTH = 250;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = NAME_MAX_LENGTH)
    private String name = "";

    @Column(name = "description", nullable = false, length = DESCRIPTION_MAX_LENGTH)
    private String description = "";

    @OneToMany(mappedBy = "header", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<ExpenseLine> expenseLines;

    @Column(name="currencyValue", nullable=false, columnDefinition="Decimal(10,2) default '0.00'")
    private BigDecimal totalAmount;

    @ManyToOne
    @JoinColumn(name="owner", nullable=false)
    private User owner;

    protected ExpenseHeader() { // To keep Hibernate happy
    }

    public ExpenseHeader(User owner, String name, String description) {
        this.owner = owner;
        this.name = name;
        this.description = description;
        this.expenseLines = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public User getUser() { return owner;}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {this.description = description;}

    public void setExpenseLines(List<ExpenseLine> lines) {
        this.expenseLines = lines;
    }

    public List<ExpenseLine> getExpenseLines() {
        return expenseLines;
    }

    public void addExpenseLine(ExpenseLine line ) {
        expenseLines.add(line);
        line.setHeader(this);
    }

    public BigDecimal getTotalAmount() { return totalAmount; }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        ExpenseHeader other = (ExpenseHeader) obj;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        // Hashcode should never change during the lifetime of an object. Because of
        // this we can't use getId() to calculate the hashcode. Unless you have sets
        // with lots of entities in them, returning the same hashcode should not be a
        // problem.
        return id == null ? getClass().hashCode() : id.hashCode();
    }
}