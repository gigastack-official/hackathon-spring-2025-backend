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

    private final CardUserService cardUserService;

    @Autowired
    public CardUserController(CardUserService cardUserService) {
        this.cardUserService = cardUserService;
    }

    @GetMapping
    public List<CardUser> getAllCardUsers() {
        return cardUserService.findAll();
    }

    @PostMapping
    public CardUser createCardUser(@RequestBody CardUser cardUser) {
        return cardUserService.createCardUser(
                cardUser.getCardId(),
                cardUser.getFullName(),
                cardUser.getRole()
        );
    }
}