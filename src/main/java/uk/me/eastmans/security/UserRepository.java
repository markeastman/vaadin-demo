package uk.me.eastmans.security;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

    User findByUsername(String username);
    List<User> findByDefaultPersona(Persona persona);
    List<User> findByPersonasContaining(Persona persona);
    Slice<User> findAllBy(Pageable pageable);
}
