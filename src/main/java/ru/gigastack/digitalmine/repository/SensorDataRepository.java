package ru.gigastack.digitalmine.repository;

import ru.gigastack.digitalmine.model.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
    Optional<SensorData> findTopByOrderByTimestampDesc();
}