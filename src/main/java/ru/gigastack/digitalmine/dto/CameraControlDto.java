package ru.gigastack.digitalmine.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CameraControlDto {

    @NotNull(message = "Угол поворота не может быть null")
    private Integer rotationAngle;

    @NotNull(message = "Флаг подсветки не может быть null")
    private Boolean lightOn;

    @NotNull(message = "Интенсивность подсветки не может быть null")
    private Integer lightIntensity;
}