package uk.me.eastmans.security;

import jakarta.persistence.*;
import jakarta.validation.ValidationException;
import org.jspecify.annotations.Nullable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    public static final int USERNAME_MAX_LENGTH = 50;
    public static final int PASSWORD_MAX_LENGTH = 100;

    private static final String encoding = "{bcrypt}";
    private static final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @Column(name = "username", unique = true, nullable = false, length = USERNAME_MAX_LENGTH)
    private String username = "";

    @Column(name = "password", nullable = false, length = PASSWORD_MAX_LENGTH)
    private String password = "";

    @ManyToOne(fetch = FetchType.EAGER)
    private Persona defaultPersona = null;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = Boolean.FALSE;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_persona",
            joinColumns = @JoinColumn(name = "username"),
            inverseJoinColumns = @JoinColumn(name = "persona_id"))
    private Set<Persona> personas;

    protected User() { // To keep Hibernate happy
    }

    public User(String username) {
        this.username = username;
        this.enabled = true;
        this.personas = new HashSet<>();
    }

    public User(String username, String password, Set<Persona> personas) {
        this(username);
        this.password = password;
        this.defaultPersona = personas.iterator().next();
        this.personas.addAll(personas);
    }

    public Long getId() { return id;}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {this.username = username;}

    public String getPassword() {
        // We need to strip off the {bcrypt} prefix
        return password.startsWith(encoding) ?
                password.substring(encoding.length()) : password;
    }

    public void setPassword(String password) {
        // We need to encode the password using bcrypt
        this.password = encoding + encoder.encode(password);
    }

    public @Nullable Persona getDefaultPersona() {
        return defaultPersona;
    }

    public void setDefaultPersona(Persona defaultPersona) {
        // Must be in the set of personas
        if (personas.contains(defaultPersona))
            this.defaultPersona = defaultPersona;
        else
            throw new ValidationException("The persona must be in the list of personas");
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Set<Persona> getPersonas() {
        return personas;
    }

    public void setPersonas(Set<Persona> personas) {this.personas = personas;}

    public void removePersona(Persona persona) {
        this.personas.remove(persona);
    }

    public Persona getPersonaWithName(String name) {
        Persona foundPersona = null;
        for (Persona persona : personas) {
            if (persona.getName().equals(name)) {
                foundPersona = persona;
                break;
            }
        }
        return foundPersona;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        User other = (User) obj;
        return getUsername() != null && getUsername().equals(other.getUsername());
    }

    @Override
    public int hashCode() {
        // Hashcode should never change during the lifetime of an object. Because of
        // this we can't use getId() to calculate the hashcode. Unless you have sets
        // with lots of entities in them, returning the same hashcode should not be a
        // problem.
        return username == null ? getClass().hashCode() : username.hashCode();
    }

}