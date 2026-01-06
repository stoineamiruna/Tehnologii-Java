package com.university.eventsourcing.repository;

import com.university.eventsourcing.domain.Snapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SnapshotRepository extends JpaRepository<Snapshot, Long> {
    Optional<Snapshot> findTopByAggregateIdOrderByVersionDesc(String aggregateId);
}