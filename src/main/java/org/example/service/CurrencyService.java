package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.model.BalanceType;
import org.example.model.Currency;
import org.example.model.Money;
import org.example.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CurrencyService {

    private CurrencyRepository currencyRepository;

    @Autowired
    public void setCurrencyRepository(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    // Обновить баланс для определенного типа баланса (свой, чужой, долг и т.д.)
    public void updateBalance(Money money, BalanceType type, double newBalance) {
        Currency currency = getCurrency(money);
        if (currency != null) {
            currency.updateBalance(type, newBalance);
            currencyRepository.save(currency);
        }
    }

    // Получить баланс для определенного типа баланса
    public double getBalance(Money money, BalanceType type) {
        Currency currency = getCurrency(money);
        return currency != null ? currency.getBalance(type) : 0.0;
    }

    // Переместить средства между типами балансов (например, с "своего" на "долг")
    public void moveBalance(Money money, BalanceType from, BalanceType to, double amount) {
        Currency currency = getCurrency(money);
        if (currency != null) {
            currency.move(from, to, amount);
            currencyRepository.save(currency);
        }
    }

    // Получить валюту по названию
    private Currency getCurrency(Money money) {
        return currencyRepository.findByName(money.name()).orElse(null);
    }

}
