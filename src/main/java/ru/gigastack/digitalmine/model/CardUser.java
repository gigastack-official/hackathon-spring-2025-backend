package ru.gigastack.digitalmine.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "card_users")
public class CardUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Уникальный ID карты, который будет совпадать с rfidDto.tagId
    @Column(name = "card_id", unique = true, nullable = false)
    private String cardId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    // Роль, уровень доступа и т.п.
    @Column(name = "role")
    private String role;
}