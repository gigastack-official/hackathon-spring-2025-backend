package ru.gigastack.digitalmine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.gigastack.digitalmine.model.RfidLog;

import java.util.Optional;

@Repository
public interface RfidLogRepository extends JpaRepository<RfidLog, Long> {

    // Важно: укажите, как в JPA найти последнюю запись по tagId (по timestamp убыванию).
    Optional<RfidLog> findTopByTagIdOrderByTimestampDesc(String tagId);
}