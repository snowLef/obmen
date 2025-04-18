package org.example.handler;

import org.example.constants.BotCommands;
import org.example.model.*;
import org.example.model.enums.AmountType;
import org.example.model.enums.CurrencyType;
import org.example.model.enums.DealType;
import org.example.model.enums.Money;
import org.example.service.DealService;
import org.example.infra.TelegramSender;
import org.example.service.UserService;
import org.example.service.ExchangeProcessor;
import org.example.model.enums.Status;
import org.example.ui.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import static org.example.model.enums.Money.*;

@Component
public class CallbackHandler {

    private User user;
    private UserService userService;
    private TelegramSender telegramSender;
    private ExchangeProcessor exchangeProcessor;
    private MenuService menuService;
    private DealService dealService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
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
    public void setMenuService(MenuService menuService) {
        this.menuService = menuService;
    }

    @Autowired
    public void setDealService(DealService dealService) {
        this.dealService = dealService;
    }

    public void process(CallbackQuery callbackQuery) {
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

            case BotCommands.BALANCE -> menuService.sendBalance(chatId);

            case "yes" -> {
                exchangeProcessor.approve(chatId);
                menuService.sendMainMenu(chatId);
            }
            case "no" -> {
                exchangeProcessor.cancel(chatId);
                menuService.sendMainMenu(chatId);
            }

            case "give" -> handleAmountSelection(chatId, AmountType.GIVE);
            case "receive" -> handleAmountSelection(chatId, AmountType.RECEIVE);

            case "division" -> handleAmountTypeSelection(chatId, CurrencyType.DIVISION);
            case "multiplication" -> handleAmountTypeSelection(chatId, CurrencyType.MULTIPLICATION);

            default -> telegramSender.sendText(chatId, "Неизвестная команда.");
        }
    }

    private void handleCurrencySelection(Long chatId, Money money) {
        Deal deal = user.getCurrentDeal();
        if (user.getStatus() == Status.AWAITING_FIRST_CURRENCY) {
            deal.setMoneyTo(money);
            if (deal.getDealType() == DealType.CHANGE_BALANCE) {
                userService.saveUserStatus(chatId, Status.AWAITING_DEAL_AMOUNT);
                telegramSender.sendText(chatId, "Введите сумму:");
            } else {
                menuService.sendSelectCurrency(chatId, "Выберите валюту выдачи");
                userService.saveUserStatus(chatId, Status.AWAITING_SECOND_CURRENCY);
            }
            telegramSender.editMsg(chatId, user.getMessageToEdit(), "Получение: " + deal.getMoneyTo().getName());
        } else if (user.getStatus() == Status.AWAITING_SECOND_CURRENCY) {
            deal.setMoneyFrom(money);
            telegramSender.editMsg(chatId, user.getMessageToEdit(), "Выдача: " + deal.getMoneyFrom().getName());
            Message message = telegramSender.sendText(chatId, "Введите сумму: ");
            user.setStatus(Status.AWAITING_DEAL_AMOUNT);
            userService.save(user);
            userService.addMessageToDel(chatId, message.getMessageId());
        }
        dealService.save(deal);
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
            telegramSender.editMsg(chatId, user.getMessageToEdit(), "Сумма %s %s"
                    .formatted(user.getCurrentDeal().getCurrentAmount(), currentCurrency)
            );
            Message message = menuService.sendSelectCurrencyType(chatId);
            user.setMessageToEdit(message.getMessageId());
            userService.save(user);
            userService.addMessageToDel(chatId, message.getMessageId());
        }
    }

    private void handleAmountTypeSelection(Long chatId, CurrencyType type) {
        if (user.getStatus().equals(Status.AWAITING_EXCHANGE_RATE_TYPE)) {
            user.setCurrencyType(type);
            user.setStatus(Status.AWAITING_EXCHANGE_RATE);
            telegramSender.editMsg(chatId, user.getMessageToEdit(), "Формула расчета: %s".formatted(user.getCurrencyType().getText()));
            userService.save(user);
            Message msg = telegramSender.sendText(chatId, "Введите курс:");
            userService.addMessageToDel(chatId, msg.getMessageId());
        }
    }
}
