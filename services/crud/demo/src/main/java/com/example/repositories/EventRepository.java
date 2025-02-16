package com.example.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entities.EventEntity;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, String> {
}

