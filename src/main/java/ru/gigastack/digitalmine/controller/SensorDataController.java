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

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/sensors")
@CrossOrigin(origins = "*")
public class SensorDataController {

    private static final Logger logger = LoggerFactory.getLogger(SensorDataController.class);
    private final SensorDataRepository sensorDataRepository;

    public SensorDataController(SensorDataRepository sensorDataRepository) {
        this.sensorDataRepository = sensorDataRepository;
    }

    @PostMapping("/data")
    public ResponseEntity<String> receiveSensorData(@RequestBody @Valid SensorDataDto sensorDataDto) {
        logger.info("Получены данные датчика: {}", sensorDataDto);

        // Преобразуем DTO в сущность и сохраняем в базу данных
        SensorData sensorData = new SensorData();
        sensorData.setGasLevel(sensorDataDto.getGasLevel());
        sensorData.setTemperature(sensorDataDto.getTemperature());
        sensorData.setHumidity(sensorDataDto.getHumidity());
        sensorData.setTimestamp(LocalDateTime.now());

        sensorDataRepository.save(sensorData);

        return ResponseEntity.status(HttpStatus.OK).body("Данные получены и сохранены");
    }
}