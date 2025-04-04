package ru.gigastack.digitalmine.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.gigastack.digitalmine.service.VehicleService;
import ru.gigastack.digitalmine.service.MqttClientService;

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

    @PostMapping("/control/penalty")
    public ResponseEntity<String> recordPenalty() {
        logger.info("Зафиксировано столкновение с конусом");
        vehicleService.addPenalty();
        return ResponseEntity.ok("Штраф начислен. Текущий штраф: " + vehicleService.getPenaltyCount());
    }

    // Новый GET‑эндпоинт для получения статуса электромобиля (например, количество штрафов)
    @GetMapping("/status")
    public ResponseEntity<String> getVehicleStatus() {
        return ResponseEntity.ok("Текущий штраф: " + vehicleService.getPenaltyCount());
    }
}