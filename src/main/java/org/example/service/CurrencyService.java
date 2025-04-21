package org.example.service;

import org.example.model.enums.BalanceType;
import org.example.model.Currency;
import org.example.model.enums.Money;
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
    public void updateBalance(Money money, BalanceType type, long newBalance) {
        Currency currency = getCurrency(money);
        if (currency != null) {
            currency.updateBalance(type, newBalance);
            currencyRepository.save(currency);
        }
    }

    // Получить баланс для определенного типа баланса
    public long getBalance(Money money, BalanceType type) {
        Currency currency = getCurrency(money);
        return currency != null ? currency.getBalance(type) : 0L;
    }

    // Переместить средства между типами балансов (например, с "своего" на "долг")
    public void moveBalance(Money money, BalanceType from, BalanceType to, long amount) {
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
