package uk.me.eastmans.expensesmanagement;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

@Entity
@Table(name = "expense_line")
public class ExpenseLine {

    public static final int DESCRIPTION_MAX_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @Column(name = "description", nullable = false, length = DESCRIPTION_MAX_LENGTH)
    private String description = "";

    @ManyToOne
    @JoinColumn(name="category", nullable=false)
    private ExpenseCategory category;

    @ManyToOne
    @JoinColumn(name="header", nullable=false)
    private ExpenseHeader header;

    @Temporal(TemporalType.DATE)
    private LocalDate expenseDate;

    @Column(name="currencyValue", nullable=false, columnDefinition="Decimal(10,2) default '0.00'")
    private BigDecimal currencyAmount;

    @Column(name = "currencyCode", nullable = false, length = 3)
    private String currencyCode;

    @Column(name="baseValue", nullable=false, columnDefinition="Decimal(10,2) default '0.00'")
    private BigDecimal baseAmount;

    protected ExpenseLine() { // To keep Hibernate happy
    }

    public ExpenseLine(String description) { this(null,description); }

    public ExpenseLine(ExpenseCategory category, String description) {
        this.category = category;
        this.description = description;
        this.currencyAmount = BigDecimal.ZERO;
        this.currencyCode = "";
        this.baseAmount = BigDecimal.ZERO;
    }

    public ExpenseHeader getHeader() {return header;}

    public void setHeader(ExpenseHeader header) {this.header = header;}

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public void setCategory(ExpenseCategory category) {
        this.category = category;
    }


    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
    }

    public BigDecimal getCurrencyAmount() {
        return currencyAmount;
    }

    public void setCurrencyAmount(BigDecimal currencyAmount) {
        this.currencyAmount = currencyAmount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Currency getCurrency() {
        if (currencyCode == null || currencyCode.isEmpty())
            return null;
        return Currency.getInstance(currencyCode);
    }

    public void setCurrency(Currency currency) {
        this.currencyCode = currency.getCurrencyCode();
    }

    public LocalDate getExpenseDate() {return expenseDate;}

    public void setExpenseDate(LocalDate when) {this.expenseDate = when;}

    public void setDescription(String description) {this.description = description;}

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        ExpenseLine other = (ExpenseLine) obj;
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