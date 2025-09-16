package uk.me.eastmans.personamanagement;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.me.eastmans.security.Persona;
import uk.me.eastmans.security.User;
import uk.me.eastmans.security.UserRepository;

import java.util.List;

@Service
public class PersonaService {

    private final PersonaRepository personaRepository;
    private final UserRepository userRepository;

    PersonaService(PersonaRepository personaRepository,
                   UserRepository userRepository) {
        this.personaRepository = personaRepository;
        this.userRepository = userRepository;
    }

    /*
    @Transactional
    public void createPersona(String name, Set<Authority> authorities) {
        var persona = new Persona(name, authorities);
        personaRepository.saveAndFlush(persona);
    }
     */

    @Transactional
    public void deletePersona(Persona persona) {
        // We need to check all users that have this persona as a default persona
        // as the menu and login will no longer work. For all these users we need to
        // delete the persona from the default persona of the user
        List<User> usersWithDefaultPersona = userRepository.findByDefaultPersona(persona);
        // For each user we need to remove the persona from their list and
        // then replace the default Persona with another or null if none available
        for (User u : usersWithDefaultPersona) {
            u.setDefaultPersona(null);
        }
        List<User> usersWithPersona = userRepository.findByPersonasContaining(persona);
        for (User u : usersWithPersona) {
            u.removePersona(persona);
        }
        personaRepository.delete(persona);
        personaRepository.flush();
    }

    @Transactional(readOnly = true)
    public List<Persona> list(Pageable pageable) {
        return personaRepository.findAllBy(pageable).toList();
    }

    @Transactional(readOnly = true)
    public List<Persona> listAll() {
        return personaRepository.findAll();
    }

}
