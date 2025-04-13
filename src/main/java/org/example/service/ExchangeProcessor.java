package org.example.service;

import org.example.model.Deal;
import org.example.model.Money;
import org.example.model.User;
import org.example.state.Status;
import org.example.util.MessageUtils;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;

public class ExchangeProcessor {

    public static void approve(long chatId) {
        User user = UserService.getUser(chatId);
        Deal deal = user.getCurrentDeal();

        if (deal == null) {
            Message botMsg = MessageUtils.sendText(chatId, "Сделка не найдена.");
            UserService.addMessageToDel(chatId, botMsg.getMessageId());
            return;
        }

        Money from = deal.getMoneyFrom();
        Money to = deal.getMoneyTo();
        double amount = deal.getAmount();
        double rate = deal.getExchangeRate();

        double fromBalance = CurrencyService.getBalance(from);
        double toBalance = CurrencyService.getBalance(to);

        if (deal.getDealType().isBuy()) {
            if (fromBalance < amount * rate) {
                Message botMsg = MessageUtils.sendText(chatId, "Недостаточно средств: " + from);
                UserService.addMessageToDel(chatId, botMsg.getMessageId());
                // Сбросим сделку
                user.setCurrentDeal(null);
                user.setStatus(Status.IDLE);
                UserService.saveOrUpdate(user);
                return;
            }

            double newFromBalance = fromBalance - amount * rate;
            double newToBalance = toBalance + amount;

            CurrencyService.updateBalance(from, newFromBalance);
            CurrencyService.updateBalance(to, newToBalance);

        } else {
            if (toBalance < amount) {
                Message botMsg = MessageUtils.sendText(chatId, "Недостаточно средств: " + to);
                UserService.addMessageToDel(chatId, botMsg.getMessageId());
                return;
            }

            double newToBalance = toBalance - amount;
            double newFromBalance = fromBalance + (amount * rate);

            CurrencyService.updateBalance(from, newFromBalance);
            CurrencyService.updateBalance(to, newToBalance);
        }

        MessageUtils.sendText(chatId, """
                Сделка завершена ✅
                %s -> %s
                Имя: %s
                Покупка/продажа: %s %s
                Курс: %s
                Сумма: %s %s
                """.formatted(from, to,
                user.getCurrentDeal().getBuyerName(),
                amount, to.getName(),
                rate,
                amount * rate, from.getName()));

        // Сбросим сделку и удалим сообщения
        MessageUtils.deleteMessages(chatId);
        user.setCurrentDeal(null);
        user.setStatus(Status.IDLE);
        user.setMessages(null);
        user.setMessageToEdit(null);
        UserService.saveOrUpdate(user);
    }

}