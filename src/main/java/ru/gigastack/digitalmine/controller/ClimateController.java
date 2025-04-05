package ru.gigastack.digitalmine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.gigastack.digitalmine.model.SensorData;
import ru.gigastack.digitalmine.repository.SensorDataRepository;

import java.time.LocalDateTime;
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

    @GetMapping("/data")
    public ResponseEntity<List<SensorData>> getClimateData(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end,

            @RequestParam(required = false) Integer limit
    ) {
        if (limit == null || limit <= 0) {
            limit = 100; // по умолчанию вернём до 100 записей
        }

        // Создаём пагинацию и сортировку (самые свежие — первыми)
        Pageable pageable = PageRequest.of(0, limit, Sort.by("timestamp").descending());

        List<SensorData> result;

        if (start == null && end == null) {
            // Ни start, ни end нет: просто берём все
            result = sensorDataRepository.findAllByOrderByTimestampDesc(pageable).getContent();

        } else if (start != null && end != null) {
            // Оба заданы: BETWEEN
            result = sensorDataRepository.findByTimestampBetweenOrderByTimestampDesc(start, end, pageable).getContent();

        } else if (start != null) {
            // Только start
            result = sensorDataRepository.findByTimestampGreaterThanEqualOrderByTimestampDesc(start, pageable).getContent();

        } else {
            // Только end
            result = sensorDataRepository.findByTimestampLessThanEqualOrderByTimestampDesc(end, pageable).getContent();
        }

        return ResponseEntity.ok(result);
    }
}