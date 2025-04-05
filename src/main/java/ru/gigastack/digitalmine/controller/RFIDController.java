package ru.gigastack.digitalmine.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.gigastack.digitalmine.dto.RFIDDto;
import ru.gigastack.digitalmine.model.RfidLog;
import ru.gigastack.digitalmine.repository.RfidLogRepository;
import ru.gigastack.digitalmine.service.LightingService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/rfid")
@CrossOrigin(origins = "*")
public class RFIDController {

    private static final Logger logger = LoggerFactory.getLogger(RFIDController.class);

    private final LightingService lightingService;
    private final RfidLogRepository rfidLogRepository;

    public RFIDController(LightingService lightingService, RfidLogRepository rfidLogRepository) {
        this.lightingService = lightingService;
        this.rfidLogRepository = rfidLogRepository;
    }

    @PostMapping("/scan")
    public ResponseEntity<String> scanRFID(@RequestBody RFIDDto rfidDto) {
        logger.info("Считана RFID метка: {}", rfidDto.getTagId());

        // Сохранение в таблицу rfid_log
        RfidLog rfidLog = new RfidLog();
        rfidLog.setTagId(rfidDto.getTagId());
        // Если action не передали, считаем, что это "enter"
        String action = (rfidDto.getAction() == null || rfidDto.getAction().isBlank())
                ? "enter"
                : rfidDto.getAction().toLowerCase();

        rfidLog.setAction(action);
        rfidLog.setTimestamp(LocalDateTime.now());
        rfidLogRepository.save(rfidLog);

        // Логируем
        logger.info("RFID данные отправлены на веб-интерфейс: {}, действие: {}", rfidDto.getTagId(), action);

        // Автоматическое управление освещением в зависимости от действия
        switch (action) {
            case "exit":
                logger.info("Освещение: ЖЁЛТЫЙ (выход)");
                lightingService.overrideLighting("#FFFF00", 100);
                break;
            case "enter":
            default:
                logger.info("Освещение: БЕЛЫЙ (вход)");
                lightingService.overrideLighting("#FFFFFF", 100);
                break;
        }

        return ResponseEntity.ok("RFID метка обработана, действие: " + action);
    }

    @GetMapping("/logs")
    public ResponseEntity<List<RfidLog>> getAllRfidLogs() {
        List<RfidLog> logs = rfidLogRepository.findAll();
        return ResponseEntity.ok(logs);
    }
}