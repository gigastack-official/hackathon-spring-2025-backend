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
@RequestMapping("/api/rfid12")
@CrossOrigin(origins = "*")
public class RFIDController {

    private static final Logger logger = LoggerFactory.getLogger(RFIDController.class);

    private final LightingService lightingService;
    private final RfidLogRepository rfidLogRepository;
    private final CardUserService cardUserService;

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

        // Проверяем, зарегистрирована ли эта карта
        boolean cardExists = cardUserService.cardExists(rfidDto.getTagId());
        String status = cardExists ? "allowed" : "denied";

        // Запись в rfid_log
        RfidLog rfidLog = new RfidLog();
        rfidLog.setTagId(rfidDto.getTagId());
        rfidLog.setAction(action);
        rfidLog.setStatus(status);
        rfidLog.setTimestamp(LocalDateTime.now());
        rfidLogRepository.save(rfidLog);

        // Логируем
        logger.info("RFID Log saved. Tag={}, Action={}, Status={}", rfidDto.getTagId(), action, status);

        // Если «denied», можно не включать свет или включать красный, как угодно
        if ("denied".equals(status)) {
            logger.warn("Доступ отклонён для метки {}", rfidDto.getTagId());
            // например, свет моргает красным или ничего не делаем
            // lightingService.overrideLighting("#FF0000", 100);
            // ...
        } else {
            // Если «allowed» — тогда делаем обычную логику освещения
            switch (action) {
                case "exit":
                    logger.info("Освещение: Жёлтый (выход)");
                    lightingService.overrideLighting("#FFFF00", 100);
                    // Можно через X секунд вернуть
                    new Thread(() -> {
                        try {
                            Thread.sleep(5000L);
                            lightingService.restoreUserSettings();
                            logger.info("Свет восстановлен после выхода");
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
        }

        return ResponseEntity.ok(
                "RFID метка: " + rfidDto.getTagId() +
                        ", action=" + action +
                        ", status=" + status
        );
    }

    // получить все логи
    @GetMapping("/logs")
    public ResponseEntity<List<RfidLog>> getAllRfidLogs() {
        List<RfidLog> logs = rfidLogRepository.findAll();
        return ResponseEntity.ok(logs);
    }
}