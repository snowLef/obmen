package org.example.service;

import org.example.infra.TelegramSender;
import org.example.model.User;
import org.example.model.enums.BalanceType;
import org.example.model.Currency;
import org.example.model.enums.Money;
import org.example.repository.CurrencyRepository;
import org.example.ui.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CurrencyService {

    private CurrencyRepository currencyRepository;
    private TelegramSender telegramSender;
    private ExchangeProcessor exchangeProcessor;
    private MenuService menuService;

    @Autowired
    public void setCurrencyRepository(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    @Autowired
    public void setMenuService(MenuService menuService) {
        this.menuService = menuService;
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
    public void moveBalance(long chatId, Money money, BalanceType from, BalanceType to, long amount) {

        long fromBalance = getBalance(money, from);
        if (fromBalance >= amount || from == BalanceType.OWN) {
            Currency currency = getCurrency(money);
            if (currency != null) {
                currency.move(from, to, amount);
                currencyRepository.save(currency);
            }
        } else {
            telegramSender.sendText(chatId, "Недостаточно средств на " + from.getDisplayName());
            menuService.sendMainMenu(chatId);
            exchangeProcessor.cancel(chatId);
            throw new IllegalArgumentException("Not enough balance in " + from);
        }
    }

    // Получить валюту по названию
    private Currency getCurrency(Money money) {
        return currencyRepository.findByName(money.name()).orElse(null);
    }

    @Autowired
    public void setTelegramSender(TelegramSender telegramSender) {
        this.telegramSender = telegramSender;
    }

    @Autowired
    public void setExchangeProcessor(ExchangeProcessor exchangeProcessor) {
        this.exchangeProcessor = exchangeProcessor;
    }
}
