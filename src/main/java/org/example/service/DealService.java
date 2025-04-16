package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.model.Deal;
import org.example.repository.DealRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DealService {

    @Autowired
    private DealRepository dealRepository;

    public Deal save(Deal deal) {
        return dealRepository.save(deal);
    }

}
