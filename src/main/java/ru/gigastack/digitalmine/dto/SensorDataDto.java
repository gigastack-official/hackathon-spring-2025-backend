package ru.gigastack.digitalmine.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SensorDataDto {

    @NotNull(message = "Уровень газа не может быть null")
    private Double gasLevel;

    @NotNull(message = "Температура не может быть null")
    private Double temperature;

    @NotNull(message = "Влажность не может быть null")
    private Double humidity;

    // При необходимости можно добавить дополнительные поля, например, время измерения, ID датчика и т.д.
}