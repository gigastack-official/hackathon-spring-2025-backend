package ru.gigastack.digitalmine.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.gigastack.digitalmine.dto.LightingControlDto;

@Service
public class LightingService {

    private static final Logger logger = LoggerFactory.getLogger(LightingService.class);

    // Текущие настройки, установленные пользователем
    private LightingControlDto userSettings = new LightingControlDto();
    // Текущие активные настройки (могут быть изменены системой для сигнализации)
    private LightingControlDto currentSettings = new LightingControlDto();

    public LightingService() {
        // Инициализация значений по умолчанию
        userSettings.setPower(true);
        userSettings.setColor("white");
        userSettings.setBrightness(100);

        // Изначально активные настройки совпадают с пользовательскими
        currentSettings.setPower(userSettings.getPower());
        currentSettings.setColor(userSettings.getColor());
        currentSettings.setBrightness(userSettings.getBrightness());
    }

    // Обновление настроек, установленных пользователем
    public void updateUserSettings(LightingControlDto newSettings) {
        logger.info("Обновление пользовательских настроек освещения: {}", newSettings);
        userSettings.setPower(newSettings.getPower());
        userSettings.setColor(newSettings.getColor());
        userSettings.setBrightness(newSettings.getBrightness());

        // Применяем изменения немедленно, если нет сигнализации
        currentSettings.setPower(newSettings.getPower());
        currentSettings.setColor(newSettings.getColor());
        currentSettings.setBrightness(newSettings.getBrightness());

        // Здесь можно добавить код для отправки команд на аппаратное обеспечение
    }

    // Автоматическое переопределение настроек при сигнализации
    public void overrideLighting(String color, Integer brightness) {
        logger.info("Переопределение настроек освещения на аварийные: цвет={}, яркость={}", color, brightness);
        currentSettings.setColor(color);
        if (brightness != null) {
            currentSettings.setBrightness(brightness);
        }
        // Здесь можно отправить команду для изменения освещения в реальном времени
    }

    // Восстановление пользовательских настроек после сигнализации
    public void restoreUserSettings() {
        logger.info("Восстановление пользовательских настроек освещения: {}", userSettings);
        currentSettings.setPower(userSettings.getPower());
        currentSettings.setColor(userSettings.getColor());
        currentSettings.setBrightness(userSettings.getBrightness());
        // Отправить команду для восстановления освещения
    }

    // Геттер для текущих настроек
    public LightingControlDto getCurrentSettings() {
        return currentSettings;
    }
}