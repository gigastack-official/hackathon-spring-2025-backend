package ru.gigastack.digitalmine.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.gigastack.digitalmine.service.MqttClientService;
import ru.gigastack.digitalmine.service.VehicleService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicle")
@CrossOrigin(origins = "*")
public class VehicleController {

    private static final Logger logger = LoggerFactory.getLogger(VehicleController.class);
    private final VehicleService vehicleService;
    private final MqttClientService mqttClientService;

    public VehicleController(VehicleService vehicleService,
                             MqttClientService mqttClientService) {
        this.vehicleService = vehicleService;
        this.mqttClientService = mqttClientService;
    }


    /**
     * Ручное управление движением, поддерживающее диагонали и передачи.
     *
     * Пример запроса:
     *  POST /api/vehicle/control/move?direction=forward-left&action=press&gear=low
     *
     * direction: forward | backward | left | right | forward-left | forward-right | backward-left | backward-right
     * action: press (нажата клавиша) / release (отжата)
     * gear: low / high
     */
    @PostMapping("/control/move")
    public ResponseEntity<String> moveVehicle(
            @RequestParam String direction,
            @RequestParam(defaultValue = "press") String action,
            @RequestParam(defaultValue = "low") String gear
    ) throws JsonProcessingException {
        logger.info("Получена команда управления: direction={}, action={}, gear={}", direction, action, gear);

        Map<String, String> payloadMap = new HashMap<>();
        payloadMap.put("direction", direction);
        payloadMap.put("action", action);
        payloadMap.put("gear", gear);

        ObjectMapper objectMapper = new ObjectMapper();
        String mqttPayload = objectMapper.writeValueAsString(payloadMap);

        mqttClientService.publish("vehicle/control", mqttPayload);
        logger.info("MQTT-команда отправлена: {}", mqttPayload);
        return ResponseEntity.ok("Команда выполнена: " + mqttPayload);
    }

    /**
     * Управление камерой на электромобиле (например, наклон/поворот).
     * direction: left, right, up, down, ...
     * action: press / release
     *
     * Пример:
     *  POST /api/vehicle/control/camera?direction=left&action=press
     */
    @PostMapping("/control/camera")
    public ResponseEntity<String> controlCameraOnVehicle(
            @RequestParam String direction,
            @RequestParam(defaultValue = "press") String action
    ) throws JsonProcessingException {
        logger.info("Управление камерой: direction={}, action={}", direction, action);

        Map<String, String> payloadMap = new HashMap<>();
        payloadMap.put("direction", direction);
        payloadMap.put("action", action);

        ObjectMapper objectMapper = new ObjectMapper();
        String mqttPayload = objectMapper.writeValueAsString(payloadMap);

        mqttClientService.publish("vehicle/control", mqttPayload);
        logger.info("MQTT-команда отправлена: {}", mqttPayload);
        return ResponseEntity.ok("Камера на электромобиле: отправлена команда " + mqttPayload);
    }
}