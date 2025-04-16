package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.interfaces.BotResponseSender;
import org.example.model.Deal;
import org.example.model.Money;
import org.example.model.User;
import org.example.state.Status;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeProcessor {

    private UserService userService;
    private CurrencyService currencyService;
    private final BotResponseSender responseSender;

    public void approve(long chatId) {
        User user = userService.getUser(chatId);
        Deal deal = user.getCurrentDeal();

        if (deal == null) {
            Message botMsg = responseSender.sendText(chatId, "Сделка не найдена.");
            userService.addMessageToDel(chatId, botMsg.getMessageId());
            return;
        }

        Money from = deal.getMoneyFrom();
        Money to = deal.getMoneyTo();
        double amountTo = deal.getAmountTo();
        double rate = deal.getExchangeRate();

        double fromBalance = currencyService.getBalance(from);
        double toBalance = currencyService.getBalance(to);

        if (deal.getDealType().isBuy()) {
            if (fromBalance < amountTo * rate) {
                Message botMsg = responseSender.sendText(chatId, "Недостаточно средств: " + from);
                userService.addMessageToDel(chatId, botMsg.getMessageId());
                // Сбросим сделку
                user.setCurrentDeal(null);
                user.setStatus(Status.IDLE);
                userService.save(user);
                return;
            }

            double newFromBalance = fromBalance - amountTo * rate;
            double newToBalance = toBalance + amountTo;

            currencyService.updateBalance(from, newFromBalance);
            currencyService.updateBalance(to, newToBalance);

        } else {
            if (toBalance < amountTo) {
                Message botMsg = responseSender.sendText(chatId, "Недостаточно средств: " + to);
                userService.addMessageToDel(chatId, botMsg.getMessageId());
                return;
            }

            double newToBalance = toBalance - amountTo;
            double newFromBalance = fromBalance + (amountTo * rate);

            currencyService.updateBalance(from, newFromBalance);
            currencyService.updateBalance(to, newToBalance);
        }

        user = userService.getUser(chatId);
        responseSender.sendText(chatId, """
                Сделка завершена ✅
                Имя: %s
                Сумма получена: %s %s
                Курс: %s
                Сумма выдана: %s %s
                """.formatted(
                user.getCurrentDeal().getBuyerName(),
                Math.round(user.getCurrentDeal().getAmountTo()), user.getCurrentDeal().getMoneyTo(),
                rate,
                Math.round(user.getCurrentDeal().getAmountFrom()), user.getCurrentDeal().getMoneyFrom()));

        // Сбросим сделку и удалим сообщения
        deleteMsgs(userService.getMessageIdsToDelete(chatId));
        user.setCurrentDeal(null);
        user.setStatus(Status.IDLE);
        user.setMessages(null);
        user.setMessageToEdit(0);
        userService.save(user);
    }

    public void cancel(long chatId) {
        deleteMsgs(userService.getMessageIdsToDelete(chatId));
        User user = userService.getUser(chatId);
        user.setCurrentDeal(null);
        user.setStatus(Status.IDLE);
        user.setMessages(null);
        user.setMessageToEdit(null);
        userService.save(user);
        responseSender.sendText(chatId, "Сделка отменена.");
    }

    private void deleteMsgs(List<Integer> ids) {
        ids.forEach(
                x -> responseSender.deleteMessage(x)
        );
    }

}