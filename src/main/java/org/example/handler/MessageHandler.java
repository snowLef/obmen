package org.example.handler;

import org.example.constants.BotCommands;
import org.example.model.*;
import org.example.state.Status;
import org.example.service.UserService;
import org.example.util.MessageUtils;
import org.example.ui.MenuService;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.HashMap;
import java.util.Map;

import static org.example.model.DealType.*;
import static org.example.model.Money.*;

public class MessageHandler {

    User user;

    private final Map<String, Runnable> commandMap = new HashMap<>();
    private long chatId;
    private Integer msgId;

    public void handle(Message message) {
        if (message == null || message.getText() == null) return;

        this.chatId = message.getChatId();
        this.msgId = message.getMessageId();
        String text = message.getText();

        user = initUserIfNeeded(chatId);
        Status status = user.getStatus();

        if (commandMap.isEmpty()) initCommandMap();

        System.out.println("[" + chatId + "] Статус: " + status + ", Текст: " + text);

        switch (status) {
            case IDLE -> handleIdleState(text);
            case AWAITING_BUYER_NAME -> handleBuyerName(user, text);
            case AWAITING_DEAL_AMOUNT -> handleDealAmount(text);
            case AWAITING_EXCHANGE_RATE -> handleExchangeRate(user, text);

            case AWAITING_CUSTOM_APPROVE -> {
            }
            case AWAITING_FIRST_CURRENCY -> {
            }
            case AWAITING_SECOND_CURRENCY -> {
            }
            default -> {
            }
        }
    }

    private User initUserIfNeeded(long chatId) {
        User user = UserService.getUser(chatId);
        if (user == null) {
            user = new User(chatId, Status.IDLE);
            UserService.save(user);
        }
        return user;
    }

    private void initCommandMap() {
        commandMap.put("Купить Доллар", () -> start(RUB, USD, BUY));
        commandMap.put("Продать Доллар", () -> start(RUB, USD, SELL));
        commandMap.put("Купить Евро", () -> start(RUB, EUR, BUY));
        commandMap.put("Продать Евро", () -> start(RUB, EUR, SELL));
        commandMap.put("Купить Белый Доллар", () -> start(RUB, USDW, BUY));
        commandMap.put("Продать Белый Доллар", () -> start(RUB, USDW, SELL));
        commandMap.put("Купить Tether", () -> start(RUB, USDT, BUY));
        commandMap.put("Продать Tether", () -> start(RUB, USDT, SELL));

        commandMap.put("Меню", () -> MenuService.sendMainMenu(chatId));
        commandMap.put("/start", () -> MenuService.sendMainMenu(chatId));

        commandMap.put("Сложный обмен", () -> customChange(chatId, msgId));

        commandMap.put("Баланс", () -> MenuService.sendBalance(chatId));
    }

    private void handleIdleState(String text) {
        Runnable command = commandMap.get(text);
        if (command != null) {
            command.run();
        } else {
            MenuService.sendMainMenu(chatId);
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

            UserService.saveOrUpdate(user);
            UserService.addMessageToDel(chatId, msgId);
            MenuService.sendApproveMenu(chatId);
        } else {
            try {
                double rate = Double.parseDouble(text.replace(",", "."));
                user.getCurrentDeal().setAmountFrom((double) Math.round(user.getCurrentDeal().getAmountTo() * rate));
                user.getCurrentDeal().setExchangeRate(rate);
                user.setStatus(Status.AWAITING_APPROVE);
                UserService.saveOrUpdate(user);
                MenuService.sendApproveMenu(chatId);
                UserService.addMessageToDel(chatId, msgId);
            } catch (NumberFormatException e) {
                Message botMsg = MessageUtils.sendText(chatId, "Неверный формат курса.");
                UserService.addMessageToDel(chatId, botMsg.getMessageId());
            }
        }
    }

    private void handleBuyerName(User user, String text) {
        UserService.saveBuyerName(chatId, text);
        if (user.getCurrentDeal().getIsCustom()) {
            UserService.saveUserStatus(chatId, Status.AWAITING_FIRST_CURRENCY);
            UserService.addMessageToDel(chatId, msgId);
            MenuService.sendSelectCurrency(chatId, "Выберите валюту получения");
        } else {
            UserService.saveUserStatus(chatId, Status.AWAITING_DEAL_AMOUNT);
            Message botMsg = MessageUtils.sendText(chatId, "Введите сумму в %s:".formatted(user.getCurrentDeal().getMoneyTo().getName()));
            UserService.addMessageToDel(chatId, botMsg.getMessageId());
            UserService.addMessageToDel(chatId, msgId);
        }
    }

    private void handleDealAmount(String text) {
        try {
            double amount = Double.parseDouble(text);

//            if (user.getAmountType() == AmountType.GIVE) {
//                user.getCurrentDeal().setAmountFrom(amount);
//            } else if (user.getAmountType() == AmountType.RECEIVE) {
//                user.getCurrentDeal().setAmountTo(amount);
//            }

            Message msgToEdit;

            if (user.getCurrentDeal().getIsCustom()) {
                user.setStatus(Status.AWAITING_SELECT_AMOUNT);
//                msgToEdit = MenuService.sendSelectCurrencyType(chatId);
//                user.setMessageToEdit(msgToEdit.getMessageId());
//                UserService.addMessageToDel(chatId, msgToEdit.getMessageId());

                user.getCurrentDeal().setCurrentAmount(amount);
                UserService.saveOrUpdate(user);
                Message msg = MenuService.sendSelectAmountType(chatId);
                user.setMessageToEdit(msg.getMessageId());
                UserService.addMessageToDel(chatId, msg.getMessageId());
            } else {
                user.setStatus(Status.AWAITING_EXCHANGE_RATE);
                Message message = MessageUtils.sendText(chatId, "Введите курс: ");
                UserService.saveOrUpdate(user);
                UserService.addMessageToDel(chatId, message.getMessageId());
            }

            UserService.addMessageToDel(chatId, msgId);
        } catch (NumberFormatException e) {
            Message botMsg = MessageUtils.sendText(chatId, "Неверный формат суммы.");
            UserService.addMessageToDel(chatId, botMsg.getMessageId());
        }
    }

    private void start(Money from, Money to, DealType dealType) {
        UserService.startDeal(chatId, from, to, dealType);
        UserService.saveUserStatus(chatId, Status.AWAITING_BUYER_NAME);
        UserService.addMessageToDel(chatId, msgId);
        Message botMsg = MessageUtils.sendText(chatId, BotCommands.ASK_FOR_NAME);
        UserService.addMessageToDel(chatId, botMsg.getMessageId());
    }

    private void customChange(Long chatId, Integer msgId) {
        UserService.saveUserStatus(chatId, Status.AWAITING_BUYER_NAME);
        Deal deal = new Deal();
        deal.setIsCustom(true);
        deal.setDealType(BUY);
        UserService.saveUserCurrentDeal(chatId, deal);
        UserService.addMessageToDel(chatId, msgId);
        Message botMsg = MessageUtils.sendText(chatId, BotCommands.ASK_FOR_NAME);
        UserService.addMessageToDel(chatId, botMsg.getMessageId());
    }

}
