package ru.kotov.orders.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.kotov.orders.domain.OutboxEvent;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("SELECT o FROM OutboxEvent o WHERE o.publishedAt IS NULL ORDER BY o.createdAt ASC")
    List<OutboxEvent> findUnpublished();

    @Modifying
    @Transactional
    @Query("UPDATE OutboxEvent o SET o.publishedAt = :publishedAt WHERE o.id = :id")
    void markAsPublished(@Param("id") Long id, @Param("publishedAt") LocalDateTime publishedAt);

    @Modifying
    @Transactional
    @Query("DELETE FROM OutboxEvent o WHERE o.publishedAt IS NOT NULL AND o.createdAt < :cutoff")
    int deleteOldPublishedEvents(@Param("cutoff") LocalDateTime cutoff);
}
