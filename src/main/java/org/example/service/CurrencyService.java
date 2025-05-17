package org.example.service;

import jakarta.transaction.Transactional;
import org.example.infra.TelegramSender;
import org.example.model.*;
import org.example.model.enums.BalanceType;
import org.example.model.enums.DealStatus;
import org.example.model.enums.Money;
import org.example.repository.BalanceEventRepository;
import org.example.repository.CurrencyRepository;
import org.example.repository.DealRepository;
import org.example.ui.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class CurrencyService {

    private CurrencyRepository currencyRepository;
    private TelegramSender telegramSender;
    private ExchangeProcessor exchangeProcessor;
    private MenuService menuService;
    private BalanceEventRepository repo;
    private DealRepository dealRepo;


    @Autowired
    public void setCurrencyRepository(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    @Lazy
    @Autowired
    public void setMenuService(MenuService menuService) {
        this.menuService = menuService;
    }

    @Autowired
    public void setTelegramSender(TelegramSender telegramSender) {
        this.telegramSender = telegramSender;
    }

    @Autowired
    public void setExchangeProcessor(ExchangeProcessor exchangeProcessor) {
        this.exchangeProcessor = exchangeProcessor;
    }

    @Autowired
    public void setRepo(BalanceEventRepository repo) {
        this.repo = repo;
    }

    @Autowired
    public void setDealRepo(DealRepository dealRepo) {
        this.dealRepo = dealRepo;
    }

    public long getBalance(Money money, BalanceType type) {
        Currency currency = getCurrency(money);
        return currency != null ? currency.getBalance(type) : 0L;
    }

    @Transactional
    public void applyDeal(Deal deal) {
        // 2) Снимаем moneyFrom
        for (CurrencyAmount from : deal.getMoneyFrom()) {
            // 2.1) История
            repo.save(new BalanceEvent(
                    deal.getId(),
                    from.getCurrency(),
                    deal.getBalanceTypeFrom(),
                    -from.getAmount()
            ));
            // 2.2) Новый остаток = старый − amount
            long oldFrom = getBalance(from.getCurrency(), deal.getBalanceTypeFrom());
            updateBalance(
                    from.getCurrency(),
                    deal.getBalanceTypeFrom(),
                    oldFrom - from.getAmount()
            );
        }

        // 3) Кладём moneyTo
        for (CurrencyAmount to : deal.getMoneyTo()) {
            repo.save(new BalanceEvent(
                    deal.getId(),
                    to.getCurrency(),
                    deal.getBalanceTypeTo(),
                    +to.getAmount()
            ));
            long oldTo = getBalance(to.getCurrency(), deal.getBalanceTypeTo());
            updateBalance(
                    to.getCurrency(),
                    deal.getBalanceTypeTo(),
                    oldTo + to.getAmount()
            );
        }
        deal.setStatus(DealStatus.APPLIED);
        dealRepo.save(deal);
    }

    @Transactional
    public void compensateDeal(Long dealId) {
        // 1) достаём все события исходной сделки
        List<BalanceEvent> evs = repo.findByDealId(dealId);

        for (BalanceEvent ev : evs) {
            // 2) записываем зеркальную проводку в историю
            BalanceEvent inverse = new BalanceEvent(
                    dealId,
                    ev.getCurrency(),
                    ev.getType(),
                    -ev.getDelta()
            );
            repo.save(inverse);

            // 3) откатываем текущий остаток в Currency/CurrencyBalance
            //    (аналогично тому, как вы делаете это в applyDeal)
            long old = getBalance(ev.getCurrency(), ev.getType());
            long updated = old - ev.getDelta();
            // ev.getDelta() может быть положительным (при кладе) или отрицательным (при снятии),
            // поэтому old - ev.getDelta() зеркально делает откат.
            updateBalance(ev.getCurrency(), ev.getType(), updated);
        }
    }

    public boolean canApply(Deal deal) {
        // тип баланса, из которого снимаем
        BalanceType fromType = deal.getBalanceTypeFrom();

        for (CurrencyAmount from : deal.getMoneyFrom()) {
            // пропускаем проверку, если это OWN (можно в минус)
            if (fromType == BalanceType.OWN) {
                continue;
            }
            long bal = getBalance(from.getCurrency(), fromType);
            if (bal < from.getAmount()) {
                return false;
            }
        }
        return true;
    }

    // Обновить баланс для определенного типа баланса (свой, чужой, долг и т.д.)
    public void updateBalance(Money money, BalanceType type, long newBalance) {
        Currency currency = getCurrency(money);
        if (currency != null) {
            currency.updateBalance(type, newBalance);
            currencyRepository.save(currency);
        }
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

    public Map<Money, Long> calculateFixedDeltas() {
        List<Deal> fixed = dealRepo.findByStatus(DealStatus.FIX);
        Map<Money, Long> deltaByCurrency = new EnumMap<>(Money.class);

        for (Deal d : fixed) {
            for (CurrencyAmount from : d.getMoneyFrom()) {
                deltaByCurrency.merge(
                        from.getCurrency(),
                        -from.getAmount(),
                        Long::sum
                );
            }
            for (CurrencyAmount to : d.getMoneyTo()) {
                deltaByCurrency.merge(
                        to.getCurrency(),
                        +to.getAmount(),
                        Long::sum
                );
            }
        }
        return deltaByCurrency;
    }
}
