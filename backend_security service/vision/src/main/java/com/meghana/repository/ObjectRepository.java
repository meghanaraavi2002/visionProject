package com.meghana.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stark.entity.Objects;

public interface ObjectRepository extends JpaRepository<Objects, Long> {

    Optional<Objects> findTopByNameOrderByIdDesc(String name);

    Optional<Objects> findByTrackAndName(Long long1,String Name);
}