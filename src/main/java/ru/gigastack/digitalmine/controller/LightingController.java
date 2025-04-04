package ru.gigastack.digitalmine.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.gigastack.digitalmine.dto.LightingControlDto;
import ru.gigastack.digitalmine.service.LightingService;
import ru.gigastack.digitalmine.service.MqttClientService;

@RestController
@RequestMapping("/api/lighting")
@CrossOrigin(origins = "*")
public class LightingController {

    private static final Logger logger = LoggerFactory.getLogger(LightingController.class);
    private final LightingService lightingService;
    private final MqttClientService mqttClientService;

    public LightingController(LightingService lightingService, MqttClientService mqttClientService) {
        this.lightingService = lightingService;
        this.mqttClientService = mqttClientService;
    }

    @PostMapping("/web")
    public ResponseEntity<String> controlLightingWeb(@RequestBody LightingControlDto controlDto) {
        logger.info("Получена команда управления освещением через веб-интерфейс: {}", controlDto);
        lightingService.updateUserSettings(controlDto);
        // Отправка команды на MQTT брокер
        mqttClientService.publish("lighting/control", controlDto.toString());
        return ResponseEntity.ok("Команда управления освещением обработана");
    }

    @PostMapping("/restore")
    public ResponseEntity<String> restoreLightingSettings() {
        lightingService.restoreUserSettings();
        mqttClientService.publish("lighting/control", "restore");
        logger.info("Пользовательские настройки освещения восстановлены");
        return ResponseEntity.ok("Пользовательские настройки освещения восстановлены");
    }

    @PostMapping("/port")
    public ResponseEntity<String> controlLightingPort(@RequestBody LightingControlDto controlDto) {
        logger.info("Получена команда управления освещением через монитор порта: {}", controlDto);
        lightingService.updateUserSettings(controlDto);
        mqttClientService.publish("lighting/control", controlDto.toString());
        return ResponseEntity.ok("Команда управления освещением через монитор порта обработана");
    }

    @GetMapping("/status")
    public ResponseEntity<LightingControlDto> getLightingStatus() {
        return ResponseEntity.ok(lightingService.getCurrentSettings());
    }
}