package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.infra.TelegramSender;
import org.example.model.*;
import org.example.model.enums.DealStatus;
import org.example.repository.DealRepository;
import org.example.ui.MenuService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeProcessor {

    private final UserService userService;
    private final CurrencyService currencyService;
    private final TelegramSender telegramSender;
    private final MenuService menuService;
    private final DealRepository dealRepo;

    public void cancel(long chatId) {
        User u = userService.getUser(chatId);
        Deal d = u.getCurrentDeal();
        if (d != null && d.isApproved()) {
            currencyService.compensateDeal(d.getId());
            telegramSender.sendText(chatId, "Сделка отменена и баланс восстановлен.");
        } else {
            telegramSender.sendText(chatId, "Сделка отменена.");
        }

        telegramSender.deleteMessages(chatId, userService.getMessageIdsToDeleteWithInit(chatId));
        userService.resetUserState(u);
        menuService.sendBalance(chatId);
        menuService.sendMainMenu(chatId);
    }

    public void cancel(long chatId, long dealId) {
        Deal deal = dealRepo.findById(dealId)
                .orElseThrow(() -> new IllegalArgumentException("Сделка не найдена: " + dealId));

        if (deal.isApproved()) {
            // 1) Откатываем изменения баланса
            currencyService.compensateDeal(dealId);

            // 2) Сбрасываем флаг approved (если нужно)
            deal.setApproved(false);
            deal.setStatus(DealStatus.CANCELLED);
            deal.setCancelledAt(LocalDateTime.now());
            dealRepo.save(deal);

            telegramSender.sendText(chatId, "Сделка отменена и баланс восстановлен.");
        } else {
            telegramSender.sendText(chatId, "Сделка не была подтверждена или уже отменена.");
        }

        // 3) Если у пользователя всё ещё висит currentDeal(), сбросим статус
        User user = userService.getUser(chatId);
        if (user.getCurrentDeal() != null && user.getCurrentDeal().getId().equals(dealId)) {
            userService.resetUserState(user);
            user.setCurrentDeal(null);
            userService.save(user);
        }

        // 4) Финальный UI
        menuService.sendBalance(chatId);
        menuService.sendMainMenu(chatId);
    }

    public void approve(long chatId, Deal d) {
        User u = userService.getUser(chatId);

        // проверка
        if (!currencyService.canApply(d)) {
            telegramSender.sendText(chatId, "Недостаточно средств для сделки.");
        } else {
            d.setApproved(true);
            dealRepo.save(d);
            currencyService.applyDeal(d);
            menuService.sendDealCompletedWithCancel(chatId, d);
        }

        telegramSender.deleteMessages(chatId, userService.getMessageIdsToDeleteWithInit(chatId));
        userService.resetUserState(u);
        menuService.sendBalance(chatId);
        menuService.sendMainMenu(chatId);
    }

}