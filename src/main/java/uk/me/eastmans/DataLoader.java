package uk.me.eastmans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import uk.me.eastmans.expensesmanagement.ExpenseCategory;
import uk.me.eastmans.expensesmanagement.ExpenseHeader;
import uk.me.eastmans.expensesmanagement.ExpenseLine;
import uk.me.eastmans.expensesmanagement.ExpenseService;
import uk.me.eastmans.personamanagement.PersonaRepository;
import uk.me.eastmans.security.*;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;

@Component
public class DataLoader implements ApplicationRunner {

    final ExpenseService expenseService;
    final AuthorityRepository authorityRepository;
    final PersonaRepository personaRepository;
    final UserRepository userRepository;

    @Autowired
    public DataLoader(ExpenseService expenseService,
                      AuthorityRepository authorityRepository,
                      PersonaRepository personaRepository,
                      UserRepository userRepository) {
        this.expenseService = expenseService;
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
        Authority expenses = new Authority("ROLE_EXPENSES", "Manage expenses");
        authorityRepository.save(expenses);

        // Create the personas
        HashSet<Authority> adminAuthorities = new HashSet<>();
        adminAuthorities.add(processes);
        adminAuthorities.add(users);
        adminAuthorities.add(personas);
        adminAuthorities.add(expenses);
        Persona adminPersona = new Persona("Administrator", adminAuthorities);
        personaRepository.save(adminPersona);
        HashSet<Authority> ccAuthorities = new HashSet<>();
        ccAuthorities.add(suppliers);
        ccAuthorities.add(tasks);
        ccAuthorities.add(expenses);
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

        // Add some expense categories
        ExpenseCategory flights = new ExpenseCategory("flights");
        ExpenseCategory hotels = new ExpenseCategory("hotels");
        expenseService.saveOrCreate(flights);
        expenseService.saveOrCreate(hotels);

        // Add some expenses
        ExpenseHeader expense = new ExpenseHeader(adminUser, "Greece trip", "Devoxx conference in Greece");
        ExpenseLine line1 = new ExpenseLine(expense, hotels, "Hotel in New York");
        line1.setCurrencyAmount( new BigDecimal("230.00" ));
        line1.setCurrencyCode("EUR");
        line1.setBaseAmount( new BigDecimal("250.00" ));
        line1.setExpenseDate(new GregorianCalendar(2025, Calendar.FEBRUARY, 11).getTime());
        ExpenseLine line2 = new ExpenseLine(expense, flights,"Flight to new york");
        line2.setExpenseDate(new GregorianCalendar(2025, Calendar.FEBRUARY, 11).getTime());
        line2.setCurrencyAmount( new BigDecimal("1230.00" ));
        line2.setCurrencyCode("EUR");
        line2.setBaseAmount( new BigDecimal("1280.00" ));
        expenseService.saveOrCreate(expense);
    }
}