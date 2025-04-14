package org.example.service;

import org.example.model.Deal;
import org.example.model.Money;
import org.example.model.User;
import org.example.state.Status;
import org.example.util.MessageUtils;
import org.telegram.telegrambots.meta.api.objects.Message;

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
        double amountTo = deal.getAmountTo();
        double rate = deal.getExchangeRate();

        double fromBalance = CurrencyService.getBalance(from);
        double toBalance = CurrencyService.getBalance(to);

        if (deal.getDealType().isBuy()) {
            if (fromBalance < amountTo * rate) {
                Message botMsg = MessageUtils.sendText(chatId, "Недостаточно средств: " + from);
                UserService.addMessageToDel(chatId, botMsg.getMessageId());
                // Сбросим сделку
                user.setCurrentDeal(null);
                user.setStatus(Status.IDLE);
                UserService.saveOrUpdate(user);
                return;
            }

            double newFromBalance = fromBalance - amountTo * rate;
            double newToBalance = toBalance + amountTo;

            CurrencyService.updateBalance(from, newFromBalance);
            CurrencyService.updateBalance(to, newToBalance);

        } else {
            if (toBalance < amountTo) {
                Message botMsg = MessageUtils.sendText(chatId, "Недостаточно средств: " + to);
                UserService.addMessageToDel(chatId, botMsg.getMessageId());
                return;
            }

            double newToBalance = toBalance - amountTo;
            double newFromBalance = fromBalance + (amountTo * rate);

            CurrencyService.updateBalance(from, newFromBalance);
            CurrencyService.updateBalance(to, newToBalance);
        }

        user = UserService.getUser(chatId);
        MessageUtils.sendText(chatId, """
                Сделка завершена ✅
                %s -> %s
                Имя: %s
                Сумма получена: %s %s
                Курс: %s
                Сумма выдана: %s %s
                """.formatted(
                from, to,
                user.getCurrentDeal().getBuyerName(),
                Math.round(user.getCurrentDeal().getAmountTo()), user.getCurrentDeal().getMoneyTo(),
                rate,
                Math.round(user.getCurrentDeal().getAmountFrom()), user.getCurrentDeal().getMoneyFrom()));

        // Сбросим сделку и удалим сообщения
        MessageUtils.deleteMessages(chatId);
        user.setCurrentDeal(null);
        user.setStatus(Status.IDLE);
        user.setMessages(null);
        user.setMessageToEdit(0);
        UserService.saveOrUpdate(user);
    }

    public static void cancel(long chatId) {
        User user = UserService.getUser(chatId);
        UserService.saveUserStatus(chatId, Status.IDLE);
        UserService.saveUserCurrentDeal(chatId, null);
        user.setCurrentDeal(null);
        user.setStatus(Status.IDLE);
        user.setMessages(null);
        user.setMessageToEdit(null);
        UserService.saveOrUpdate(user);
        MessageUtils.sendText(chatId, "Сделка отменена.");
        MessageUtils.deleteMessages(chatId);
    }

}