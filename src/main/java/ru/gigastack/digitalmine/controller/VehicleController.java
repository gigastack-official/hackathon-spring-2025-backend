package ru.gigastack.digitalmine.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.gigastack.digitalmine.service.MqttClientService;
import ru.gigastack.digitalmine.service.VehicleService;

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
    ) {
        logger.info("Получена команда управления: direction={}, action={}, gear={}", direction, action, gear);

        // Формируем MQTT-пэйлоад, чтобы устройство знало, что делать
        String mqttPayload = String.format("move_%s_%s_%s", direction, action, gear);
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
    ) {
        logger.info("Управление камерой: direction={}, action={}", direction, action);

        String mqttPayload = String.format("camera_%s_%s", direction, action);
        mqttClientService.publish("vehicle/control", mqttPayload);

        return ResponseEntity.ok("Камера на электромобиле: отправлена команда " + mqttPayload);
    }


    /**
     * Пример получения статуса (количество штрафов и пр.).
     */
    @GetMapping("/status")
    public ResponseEntity<String> getVehicleStatus() {
        return ResponseEntity.ok("Текущий штраф: " + vehicleService.getPenaltyCount());
    }
}