package ru.gigastack.digitalmine.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.gigastack.digitalmine.dto.CameraControlDto;
import ru.gigastack.digitalmine.service.MqttClientService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/camera")
@CrossOrigin(origins = "*")
public class CameraController {

    private static final Logger logger = LoggerFactory.getLogger(CameraController.class);
    private final MqttClientService mqttClientService;

    public CameraController(MqttClientService mqttClientService) {
        this.mqttClientService = mqttClientService;
    }

    @PostMapping("/control")
    public ResponseEntity<String> controlCamera(@RequestBody CameraControlDto command) {
        logger.info("Получена команда управления камерой: {}", command);

        String mqttPayload = String.format("rotate:%d,lightOn:%b,intensity:%d",
                command.getRotationAngle(),
                command.getLightOn(),
                command.getLightIntensity());
        mqttClientService.publish("camera/control", mqttPayload);

        return ResponseEntity.ok("Команда управления камерой обработана");
    }

    @GetMapping("/recognize")
    public ResponseEntity<Map<String, Object>> recognizeObjects() {
        // Симуляция распознавания объектов без классификации
        List<String> objects = Arrays.asList("Деталь1", "Деталь2", "Человек");
        Map<String, Object> response = new HashMap<>();
        response.put("recognizedObjects", objects);
        logger.info("Распознаны объекты (без классификации): {}", objects);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recognize/classified")
    public ResponseEntity<Map<String, Object>> recognizeObjectsWithClassification() {
        // Симуляция распознавания объектов с классификацией
        List<Map<String, Object>> objects = Arrays.asList(
                new HashMap<String, Object>() {{
                    put("object", "Человек");
                    put("confidence", 0.95);
                }},
                new HashMap<String, Object>() {{
                    put("object", "Грузовик");
                    put("confidence", 0.90);
                }},
                new HashMap<String, Object>() {{
                    put("object", "Оборудование");
                    put("confidence", 0.85);
                }}
        );
        Map<String, Object> response = new HashMap<>();
        response.put("recognizedObjects", objects);
        logger.info("Распознаны объекты с классификацией: {}", objects);
        return ResponseEntity.ok(response);
    }
}