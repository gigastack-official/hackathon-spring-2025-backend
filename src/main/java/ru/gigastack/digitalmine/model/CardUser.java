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

    @Column(name = "card_id", unique = true, nullable = false)
    private String cardId; // та самая RFID-метка

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "role")
    private String role;
}