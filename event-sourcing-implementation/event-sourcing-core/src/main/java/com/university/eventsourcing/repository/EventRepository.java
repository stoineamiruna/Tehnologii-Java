package com.university.eventsourcing.repository;

import com.university.eventsourcing.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByAggregateIdOrderByVersionAsc(String aggregateId);
    List<Event> findByAggregateIdAndVersionGreaterThanOrderByVersionAsc(String aggregateId, Long version);
}