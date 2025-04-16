package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.model.Currency;
import org.example.model.Money;
import org.example.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private CurrencyRepository currencyRepository;

    public void updateBalance(Money money, double newBalance) {
        Currency currency = getCurrency(money);
        currency.setBalance(newBalance);
        currencyRepository.save(currency);
    }

    public double getBalance(Money money) {
        Currency currency = getCurrency(money);
        return currency != null ? currency.getBalance() : 0.0;
    }

    public Currency getCurrency(Money name) {
        return currencyRepository.findByName(name.name())
                .orElse(null);
    }

}
