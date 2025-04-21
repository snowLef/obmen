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
            case MOVING_BALANCE -> processMovingBalance(chatId, user, deal);
            case TRANSPOSITION, INVOICE -> processTranspositionOrInvoiceDeal(chatId, user, deal);
        }

    }

    private void processMovingBalance(long chatId, User user, Deal deal) {
        BalanceType balanceTypeFrom = user.getBalanceFrom();
        BalanceType balanceTypeTo = user.getBalanceTo();
        long toBalance = currencyService.getBalance(deal.getMoneyTo().get(0).getCurrency(), balanceTypeTo);
        long fromBalance = currencyService.getBalance(deal.getMoneyTo().get(0).getCurrency(), balanceTypeFrom);

        currencyService.updateBalance(deal.getMoneyTo().get(0).getCurrency(), balanceTypeFrom, fromBalance - deal.getAmountTo());
        currencyService.updateBalance(deal.getMoneyTo().get(0).getCurrency(), balanceTypeTo, toBalance + deal.getAmountTo());

        menuService.sendBalanceMovedMessage(chatId);
        deleteMsgs(chatId, userService.getMessageIdsToDeleteWithInit(chatId));
        userService.resetUserState(user);
    }

    private void processTranspositionOrInvoiceDeal(long chatId, User user, Deal deal) {
        List<CurrencyAmount> moneyTo = user.getCurrentDeal().getMoneyTo();

        moneyTo
                .forEach(e -> {
                    Money money = e.getCurrency();
                    long currentBalance = currencyService.getBalance(money, BalanceType.OWN);
                    long newBalance = currentBalance + e.getAmount();
                    currencyService.updateBalance(money, BalanceType.OWN, newBalance);
                });

        List<CurrencyAmount> moneyFrom = user.getCurrentDeal().getMoneyFrom();

        moneyFrom
                .forEach(e -> {
                    Money money = e.getCurrency();
                    long currentBalance = currencyService.getBalance(money, BalanceType.OWN);
                    long newBalance = currentBalance - e.getAmount();
                    currencyService.updateBalance(money, BalanceType.OWN, newBalance);
                });
        menuService.sendTranspositionOrInvoiceComplete(chatId);
        deleteMsgs(chatId, userService.getMessageIdsToDeleteWithInit(chatId));
        userService.resetUserState(user);
    }

    public void cancel(long chatId) {
        deleteMsgs(chatId, userService.getMessageIdsToDeleteWithInit(chatId));
        User user = userService.getUser(chatId);
        userService.resetUserState(user);
        telegramSender.sendText(chatId, "Сделка отменена.");
    }

    private void deleteMsgs(long chatId, List<Integer> ids) {
        ids.forEach(
                x -> telegramSender.deleteMessage(chatId, x)
        );
    }

    private long getOwnBalance(Money currency) {
        return currencyService.getBalance(currency, BalanceType.OWN);
    }

    private void updateOwnBalance(Money currency, long newBalance) {
        currencyService.updateBalance(currency, BalanceType.OWN, newBalance);
    }

    private long calculateAmountWithRate(long amount, double rate) {
        return Math.round(amount * rate);
    }

    private void sendInsufficientFundsMessage(long chatId, Money currency) {
        Message botMsg = telegramSender.sendText(chatId, "Недостаточно средств: " + currency);
        deleteMsgs(chatId, userService.getMessageIdsToDeleteWithInit(chatId));
        userService.resetUserState(userService.getUser(chatId));
    }

    private void processBuy(long chatId, User user, Deal deal) {
        long fromBalance = getOwnBalance(deal.getMoneyFrom().get(0).getCurrency());
        long toBalance = getOwnBalance(deal.getMoneyTo().get(0).getCurrency());
        long required = calculateAmountWithRate(deal.getAmountTo(), deal.getExchangeRate());

        if (fromBalance < required) {
            sendInsufficientFundsMessage(chatId, deal.getMoneyFrom().get(0).getCurrency());
            userService.resetUserState(user);
            return;
        }

        updateOwnBalance(deal.getMoneyFrom().get(0).getCurrency(), fromBalance - required);
        updateOwnBalance(deal.getMoneyTo().get(0).getCurrency(), toBalance + deal.getAmountTo());
        menuService.sendDealCompletedMessage(chatId);
        deleteMsgs(chatId, userService.getMessageIdsToDeleteWithInit(chatId));
        userService.resetUserState(user);
    }

    private void processSell(long chatId, User user, Deal deal) {
        long fromBalance = getOwnBalance(deal.getMoneyFrom().get(0).getCurrency());
        if (fromBalance < deal.getAmountFrom()) {
            sendInsufficientFundsMessage(chatId, deal.getMoneyFrom().get(0).getCurrency());
            userService.resetUserState(user);
            return;
        }

        long toBalance = getOwnBalance(deal.getMoneyTo().get(0).getCurrency());
        updateOwnBalance(deal.getMoneyFrom().get(0).getCurrency(), fromBalance - deal.getAmountFrom());
        updateOwnBalance(deal.getMoneyTo().get(0).getCurrency(), toBalance + deal.getAmountTo());
        menuService.sendDealCompletedMessage(chatId);
        deleteMsgs(chatId, userService.getMessageIdsToDeleteWithInit(chatId));
        userService.resetUserState(user);
    }

    private void processChangeBalance(long chatId, User user, Deal deal) {
        long toForeignBalance = currencyService.getBalance(deal.getMoneyTo().get(0).getCurrency(), BalanceType.FOREIGN);
        ChangeBalanceType type = user.getChangeBalanceType();

        switch (type) {
            case GET ->
                    currencyService.updateBalance(deal.getMoneyTo().get(0).getCurrency(), BalanceType.FOREIGN, toForeignBalance + deal.getAmountTo());
            case GIVE ->
                    currencyService.updateBalance(deal.getMoneyTo().get(0).getCurrency(), BalanceType.FOREIGN, toForeignBalance - deal.getAmountTo());
            case LEND ->
                    currencyService.moveBalance(deal.getMoneyTo().get(0).getCurrency(), BalanceType.OWN, BalanceType.DEBT, deal.getAmountTo());
            case DEBT_REPAYMENT ->
                    currencyService.moveBalance(deal.getMoneyTo().get(0).getCurrency(), BalanceType.DEBT, BalanceType.OWN, deal.getAmountTo());
        }

        menuService.sendBalanceChangedMessage(chatId);
        deleteMsgs(chatId, userService.getMessageIdsToDeleteWithInit(chatId));
        userService.resetUserState(user);
    }
}