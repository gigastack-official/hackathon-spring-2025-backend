package ru.gigastack.digitalmine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.gigastack.digitalmine.model.SensorData;
import ru.gigastack.digitalmine.repository.SensorDataRepository;

import java.util.List;

@RestController
@RequestMapping("/api/climate")
@CrossOrigin(origins = "*")
public class ClimateController {

    private final SensorDataRepository sensorDataRepository;

    @Autowired
    public ClimateController(SensorDataRepository sensorDataRepository) {
        this.sensorDataRepository = sensorDataRepository;
    }

    // Возвращает данные для построения графиков климата (температура, влажность)
    @GetMapping("/data")
    public ResponseEntity<List<SensorData>> getClimateData() {
        List<SensorData> data = sensorDataRepository.findAll();
        return ResponseEntity.ok(data);
    }
}