package com.example.prefschedule.repository;

import com.example.prefschedule.entity.Pack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface PackRepository extends JpaRepository<Pack, Long> {

    List<Pack> findByYear(Integer year);

    @Query("SELECT p FROM Pack p WHERE p.name LIKE %:name%")
    List<Pack> searchByName(String name);
    @Modifying
    @Transactional
    @Query("UPDATE Pack p SET p.name = :name WHERE p.id = :id")
    void updateName(Long id, String name);
    List<Pack> findByYearAndSemester(Integer year, Integer semester);
}
