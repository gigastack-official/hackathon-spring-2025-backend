package ru.gigastack.digitalmine.dto;

import lombok.Data;

@Data
public class CameraControlDto {
    // Угол поворота камеры в градусах
    private Integer rotationAngle;
    // Флаг: включить/выключить подсветку
    private Boolean lightOn;
    // Интенсивность подсветки (например, от 0 до 100)
    private Integer lightIntensity;
}