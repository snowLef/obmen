package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.model.Deal;
import org.example.repository.DealRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DealService {

    private DealRepository dealRepository;

    @Autowired
    public void setDealRepository(DealRepository dealRepository) {
        this.dealRepository = dealRepository;
    }

    public void save(Deal deal) {
        dealRepository.save(deal);
    }

}
