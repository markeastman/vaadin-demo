package uk.me.eastmans.security;

import jakarta.persistence.*;

@Entity
@Table(name = "authorities")
public class Authority {

    public static final int NAME_MAX_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", unique = true, nullable = false, length = NAME_MAX_LENGTH)
    private String name = "";

    protected Authority() { // To keep Hibernate happy
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        Authority other = (Authority) obj;
        return getName() != null && getName().equals(other.getName());
    }

    @Override
    public int hashCode() {
        // Hashcode should never change during the lifetime of an object. Because of
        // this we can't use getId() to calculate the hashcode. Unless you have sets
        // with lots of entities in them, returning the same hashcode should not be a
        // problem.
        return name == null ? getClass().hashCode() : name.hashCode();
    }

}
