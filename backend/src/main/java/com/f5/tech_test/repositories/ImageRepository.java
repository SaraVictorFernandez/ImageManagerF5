package com.f5.tech_test.repositories;

import com.f5.tech_test.entities.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    // Basic CRUD operations are automatically provided by JpaRepository
} 