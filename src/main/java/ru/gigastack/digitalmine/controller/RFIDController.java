package ru.gigastack.digitalmine.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.gigastack.digitalmine.dto.RFIDDto;
import ru.gigastack.digitalmine.service.LightingService;

@RestController
@RequestMapping("/api/rfid")
@CrossOrigin(origins = "*")
public class RFIDController {

    private static final Logger logger = LoggerFactory.getLogger(RFIDController.class);
    private final LightingService lightingService;

    public RFIDController(LightingService lightingService) {
        this.lightingService = lightingService;
    }

    @PostMapping("/scan")
    public ResponseEntity<String> scanRFID(@RequestBody RFIDDto rfidDto) {
        logger.info("Считана RFID метка: {}", rfidDto.getTagId());
        // Вывод данных в монитор порта (симуляция)
        System.out.println("RFID метка: " + rfidDto.getTagId());

        // Вывод данных на веб-интерфейс — здесь просто логируем
        logger.info("RFID данные отправлены на веб-интерфейс: {}", rfidDto.getTagId());

        // Автоматическое управление освещением по данным RFID:
        // При входе человека свет должен становиться белым
        lightingService.overrideLighting("white", 100);
        logger.info("Освещение автоматически установлено в белый цвет при входе через RFID");

        // Логирование действия RFID (в дальнейшем можно сохранять в БД)
        logger.info("Действие RFID логировано для метки: {}", rfidDto.getTagId());

        return ResponseEntity.ok("RFID метка обработана");
    }
}