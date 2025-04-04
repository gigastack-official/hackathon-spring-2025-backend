package ru.gigastack.digitalmine.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.gigastack.digitalmine.dto.CameraControlDto;
import ru.gigastack.digitalmine.service.MqttClientService;

@RestController
@RequestMapping("/api/camera")
public class CameraController {

    private static final Logger logger = LoggerFactory.getLogger(CameraController.class);
    private final MqttClientService mqttClientService;

    public CameraController(MqttClientService mqttClientService) {
        this.mqttClientService = mqttClientService;
    }

    @PostMapping("/control")
    public ResponseEntity<String> controlCamera(@RequestBody CameraControlDto command) {
        logger.info("Получена команда управления камерой: {}", command);

        // Пример формирования MQTT-сообщения (формат можно изменить по необходимости)
        String mqttPayload = String.format("rotate:%d,lightOn:%b,intensity:%d",
                command.getRotationAngle(),
                command.getLightOn(),
                command.getLightIntensity());
        mqttClientService.publish("camera/control", mqttPayload);

        return ResponseEntity.ok("Команда управления камерой обработана");
    }
}