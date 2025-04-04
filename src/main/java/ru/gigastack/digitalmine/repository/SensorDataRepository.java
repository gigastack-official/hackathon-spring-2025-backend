package ru.gigastack.digitalmine.repository;

import ru.gigastack.digitalmine.model.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
    // При необходимости можно добавить методы для поиска по диапазону времени и т.д.
}