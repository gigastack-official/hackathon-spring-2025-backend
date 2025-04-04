package ru.gigastack.digitalmine.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class VehicleService {

    private static final Logger logger = LoggerFactory.getLogger(VehicleService.class);
    private int penaltyCount = 0;

    // Симуляция выполнения маршрута электромобиля
    public void startRoute() {
        logger.info("Электромобиль начинает маршрут из точки А в точку Б, затем разворот и возврат в точку А");
        // Здесь можно добавить логику для симуляции маршрута
    }

    // Добавление штрафа за сбитый конус
    public void addPenalty() {
        penaltyCount++;
        logger.info("Добавлен штраф. Текущий счет штрафов: {}", penaltyCount);
    }

    public int getPenaltyCount() {
        return penaltyCount;
    }
}