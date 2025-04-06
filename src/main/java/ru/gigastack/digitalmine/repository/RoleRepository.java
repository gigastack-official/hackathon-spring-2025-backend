package ru.gigastack.digitalmine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.gigastack.digitalmine.model.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}