package uk.me.eastmans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import uk.me.eastmans.personamanagement.PersonaRepository;
import uk.me.eastmans.security.*;

import java.util.HashSet;

@Component
public class DataLoader implements ApplicationRunner {

    final AuthorityRepository authorityRepository;
    final PersonaRepository personaRepository;
    final UserRepository userRepository;

    @Autowired
    public DataLoader(AuthorityRepository authorityRepository,
                      PersonaRepository personaRepository,
                      UserRepository userRepository) {
        this.authorityRepository = authorityRepository;
        this.personaRepository = personaRepository;
        this.userRepository = userRepository;
    }

    /*
     * We load data via Java rather than data.sql as I have found with h2 and hibernate the sequence is not initialised properly
     */
    public void run(ApplicationArguments args) {

        // Create all the authorities for role based access
        Authority processes = new Authority("ROLE_PROCESSES", "Manage the background processes");
        authorityRepository.save(processes);
        Authority suppliers = new Authority("ROLE_SUPPLIERS", "Manage suppliers");
        authorityRepository.save(suppliers);
        Authority tasks = new Authority("ROLE_TASKS", "Manage tasks");
        authorityRepository.save(tasks);
        Authority users = new Authority("ROLE_USERS", "Manage users");
        authorityRepository.save(users);
        Authority personas = new Authority("ROLE_PERSONAS", "Manage personas");
        authorityRepository.save(personas);

        // Create the personas
        HashSet<Authority> adminAuthorities = new HashSet<>();
        adminAuthorities.add(processes);
        adminAuthorities.add(users);
        adminAuthorities.add(personas);
        Persona adminPersona = new Persona("Administrator", adminAuthorities);
        personaRepository.save(adminPersona);
        HashSet<Authority> ccAuthorities = new HashSet<>();
        ccAuthorities.add(suppliers);
        ccAuthorities.add(tasks);
        Persona ccPersona = new Persona("Credit Controller", ccAuthorities);
        personaRepository.save(ccPersona);
        HashSet<Authority> smAuthorities = new HashSet<>();
        smAuthorities.add(suppliers);
        Persona smPersona = new Persona("Supplier Management", smAuthorities);
        personaRepository.save(smPersona);

        // Create the users
        HashSet<Persona> adminPersonas = new HashSet<>();
        adminPersonas.add(adminPersona);
        User adminUser = new User("admin", "{noop}admin", adminPersonas);
        userRepository.save(adminUser);
        HashSet<Persona> ccPersonas = new HashSet<>();
        ccPersonas.add(ccPersona);
        ccPersonas.add(smPersona);
        User ccUser = new User("cc", "{noop}cc", ccPersonas);
        userRepository.save(ccUser);
        User disabledUser = new User("disabled", "{noop}disabled", ccPersonas);
        disabledUser.setEnabled(false);
        userRepository.save(disabledUser);
        for (int i = 0; i < 100; i++) {
            User u = new User("u" + i, "{bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG", ccPersonas);
            userRepository.save(u);
        }
    }
}