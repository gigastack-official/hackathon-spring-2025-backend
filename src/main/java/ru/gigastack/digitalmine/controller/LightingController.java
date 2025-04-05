package ru.gigastack.digitalmine.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private ObjectMapper objectMapper;

    public LightingController(LightingService lightingService, MqttClientService mqttClientService) {
        this.lightingService = lightingService;
        this.mqttClientService = mqttClientService;
    }

    @PostMapping("/web")
    public ResponseEntity<String> controlLightingWeb(@RequestBody LightingControlDto controlDto) throws JsonProcessingException {
        logger.info("Получена команда управления освещением через веб-интерфейс: {}", controlDto);
        lightingService.updateUserSettings(controlDto);
        // Отправка команды на MQTT брокер
        String jsonPayload = objectMapper.writeValueAsString(controlDto);
        mqttClientService.publish("lighting/control", jsonPayload);
        return ResponseEntity.ok("Команда управления освещением обработана");
    }

    @PostMapping("/restore")
    public ResponseEntity<String> restoreLightingSettings() {
        lightingService.restoreUserSettings();
        mqttClientService.publish("lighting/control", "{\"power\":true,\"color\":\"#000000\",\"brightness\":50}");
        logger.info("Пользовательские настройки освещения восстановлены");
        return ResponseEntity.ok("Пользовательские настройки освещения восстановлены");
    }


    @GetMapping("/status")
    public ResponseEntity<LightingControlDto> getLightingStatus() {
        return ResponseEntity.ok(lightingService.getCurrentSettings());
    }
}