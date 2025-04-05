package ru.gigastack.digitalmine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.gigastack.digitalmine.model.CardUser;
import ru.gigastack.digitalmine.service.CardUserService;

import java.util.List;

@RestController
@RequestMapping("/api/card-users")
@CrossOrigin(origins = "*")
public class CardUserController {

    @Autowired
    private CardUserService cardUserService;

    /**
     * Получение всех владельцев карт (для отображения на фронте).
     */
    @GetMapping
    public List<CardUser> getAllCardUsers() {
        return cardUserService.findAll();
    }

    /**
     * Создание нового владельца карты.
     * Пример: POST /api/card-users
     * {
     *   "cardId": "ABC12345",
     *   "fullName": "Ivan Petrov",
     *   "role": "GUEST"
     * }
     */
    @PostMapping
    public CardUser createCardUser(@RequestBody CardUser cardUser) {
        return cardUserService.createCardUser(
                cardUser.getCardId(),
                cardUser.getFullName(),
                cardUser.getRole()
        );
    }
}