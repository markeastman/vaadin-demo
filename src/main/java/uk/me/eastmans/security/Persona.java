package uk.me.eastmans.security;

import jakarta.persistence.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "personas")
public class Persona implements Comparable<Persona> {

    public static final int NAME_MAX_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", unique = true, nullable = false, length = NAME_MAX_LENGTH)
    private String name = "";

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "persona_authority",
            joinColumns = @JoinColumn(name = "persona_id"),
            inverseJoinColumns = @JoinColumn(name = "authority_id"))
    private Set<Authority> authorities;

    protected Persona() { // To keep Hibernate happy
    }

    public Persona(String name, Set<Authority> authorities) {
        this.name = name;
        this.authorities = new HashSet<>();
        this.authorities.addAll(authorities);
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

    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
    }

    public Set<Authority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        Persona other = (Persona) obj;
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

    @Override
    public int compareTo(@NotNull Persona persona) {
        return name.compareToIgnoreCase(persona.getName());
    }

    @Override
    public String toString() {
        return name;
    }
}
