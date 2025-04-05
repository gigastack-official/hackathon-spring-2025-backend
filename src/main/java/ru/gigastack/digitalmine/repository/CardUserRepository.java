package ru.gigastack.digitalmine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.gigastack.digitalmine.model.CardUser;

import java.util.Optional;

public interface CardUserRepository extends JpaRepository<CardUser, Long> {

    Optional<CardUser> findByCardId(String cardId);

}