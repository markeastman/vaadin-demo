package uk.me.eastmans.personamanagement;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.me.eastmans.security.Authority;
import uk.me.eastmans.security.Persona;

import java.util.List;
import java.util.Set;

@Service
public class PersonaService {

    private final PersonaRepository personaRepository;

    PersonaService(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

    @Transactional
    public void createPersona(String name, Set<Authority> authorities) {
        var persona = new Persona(name, authorities);
        personaRepository.saveAndFlush(persona);
    }

    @Transactional(readOnly = true)
    public List<Persona> list(Pageable pageable) {
        return personaRepository.findAllBy(pageable).toList();
    }

}
