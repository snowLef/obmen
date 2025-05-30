package org.example.handler.callback;

import lombok.RequiredArgsConstructor;
import org.example.infra.TelegramSender;
import org.example.model.CurrencyAmount;
import org.example.model.Deal;
import org.example.model.User;
import org.example.model.enums.*;
import org.example.service.DealService;
import org.example.service.UserService;
import org.example.ui.MenuService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ExchangeRateTypeCallbackHandler implements CallbackCommandHandler {

    private final TelegramSender telegramSender;
    private final UserService userService;
    private final MenuService menuService;
    private final DealService dealService;

    @Override
    public boolean supports(String data) {
        return switch (data) {
            case "division", "multiplication" -> true;
            default -> false;
        };
    }

    public void handle(CallbackQuery query, User user) {
        Long chatId = query.getMessage().getChatId();
        String data = query.getData();
        CurrencyType type = null;

        Deal deal = user.getCurrentDeal();

        long amountFrom = deal.getAmountFrom();
        long amountTo = deal.getAmountTo();
        AmountType amountType = deal.getAmountType();
        double rate = deal.getExchangeRate();

        switch (data) {
            case "division" -> type = CurrencyType.DIVISION;
            case "multiplication" -> type = CurrencyType.MULTIPLICATION;
        }

        if (amountType == AmountType.GIVE) {
            if (type == CurrencyType.DIVISION) {
                deal.getMoneyTo().get(0).setAmount(Math.round(amountFrom / rate));
            } else if (type == CurrencyType.MULTIPLICATION) {
                deal.getMoneyTo().get(0).setAmount(Math.round(amountFrom * rate));
            }
        } else if (amountType == AmountType.RECEIVE) {
            if (type == CurrencyType.DIVISION) {
                deal.getMoneyFrom().get(0).setAmount(Math.round(amountTo / rate));
            } else if (type == CurrencyType.MULTIPLICATION) {
                deal.getMoneyFrom().get(0).setAmount(Math.round(amountTo * rate));
            }
        }

        user.setCurrentDeal(deal);

        if (user.getStatus().equals(Status.AWAITING_EXCHANGE_RATE_TYPE)) {
            user.pushStatus(Status.AWAITING_APPROVE);
            telegramSender.editMsg(chatId, user.getMessageToEdit(), "Формула расчета: %s".formatted(type.getText()));
            userService.save(user);
            menuService.sendApproveMenu(chatId);
        }
    }
}
