package uk.me.eastmans.expensesmanagement;

import jakarta.persistence.*;

import java.util.Date;

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
    @JoinColumn(name="header", nullable=false)
    private ExpenseHeader header;

    @Temporal(TemporalType.DATE)
    private Date expenseDate;

    protected ExpenseLine() { // To keep Hibernate happy
    }

    public ExpenseLine(ExpenseHeader header, String description) {
        this.header = header;
        header.addExpenseLine(this);
        this.description = description;
    }

    public ExpenseHeader getHeader() {return header;}

    public void setHeader(ExpenseHeader header) {this.header = header;}

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Date getExpenseDate() {return expenseDate;}

    public void setExpenseDate(Date when) {this.expenseDate = when;}

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