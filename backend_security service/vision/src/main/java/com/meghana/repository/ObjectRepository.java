package com.meghana.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stark.entity.Objects;

public interface ObjectRepository extends JpaRepository<Objects, Long> {

}
