package ru.gigastack.digitalmine.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gigastack.digitalmine.model.CardUser;
import ru.gigastack.digitalmine.repository.CardUserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CardUserService {

    private final CardUserRepository cardUserRepository;

    @Autowired
    public CardUserService(CardUserRepository cardUserRepository) {
        this.cardUserRepository = cardUserRepository;
    }

    /**
     * Проверяем, существует ли пользователь с данной RFID-картой
     */
    public boolean cardExists(String cardId) {
        return cardUserRepository.findByCardId(cardId).isPresent();
    }

    /**
     * Создаём / регистрируем нового владельца карты
     */
    public CardUser createCardUser(String cardId, String fullName, String role) {
        // Можно проверить, нет ли уже
        Optional<CardUser> existing = cardUserRepository.findByCardId(cardId);
        if (existing.isPresent()) {
            throw new RuntimeException("Карта с таким ID уже зарегистрирована!");
        }
        CardUser user = new CardUser();
        user.setCardId(cardId);
        user.setFullName(fullName);
        user.setRole(role);
        return cardUserRepository.save(user);
    }

    public List<CardUser> findAll() {
        return cardUserRepository.findAll();
    }

    public Optional<CardUser> findById(Long id) {
        return cardUserRepository.findById(id);
    }
}