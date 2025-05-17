package org.example.repository;

import org.example.model.BalanceEvent;
import org.example.model.enums.BalanceType;
import org.example.model.enums.Money;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BalanceEventRepository extends JpaRepository<BalanceEvent, Long> {
    @Query("SELECT SUM(be.delta) FROM BalanceEvent be WHERE be.currency = :cur AND be.type = :type")
    Long sumDeltasByCurrencyAndType(@Param("cur") Money cur, @Param("type") BalanceType type);

    List<BalanceEvent> findByDealId(Long dealId);
}

