package ru.gigastack.digitalmine.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rfid_log")
public class RfidLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag_id", nullable = false)
    private String tagId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "status", nullable = false)
    private String status;  // "allowed" / "denied" / etc.

    // Новое поле: ФИО/Имя владельца карты. Может быть null, если "denied"
    @Column(name = "full_name")
    private String fullName;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
}