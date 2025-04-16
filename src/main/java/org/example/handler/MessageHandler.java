package org.example.handler;

import lombok.RequiredArgsConstructor;
import org.example.constants.BotCommands;
import org.example.model.*;
import org.example.service.MessageSender;
import org.example.service.UserService;
import org.example.state.Status;
import org.example.ui.MenuService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.HashMap;
import java.util.Map;

import static org.example.model.DealType.*;
import static org.example.model.Money.*;

@Component
@RequiredArgsConstructor
public class MessageHandler {
    private final UserService userService;
    private final MessageSender messageSender;
    private final MenuService menuService;

    User user;
    private final Map<String, Runnable> commandMap = new HashMap<>();
    private long chatId;
    private Integer msgId;

    public void process(Message message) {
        if (message == null || message.getText() == null) return;

        this.chatId = message.getChatId();
        this.msgId = message.getMessageId();
        String text = message.getText();

        user = userService.getOrCreate(chatId);
        Status status = user.getStatus();

        if (commandMap.isEmpty()) initCommandMap();

        System.out.println("[" + chatId + "] Статус: " + status + ", Текст: " + text);

        switch (status) {
            case IDLE -> handleIdleState(text);
            case AWAITING_BUYER_NAME -> handleBuyerName(user, text);
            case AWAITING_DEAL_AMOUNT -> handleDealAmount(text);
            case AWAITING_EXCHANGE_RATE -> handleExchangeRate(user, text);

            default -> {
            }
        }
    }

    private void initCommandMap() {
        commandMap.put("Купить Доллар", () -> start(RUB, USD, BUY));
        commandMap.put("Продать Доллар", () -> start(USD, RUB, SELL));
        commandMap.put("Купить Евро", () -> start(RUB, EUR, BUY));
        commandMap.put("Продать Евро", () -> start(EUR, RUB, SELL));
        commandMap.put("Купить Белый Доллар", () -> start(RUB, USDW, BUY));
        commandMap.put("Продать Белый Доллар", () -> start(USDW, RUB, SELL));
        commandMap.put("Купить Tether", () -> start(RUB, USDT, BUY));
        commandMap.put("Продать Tether", () -> start(USDT, RUB, SELL));

        commandMap.put("Меню", () -> menuService.sendMainMenu(chatId));
        commandMap.put("/start", () -> menuService.sendMainMenu(chatId));

        commandMap.put("Сложный обмен", () -> customChange(chatId, msgId));

        commandMap.put("Баланс", () -> menuService.sendBalance(chatId));
    }

    private void handleIdleState(String text) {
        Runnable command = commandMap.get(text);
        if (command != null) {
            command.run();
        } else {
            menuService.sendMainMenu(chatId);
        }
    }

    private void handleExchangeRate(User user, String text) {
        if (user.getCurrentDeal().getIsCustom()) {
            double rate = Double.parseDouble(text.replace(",", "."));
            user.getCurrentDeal().setExchangeRate(rate);
            user.setStatus(Status.AWAITING_APPROVE);

            Double amountTo = user.getCurrentDeal().getAmountTo();
            Double amountFrom = user.getCurrentDeal().getAmountFrom();

            if (user.getAmountType() == AmountType.GIVE) {
                if (user.getCurrencyType() == CurrencyType.DIVISION) {
                    user.getCurrentDeal().setAmountTo(amountFrom / rate);
                } else if (user.getCurrencyType() == CurrencyType.MULTIPLICATION) {
                    user.getCurrentDeal().setAmountTo(amountFrom * rate);
                }
            } else if (user.getAmountType() == AmountType.RECEIVE) {
                if (user.getCurrencyType() == CurrencyType.DIVISION) {
                    user.getCurrentDeal().setAmountFrom(amountTo / rate);
                } else if (user.getCurrencyType() == CurrencyType.MULTIPLICATION) {
                    user.getCurrentDeal().setAmountFrom(amountTo * rate);
                }
            }

            userService.save(user);
            userService.addMessageToDel(chatId, msgId);
            menuService.sendApproveMenu(chatId);
        } else {
            try {
                double rate = Double.parseDouble(text.replace(",", "."));
                user.getCurrentDeal().setAmountFrom((double) Math.round(user.getCurrentDeal().getAmountTo() * rate));
                user.getCurrentDeal().setExchangeRate(rate);
                user.setStatus(Status.AWAITING_APPROVE);
                userService.save(user);
                menuService.sendApproveMenu(chatId);
                userService.addMessageToDel(chatId, msgId);
            } catch (NumberFormatException e) {
                Message botMsg = messageSender.sendText(chatId, "Неверный формат курса.");
                userService.addMessageToDel(chatId, botMsg.getMessageId());
            }
        }
    }

    private void handleBuyerName(User user, String text) {
        userService.saveBuyerName(chatId, text);
        if (user.getCurrentDeal().getIsCustom()) {
            userService.saveUserStatus(chatId, Status.AWAITING_FIRST_CURRENCY);
            userService.addMessageToDel(chatId, msgId);
            menuService.sendSelectCurrency(chatId, "Выберите валюту получения");
        } else {
            userService.saveUserStatus(chatId, Status.AWAITING_DEAL_AMOUNT);
            Message botMsg = messageSender.sendText(chatId, "Введите сумму в %s:".formatted(user.getCurrentDeal().getMoneyTo().getName()));
            userService.addMessageToDel(chatId, botMsg.getMessageId());
            userService.addMessageToDel(chatId, msgId);
        }
    }

    private void handleDealAmount(String text) {
        try {
            double amount = Double.parseDouble(text);

            if (user.getCurrentDeal().getIsCustom()) {
                user.setStatus(Status.AWAITING_SELECT_AMOUNT);
                user.getCurrentDeal().setCurrentAmount(amount);
                userService.save(user);
                Message msg = menuService.sendSelectAmountType(chatId);
                user.setMessageToEdit(msg.getMessageId());
                userService.save(user);
                userService.addMessageToDel(chatId, msg.getMessageId());
            } else {
                user.setStatus(Status.AWAITING_EXCHANGE_RATE);
                user.getCurrentDeal().setAmountTo(amount);
                Message message = messageSender.sendText(chatId, "Введите курс: ");
                userService.save(user);
                userService.addMessageToDel(chatId, message.getMessageId());
            }

            userService.addMessageToDel(chatId, msgId);
        } catch (NumberFormatException e) {
            Message botMsg = messageSender.sendText(chatId, "Неверный формат суммы.");
            userService.addMessageToDel(chatId, botMsg.getMessageId());
        }
    }

    private void start(Money from, Money to, DealType dealType) {
        userService.startDeal(chatId, from, to, dealType);
        userService.saveUserStatus(chatId, Status.AWAITING_BUYER_NAME);
        userService.addMessageToDel(chatId, msgId);
        Message botMsg = messageSender.sendText(chatId, BotCommands.ASK_FOR_NAME);
        userService.addMessageToDel(chatId, botMsg.getMessageId());
    }

    private void customChange(Long chatId, Integer msgId) {
        userService.saveUserStatus(chatId, Status.AWAITING_BUYER_NAME);
        Deal deal = new Deal();
        deal.setIsCustom(true);
        deal.setDealType(BUY);
        userService.saveUserCurrentDeal(chatId, deal);
        userService.addMessageToDel(chatId, msgId);
        Message botMsg = messageSender.sendText(chatId, BotCommands.ASK_FOR_NAME);
        userService.addMessageToDel(chatId, botMsg.getMessageId());
    }
}
