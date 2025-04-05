package ru.gigastack.digitalmine.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gigastack.digitalmine.model.CardUser;
import ru.gigastack.digitalmine.repository.CardUserRepository;

import java.util.List;
import java.util.Optional;

/**
 * Пример сервиса для управления владельцами карт (CardUser).
 * Тут вы можете хранить любую бизнес-логику, связанную с ними.
 */
@Service
public class CardUserService {

    private final CardUserRepository cardUserRepository;

    @Autowired
    public CardUserService(CardUserRepository cardUserRepository) {
        this.cardUserRepository = cardUserRepository;
    }

    /**
     * Проверяем, зарегистрирована ли карта в БД.
     */
    public boolean cardExists(String cardId) {
        return cardUserRepository.findByCardId(cardId).isPresent();
    }

    /**
     * Возвращаем CardUser, если есть.
     */
    public Optional<CardUser> findByCardId(String cardId) {
        return cardUserRepository.findByCardId(cardId);
    }

    /**
     * Создание (регистрация) нового владельца RFID-карты.
     */
    public CardUser createCardUser(String cardId, String fullName, String role) {
        // Проверка, нет ли уже такой карты
        if (cardExists(cardId)) {
            throw new RuntimeException("Карта с таким ID уже зарегистрирована");
        }

        CardUser user = new CardUser();
        user.setCardId(cardId);
        user.setFullName(fullName);
        user.setRole(role);

        return cardUserRepository.save(user);
    }

    /**
     * Просто получить список всех владельцев карт
     */
    public List<CardUser> findAll() {
        return cardUserRepository.findAll();
    }

    /**
     * Найти по id из БД (не путать с cardId)
     */
    public Optional<CardUser> findById(Long id) {
        return cardUserRepository.findById(id);
    }
}