package org.example.handler;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.constants.BotCommands;
import org.example.model.*;
import org.example.service.DealService;
import org.example.service.UserService;
import org.example.service.ExchangeProcessor;
import org.example.state.Status;
import org.example.util.MessageUtils;
import org.example.ui.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import static org.example.model.Money.*;

@Component
@RequiredArgsConstructor
public class CallbackHandler {

    User user;

    private final UserService userService;

    public void handle(CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getMessage().getChatId();
        String data = callbackQuery.getData();
        user = userService.getOrCreate(chatId);

        System.out.println("[" + chatId + "] Статус: " + user.getStatus() + ", CallBackData: " + data);

        switch (data) {
            case "Usd" -> handleCurrencySelection(chatId, USD);
            case "Eur" -> handleCurrencySelection(chatId, EUR);
            case "UsdW" -> handleCurrencySelection(chatId, USDW);
            case "UsdT" -> handleCurrencySelection(chatId, USDT);
            case "Y.e." -> handleCurrencySelection(chatId, YE);

            case BotCommands.BALANCE -> MenuService.sendBalance(chatId);

            case "yes" -> {
                ExchangeProcessor.approve(chatId);
                MenuService.sendMainMenu(chatId);
            }
            case "no" -> {
                ExchangeProcessor.cancel(chatId);
                MenuService.sendMainMenu(chatId);
            }

            case "give" -> handleAmountSelection(chatId, AmountType.GIVE);
            case "receive" -> handleAmountSelection(chatId, AmountType.RECEIVE);

            case "division" -> handleAmountTypeSelection(chatId, CurrencyType.DIVISION);
            case "multiplication" -> handleAmountTypeSelection(chatId, CurrencyType.MULTIPLICATION);

            default -> MessageUtils.sendText(chatId, "Неизвестная команда.");
        }
    }

    private void handleCurrencySelection(Long chatId, Money money) {
        Deal deal = user.getCurrentDeal();
        if (user.getStatus() == Status.AWAITING_FIRST_CURRENCY) {
            deal.setMoneyTo(money);
            userService.saveUserStatus(chatId, Status.AWAITING_SECOND_CURRENCY);
            MessageUtils.editMsg(chatId, user.getMessageToEdit(), "Получение: " + deal.getMoneyTo().getName());
            MenuService.sendSelectCurrency(chatId, "Выберите валюту выдачи");
        } else if (user.getStatus() == Status.AWAITING_SECOND_CURRENCY) {
            deal.setMoneyFrom(money);
            MessageUtils.editMsg(chatId, user.getMessageToEdit(), "Выдача: " + deal.getMoneyFrom().getName());
            Message message = MessageUtils.sendText(chatId, "Введите сумму: ");
            user.setStatus(Status.AWAITING_DEAL_AMOUNT);
            userService.save(user);
            userService.addMessageToDel(chatId, message.getMessageId());
        }
        DealService.saveOrUpdate(deal);
    }

    private void handleAmountSelection(Long chatId, AmountType type) {
        if (user.getStatus().equals(Status.AWAITING_SELECT_AMOUNT)) {
            Double amount = user.getCurrentDeal().getCurrentAmount();
            user.setAmountType(type);

            String currentCurrency = "";

            if (type == AmountType.RECEIVE) {
                user.getCurrentDeal().setAmountTo(amount);
                currentCurrency = user.getCurrentDeal().getMoneyTo().getName();
            } else if (type == AmountType.GIVE) {
                user.getCurrentDeal().setAmountFrom(amount);
                currentCurrency = user.getCurrentDeal().getMoneyFrom().getName();
            }

            user.setStatus(Status.AWAITING_EXCHANGE_RATE_TYPE);
            userService.save(user);
            MessageUtils.editMsg(chatId, user.getMessageToEdit(), "Сумма %s %s"
                    .formatted(user.getCurrentDeal().getCurrentAmount(), currentCurrency)
            );
            Message message = MenuService.sendSelectCurrencyType(chatId);
            user.setMessageToEdit(message.getMessageId());
            userService.save(user);
            userService.addMessageToDel(chatId, message.getMessageId());
        }
    }

    private void handleAmountTypeSelection(Long chatId, CurrencyType type) {
        if (user.getStatus().equals(Status.AWAITING_EXCHANGE_RATE_TYPE)) {
            user.setCurrencyType(type);
            user.setStatus(Status.AWAITING_EXCHANGE_RATE);
            MessageUtils.editMsg(chatId, user.getMessageToEdit(), "Формула расчета: %s".formatted(user.getCurrencyType().getText()));
            userService.save(user);
            Message msg = MessageUtils.sendText(chatId, "Введите курс:");
            userService.addMessageToDel(chatId, msg.getMessageId());
        }
    }

}
