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

    // Флаг, указывающий, что освещение «принудительно переопределено» (авария/выход и пр.)
    private boolean emergencyOverridden;

    public LightingService() {
        // Инициализация значений по умолчанию
        userSettings.setPower(true);
        userSettings.setColor("#FFFFFF");  // Лучше хранить в HEX
        userSettings.setBrightness(100);

        // Изначально активные настройки совпадают с пользовательскими
        currentSettings.setPower(userSettings.getPower());
        currentSettings.setColor(userSettings.getColor());
        currentSettings.setBrightness(userSettings.getBrightness());

        this.emergencyOverridden = false;
    }

    /**
     * Обновление настроек, установленных пользователем (ручной режим).
     * Если система сейчас не в «аварийном режиме» — обновляем сразу же.
     */
    public void updateUserSettings(LightingControlDto newSettings) {
        logger.info("Обновление пользовательских настроек освещения: {}", newSettings);

        // Сохраняем пользовательские настройки
        userSettings.setPower(newSettings.getPower());
        userSettings.setColor(newSettings.getColor());
        userSettings.setBrightness(newSettings.getBrightness());

        // Если не аварийный режим — применяем немедленно
        if (!emergencyOverridden) {
            currentSettings.setPower(newSettings.getPower());
            currentSettings.setColor(newSettings.getColor());
            currentSettings.setBrightness(newSettings.getBrightness());
        }
        // Если аварийный режим включён — применяем новое значение только после restoreUserSettings()
    }

    /**
     * Принудительное переопределение настроек (например, при аварии).
     */
    public void overrideLighting(String color, Integer brightness) {
        logger.info("Переопределение настроек освещения на аварийные: цвет={}, яркость={}", color, brightness);

        // Включаем флаг «аварийное переопределение»
        emergencyOverridden = true;

        // Мгновенно меняем текущие настройки
        if (color != null) {
            currentSettings.setColor(color);
        }
        if (brightness != null) {
            currentSettings.setBrightness(brightness);
        }
        currentSettings.setPower(true); // обычно при аварии обязательно включаем свет
    }

    /**
     * Восстановление пользовательских настроек после сигнала опасности.
     */
    public void restoreUserSettings() {
        logger.info("Восстановление пользовательских настроек освещения: {}", userSettings);
        emergencyOverridden = false;
        currentSettings.setPower(userSettings.getPower());
        currentSettings.setColor(userSettings.getColor());
        currentSettings.setBrightness(userSettings.getBrightness());
    }

    public LightingControlDto getCurrentSettings() {
        return currentSettings;
    }

    public boolean isEmergencyOverridden() {
        return emergencyOverridden;
    }
}