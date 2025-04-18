package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.infra.TelegramSender;
import org.example.model.*;
import org.example.model.enums.BalanceType;
import org.example.model.enums.ChangeBalanceType;
import org.example.model.enums.Money;
import org.example.model.enums.Status;
import org.example.ui.MenuService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeProcessor {

    private final UserService userService;
    private final CurrencyService currencyService;
    private final TelegramSender telegramSender;
    private final MenuService menuService;

    public void approve(long chatId) {
        User user = userService.getUser(chatId);
        Deal deal = user.getCurrentDeal();

        if (deal == null) {
            Message botMsg = telegramSender.sendText(chatId, "Сделка не найдена.");
            userService.addMessageToDel(chatId, botMsg.getMessageId());
            return;
        }

        switch (deal.getDealType()) {
            case BUY, CUSTOM -> processBuy(chatId, user, deal);
            case SELL -> processSell(chatId, user, deal);
            case CHANGE_BALANCE -> processChangeBalance(chatId, user, deal);
        }

    }

    public void cancel(long chatId) {
        deleteMsgs(chatId, userService.getMessageIdsToDeleteWithInit(chatId));
        User user = userService.getUser(chatId);
        resetUserState(user);
        telegramSender.sendText(chatId, "Сделка отменена.");
    }

    private void deleteMsgs(long chatId, List<Integer> ids) {
        ids.forEach(
                x -> telegramSender.deleteMessage(chatId, x)
        );
    }

    private void resetUserState(User user) {

        user.setCurrentDeal(null);
        user.setStatus(Status.IDLE);
        user.setMessages(null);
        user.setMessageToEdit(null);
        userService.save(user);
    }


    private double getOwnBalance(Money currency) {
        return currencyService.getBalance(currency, BalanceType.OWN);
    }

    private void updateOwnBalance(Money currency, double newBalance) {
        currencyService.updateBalance(currency, BalanceType.OWN, newBalance);
    }

    private double calculateAmountWithRate(double amount, double rate) {
        return amount * rate;
    }

    private void sendInsufficientFundsMessage(long chatId, Money currency) {
        Message botMsg = telegramSender.sendText(chatId, "Недостаточно средств: " + currency);
        userService.addMessageToDel(chatId, botMsg.getMessageId());
        deleteMsgs(chatId, userService.getMessageIdsToDeleteWithInit(chatId));
        resetUserState(userService.getUser(chatId));
    }

    private void processBuy(long chatId, User user, Deal deal) {
        double fromBalance = getOwnBalance(deal.getMoneyFrom());
        double toBalance = getOwnBalance(deal.getMoneyTo());
        double required = calculateAmountWithRate(deal.getAmountTo(), deal.getExchangeRate());

        if (fromBalance < required) {
            sendInsufficientFundsMessage(chatId, deal.getMoneyFrom());
            resetUserState(user);
            return;
        }

        updateOwnBalance(deal.getMoneyFrom(), fromBalance - required);
        updateOwnBalance(deal.getMoneyTo(), toBalance + deal.getAmountTo());
        menuService.sendDealCompletedMessage(chatId);
        deleteMsgs(chatId, userService.getMessageIdsToDeleteWithInit(chatId));
        resetUserState(user);
    }

    private void processSell(long chatId, User user, Deal deal) {
        double fromBalance = getOwnBalance(deal.getMoneyFrom());
        if (fromBalance < deal.getAmountFrom()) {
            sendInsufficientFundsMessage(chatId, deal.getMoneyFrom());
            resetUserState(user);
            return;
        }

        double toBalance = getOwnBalance(deal.getMoneyTo());
        updateOwnBalance(deal.getMoneyFrom(), fromBalance - deal.getAmountFrom());
        updateOwnBalance(deal.getMoneyTo(), toBalance + deal.getAmountTo());
        menuService.sendDealCompletedMessage(chatId);
        deleteMsgs(chatId, userService.getMessageIdsToDeleteWithInit(chatId));
        resetUserState(user);
    }

    private void processChangeBalance(long chatId, User user, Deal deal) {
        double toForeignBalance = currencyService.getBalance(deal.getMoneyTo(), BalanceType.FOREIGN);
        ChangeBalanceType type = user.getChangeBalanceType();

        switch (type) {
            case GET ->
                    currencyService.updateBalance(deal.getMoneyTo(), BalanceType.FOREIGN, toForeignBalance + deal.getAmountTo());
            case GIVE ->
                    currencyService.updateBalance(deal.getMoneyTo(), BalanceType.FOREIGN, toForeignBalance - deal.getAmountTo());
            case LEND ->
                    currencyService.moveBalance(deal.getMoneyTo(), BalanceType.OWN, BalanceType.DEBT, deal.getAmountTo());
            case DEBT_REPAYMENT ->
                    currencyService.moveBalance(deal.getMoneyTo(), BalanceType.DEBT, BalanceType.OWN, deal.getAmountTo());
        }

        menuService.sendBalanceChangedMessage(chatId);
        deleteMsgs(chatId, userService.getMessageIdsToDeleteWithInit(chatId));
        resetUserState(user);
    }
}