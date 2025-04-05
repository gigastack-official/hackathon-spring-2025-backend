package ru.gigastack.digitalmine.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.gigastack.digitalmine.dto.RFIDDto;
import ru.gigastack.digitalmine.model.RfidLog;
import ru.gigastack.digitalmine.repository.RfidLogRepository;
import ru.gigastack.digitalmine.service.CardUserService;
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
    private final CardUserService cardUserService; // <-- сервис для поиска пользователей по карте

    public RFIDController(
            LightingService lightingService,
            RfidLogRepository rfidLogRepository,
            CardUserService cardUserService
    ) {
        this.lightingService = lightingService;
        this.rfidLogRepository = rfidLogRepository;
        this.cardUserService = cardUserService;
    }

    @PostMapping("/scan")
    public ResponseEntity<String> scanRFID(@RequestBody RFIDDto rfidDto) {
        logger.info("Считана RFID метка: {}", rfidDto.getTagId());

        // Определяем action
        String action = (rfidDto.getAction() == null || rfidDto.getAction().isBlank())
                ? "enter"
                : rfidDto.getAction().toLowerCase();

        // Ищем, зарегистрирована ли эта карта
        boolean cardExists = cardUserService.cardExists(rfidDto.getTagId());
        String status = cardExists ? "allowed" : "denied";

        // Сохраняем в таблицу rfid_log
        RfidLog rfidLog = new RfidLog();
        rfidLog.setTagId(rfidDto.getTagId());
        rfidLog.setAction(action);
        rfidLog.setStatus(status);
        rfidLog.setTimestamp(LocalDateTime.now());
        rfidLogRepository.save(rfidLog);

        logger.info("RFID: tagId={}, action={}, status={}", rfidDto.getTagId(), action, status);

        // Автоматическое управление освещением в зависимости от действия
        switch (action) {
            case "exit":
                logger.info("Освещение: Жёлтый (выход)");
                lightingService.overrideLighting("#FFFF00", 100);

                // Пример "автоматического" восстановления через 5 секунд в отдельном потоке
                new Thread(() -> {
                    try {
                        Thread.sleep(5000L);
                        logger.info("Прошло 5 секунд после выхода, восстанавливаем настройки.");
                        lightingService.restoreUserSettings();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();

                break;

            case "enter":
            default:
                logger.info("Освещение: Белый (вход)");
                lightingService.overrideLighting("#FFFFFF", 100);
                break;
        }

        return ResponseEntity.ok("RFID метка обработана: action=" + action + ", status=" + status);
    }

    @GetMapping("/logs")
    public ResponseEntity<List<RfidLog>> getAllRfidLogs() {
        List<RfidLog> logs = rfidLogRepository.findAll();
        return ResponseEntity.ok(logs);
    }
}