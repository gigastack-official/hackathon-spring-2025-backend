package ru.gigastack.digitalmine.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.gigastack.digitalmine.dto.LightingControlDto;
import ru.gigastack.digitalmine.service.LightingService;

@RestController
@RequestMapping("/api/lighting")
@CrossOrigin(origins = "*")
public class LightingController {

    private static final Logger logger = LoggerFactory.getLogger(LightingController.class);
    private final LightingService lightingService;

    public LightingController(LightingService lightingService) {
        this.lightingService = lightingService;
    }

    // Ручное управление через веб-интерфейс
    @PostMapping("/web")
    public ResponseEntity<String> controlLightingWeb(@RequestBody LightingControlDto controlDto) {
        logger.info("Получена команда управления освещением через веб-интерфейс: {}", controlDto);
        lightingService.updateUserSettings(controlDto);
        return ResponseEntity.ok("Команда управления освещением обработана");
    }

    // Восстановление пользовательских настроек после сигнализации
    @PostMapping("/restore")
    public ResponseEntity<String> restoreLightingSettings() {
        lightingService.restoreUserSettings();
        logger.info("Пользовательские настройки освещения восстановлены");
        return ResponseEntity.ok("Пользовательские настройки освещения восстановлены");
    }

    // Управление через монитор порта (симуляция)
    @PostMapping("/port")
    public ResponseEntity<String> controlLightingPort(@RequestBody LightingControlDto controlDto) {
        logger.info("Получена команда управления освещением через монитор порта: {}", controlDto);
        lightingService.updateUserSettings(controlDto);
        return ResponseEntity.ok("Команда управления освещением через монитор порта обработана");
    }

    // Новый GET‑эндпоинт для получения текущих настроек освещения (используется страницей "lighting")
    @GetMapping("/status")
    public ResponseEntity<LightingControlDto> getLightingStatus() {
        return ResponseEntity.ok(lightingService.getCurrentSettings());
    }
}