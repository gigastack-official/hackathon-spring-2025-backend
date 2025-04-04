package ru.gigastack.digitalmine.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.gigastack.digitalmine.dto.SensorDataDto;

@RestController
@RequestMapping("/api/sensors")
public class SensorDataController {
    private static final Logger logger = LoggerFactory.getLogger(SensorDataController.class);

    @PostMapping("/data")
    public ResponseEntity<String> receiveSensorData(@RequestBody @Valid SensorDataDto sensorDataDto) {
        // Здесь можно добавить обработку данных: сохранить в БД, отправить через MQTT, переслать по WebSocket и т.д.
        logger.info("Получены данные датчика: {}", sensorDataDto);
        return ResponseEntity.status(HttpStatus.OK).body("Данные получены");
    }
}