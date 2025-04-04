package ru.gigastack.digitalmine.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.gigastack.digitalmine.dto.SensorDataDto;
import ru.gigastack.digitalmine.model.SensorData;
import ru.gigastack.digitalmine.repository.SensorDataRepository;
import ru.gigastack.digitalmine.service.LightingService;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/sensors")
@CrossOrigin(origins = "*")
public class SensorDataController {

    private static final Logger logger = LoggerFactory.getLogger(SensorDataController.class);
    private final SensorDataRepository sensorDataRepository;
    private final LightingService lightingService;

    // Пороговое значение для загазованности (например, 50.0)
    private static final double GAS_THRESHOLD = 50.0;

    public SensorDataController(SensorDataRepository sensorDataRepository, LightingService lightingService) {
        this.sensorDataRepository = sensorDataRepository;
        this.lightingService = lightingService;
    }

    @PostMapping("/data")
    public ResponseEntity<String> receiveSensorData(@RequestBody @Valid SensorDataDto sensorDataDto) {
        logger.info("Получены данные датчика: {}", sensorDataDto);

        SensorData sensorData = new SensorData();
        sensorData.setGasLevel(sensorDataDto.getGasLevel());
        sensorData.setTemperature(sensorDataDto.getTemperature());
        sensorData.setHumidity(sensorDataDto.getHumidity());
        sensorData.setTimestamp(LocalDateTime.now());

        sensorDataRepository.save(sensorData);
        logger.info("Данные датчика сохранены в БД");

        // Если уровень газа превышает порог, переключаем освещение в аварийный режим (красный)
        if (sensorData.getGasLevel() > GAS_THRESHOLD) {
            logger.warn("Уровень газа ({}) превышает порог ({}), переключаем освещение на красный", sensorData.getGasLevel(), GAS_THRESHOLD);
            lightingService.overrideLighting("red", 100);
        }

        return ResponseEntity.status(HttpStatus.OK).body("Данные получены и сохранены");
    }

    // Новый GET‑эндпоинт для получения последних данных (например, для страницы "gas")
    @GetMapping("/latest")
    public ResponseEntity<SensorData> getLatestSensorData() {
        Optional<SensorData> latestData = sensorDataRepository.findTopByOrderByTimestampDesc();
        return latestData.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }
}