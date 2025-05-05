package org.example.repository;

import org.example.model.Currency;
import org.example.model.Deal;
import org.example.model.enums.DealStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {
    // находим все FIX‐сделки пользователя
    List<Deal> findByStatus(DealStatus status);
}
