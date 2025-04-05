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

    public VehicleController(VehicleService vehicleService, MqttClientService mqttClientService) {
        this.vehicleService = vehicleService;
        this.mqttClientService = mqttClientService;
    }

    @PostMapping("/control/start")
    public ResponseEntity<String> startVehicleRoute() {
        logger.info("Инициирован старт маршрута электромобиля");
        vehicleService.startRoute();
        mqttClientService.publish("vehicle/control", "start_route");
        return ResponseEntity.ok("Маршрут электромобиля запущен");
    }

    /**
     * Пример ручного управления через POST /api/vehicle/control/move?direction=forward/backward/left/right
     * Можно передавать параметры в JSON, если удобнее.
     */
    @PostMapping("/control/move")
    public ResponseEntity<String> moveVehicle(@RequestParam String direction) {
        logger.info("Получена команда ручного управления: {}", direction);

        // Можно реализовать логику во VehicleService, либо прямо тут.
        // Для примера - просто отправляем MQTT-команду в "vehicle/control".
        String mqttPayload;
        switch (direction.toLowerCase()) {
            case "forward":
                mqttPayload = "move_forward";
                break;
            case "backward":
                mqttPayload = "move_backward";
                break;
            case "left":
                mqttPayload = "turn_left";
                break;
            case "right":
                mqttPayload = "turn_right";
                break;
            default:
                // Неверное направление
                logger.warn("Неизвестная команда движения: {}", direction);
                return ResponseEntity.badRequest().body("Неизвестная команда: " + direction);
        }
        mqttClientService.publish("vehicle/control", mqttPayload);

        logger.info("MQTT-команда отправлена: {}", mqttPayload);
        return ResponseEntity.ok("Выполнена команда: " + mqttPayload);
    }

    @PostMapping("/control/penalty")
    public ResponseEntity<String> recordPenalty() {
        logger.info("Зафиксировано столкновение с конусом");
        vehicleService.addPenalty();
        return ResponseEntity.ok("Штраф начислен. Текущий штраф: " + vehicleService.getPenaltyCount());
    }

    // Пример получения статуса: штрафы, состояние и т.п.
    @GetMapping("/status")
    public ResponseEntity<String> getVehicleStatus() {
        return ResponseEntity.ok("Текущий штраф: " + vehicleService.getPenaltyCount());
    }
}