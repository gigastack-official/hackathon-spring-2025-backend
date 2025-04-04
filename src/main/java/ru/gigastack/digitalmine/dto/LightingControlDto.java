package ru.gigastack.digitalmine.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LightingControlDto {
    @NotNull(message = "Состояние включения не может быть null")
    private Boolean power;

    // Цвет в формате строки (например, "white", "yellow", "red")
    @NotNull(message = "Цвет не может быть null")
    private String color;

    @NotNull(message = "Яркость не может быть null")
    private Integer brightness;
}