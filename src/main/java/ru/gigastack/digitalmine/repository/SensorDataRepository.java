package ru.gigastack.digitalmine.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.gigastack.digitalmine.model.SensorData;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SensorDataRepository extends JpaRepository<SensorData, Long> {

    // Найти все записи с сортировкой по timestamp (по убыванию)
    Page<SensorData> findAllByOrderByTimestampDesc(Pageable pageable);

    // Найти записи, у которых timestamp >= start AND timestamp <= end
    Page<SensorData> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end, Pageable pageable);

    // Найти записи, у которых timestamp >= start
    Page<SensorData> findByTimestampGreaterThanEqualOrderByTimestampDesc(LocalDateTime start, Pageable pageable);

    // Найти записи, у которых timestamp <= end
    Page<SensorData> findByTimestampLessThanEqualOrderByTimestampDesc(LocalDateTime end, Pageable pageable);

    Optional<SensorData> findTopByOrderByTimestampDesc();
}