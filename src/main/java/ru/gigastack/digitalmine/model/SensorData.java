package ru.gigastack.digitalmine.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sensor_data")
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Уровень загазованности (например, в ppm)
    @Column(name = "gas_level", nullable = false)
    private Double gasLevel;

    // Температура (например, в градусах Цельсия)
    @Column(name = "temperature", nullable = false)
    private Double temperature;

    // Влажность (например, в процентах)
    @Column(name = "humidity", nullable = false)
    private Double humidity;

    // Время измерения данных
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
}