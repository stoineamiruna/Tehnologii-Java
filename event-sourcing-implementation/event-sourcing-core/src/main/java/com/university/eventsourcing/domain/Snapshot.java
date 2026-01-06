package com.university.eventsourcing.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "snapshots")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Snapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String aggregateId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String snapshotData;

    @Column(nullable = false)
    private Long version;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public Snapshot(String aggregateId, String snapshotData, Long version, LocalDateTime timestamp) {
        this.aggregateId = aggregateId;
        this.snapshotData = snapshotData;
        this.version = version;
        this.timestamp = timestamp;
    }
}