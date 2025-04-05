package ru.gigastack.digitalmine.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LightingControlDto {
    @NotNull(message = "Состояние включения не может быть null")
    private Boolean power;

    // Цвет в формате HEX. Пример: #FFFFFF, #ff0000
    @NotNull(message = "Цвет не может быть null")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Цвет должен быть в формате #RRGGBB (HEX)")
    private String color;

    @NotNull(message = "Яркость не может быть null")
    private Integer brightness;
}