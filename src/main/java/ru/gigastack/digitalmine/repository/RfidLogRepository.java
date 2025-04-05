package ru.gigastack.digitalmine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.gigastack.digitalmine.model.RfidLog;

@Repository
public interface RfidLogRepository extends JpaRepository<RfidLog, Long> {
    // При желании можно добавить методы поиска по tagId или другим полям
}