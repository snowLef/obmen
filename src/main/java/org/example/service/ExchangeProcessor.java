package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.infra.TelegramSender;
import org.example.model.*;
import org.example.model.enums.BalanceType;
import org.example.model.enums.ChangeBalanceType;
import org.example.model.enums.PlusMinusType;
import org.example.model.enums.Money;
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
            case BUY -> processBuy(chatId, user, deal);
            case CUSTOM -> processCustom(chatId, user, deal);
            case SELL -> processSell(chatId, user, deal);
            case PLUS_MINUS -> processPlusMinusBalance(chatId, user, deal);
            case MOVING_BALANCE -> processMovingBalance(chatId, user, deal);
            case CHANGE_BALANCE -> processChangeBalance(chatId, user, deal);
            case TRANSPOSITION, INVOICE -> processTranspositionOrInvoiceDeal(chatId, user, deal);
        }
        menuService.sendBalance(chatId);
        menuService.sendMainMenu(chatId);
    }

    private void processChangeBalance(long chatId, User user, Deal deal) {
        if (user.getChangeBalanceType() == ChangeBalanceType.ADD) {
            BalanceType balanceTypeTo = BalanceType.OWN;
            long toBalance = currencyService.getBalance(deal.getMoneyTo().get(0).getCurrency(), balanceTypeTo);
            currencyService.updateBalance(deal.getMoneyTo().get(0).getCurrency(), balanceTypeTo, toBalance + deal.getAmountTo());
        } else if (user.getChangeBalanceType() == ChangeBalanceType.WITHDRAWAL) {
            BalanceType balanceTypeFrom = BalanceType.OWN;
            long fromBalance = currencyService.getBalance(deal.getMoneyFrom().get(0).getCurrency(), balanceTypeFrom);
            currencyService.updateBalance(deal.getMoneyFrom().get(0).getCurrency(), balanceTypeFrom, fromBalance - deal.getAmountFrom());
        }
        menuService.sendChangedBalanceMessage(chatId);
        deleteMsgs(chatId, userService.getMessageIdsToDeleteWithInit(chatId));
        userService.resetUserState(user);
    }

    private void processMovingBalance(long chatId, User user, Deal deal) {
        BalanceType balanceTypeFrom = user.getBalanceFrom();
        BalanceType balanceTypeTo = user.getBalanceTo();
        long toBalance = currencyService.getBalance(deal.getMoneyTo().get(0).getCurrency(), balanceTypeTo);
        long fromBalance = currencyService.getBalance(deal.getMoneyTo().get(0).getCurrency(), balanceTypeFrom);

        if (fromBalance < deal.getAmountTo()) {
            telegramSender.sendText(chatId, "Недостаточно средств: *" + BalanceType.FOREIGN.getDisplayName().toUpperCase() + "*");
        } else {
            currencyService.updateBalance(deal.getMoneyTo().get(0).getCurrency(), balanceTypeFrom, fromBalance - deal.getAmountTo());
            currencyService.updateBalance(deal.getMoneyTo().get(0).getCurrency(), balanceTypeTo, toBalance + deal.getAmountTo());
            menuService.sendBalanceMovedMessage(chatId);
        }

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

    public void deleteMsgs(long chatId, List<Integer> ids) {
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
        telegramSender.sendText(chatId, "Недостаточно средств: " + currency);
        deleteMsgs(chatId, userService.getMessageIdsToDeleteWithInit(chatId));
        userService.resetUserState(userService.getUser(chatId));
        menuService.sendMainMenu(chatId);
    }

    private void processCustom(long chatId, User user, Deal deal) {
        long fromBalance = getOwnBalance(deal.getMoneyFrom().get(0).getCurrency());
        long toBalance = getOwnBalance(deal.getMoneyTo().get(0).getCurrency());

        updateOwnBalance(deal.getMoneyFrom().get(0).getCurrency(), fromBalance - deal.getAmountFrom());
        updateOwnBalance(deal.getMoneyTo().get(0).getCurrency(), toBalance + deal.getAmountTo());
        menuService.sendDealCompletedMessage(chatId);
        deleteMsgs(chatId, userService.getMessageIdsToDeleteWithInit(chatId));
        userService.resetUserState(user);
    }

    private void processBuy(long chatId, User user, Deal deal) {
        long fromBalance = getOwnBalance(deal.getMoneyFrom().get(0).getCurrency());
        long toBalance = getOwnBalance(deal.getMoneyTo().get(0).getCurrency());

        updateOwnBalance(deal.getMoneyFrom().get(0).getCurrency(), fromBalance - deal.getAmountFrom());
        updateOwnBalance(deal.getMoneyTo().get(0).getCurrency(), toBalance + deal.getAmountTo());
        menuService.sendDealCompletedMessage(chatId);
        deleteMsgs(chatId, userService.getMessageIdsToDeleteWithInit(chatId));
        userService.resetUserState(user);
    }

    private void processSell(long chatId, User user, Deal deal) {
        long fromBalance = getOwnBalance(deal.getMoneyFrom().get(0).getCurrency());
        long toBalance = getOwnBalance(deal.getMoneyTo().get(0).getCurrency());
        updateOwnBalance(deal.getMoneyFrom().get(0).getCurrency(), fromBalance - deal.getAmountFrom());
        updateOwnBalance(deal.getMoneyTo().get(0).getCurrency(), toBalance + deal.getAmountTo());
        menuService.sendDealCompletedMessage(chatId);
        deleteMsgs(chatId, userService.getMessageIdsToDeleteWithInit(chatId));
        userService.resetUserState(user);
    }

    private void processPlusMinusBalance(long chatId, User user, Deal deal) {
        PlusMinusType type = user.getPlusMinusType();

        switch (type) {
            case GET -> deal.getMoneyToList()
                    .forEach(e -> {
                        long balance = currencyService.getBalance(e, BalanceType.FOREIGN);
                        currencyService.updateBalance(e, BalanceType.FOREIGN, balance + getCurrentAmountTo(deal, e));
                    });
            case GIVE -> {
                boolean hasSufficientBalance = deal.getMoneyFrom().stream()
                        .allMatch(amount ->
                                amount.getAmount() <= currencyService.getBalance(
                                        amount.getCurrency(),
                                        BalanceType.FOREIGN
                                )
                        );
                if (hasSufficientBalance) {
                    deal.getMoneyFromList()
                            .forEach(e -> {
                                long balance = currencyService.getBalance(e, BalanceType.FOREIGN);
                                currencyService.updateBalance(e, BalanceType.FOREIGN, balance - getCurrentAmountFrom(deal, e));
                            });
                } else {
                    telegramSender.sendText(chatId, "Недостаточно средств: *" + BalanceType.FOREIGN.getDisplayName().toUpperCase() + "*");
                    deleteMsgs(chatId, userService.getMessageIdsToDeleteWithInit(chatId));
                    userService.resetUserState(user);
                    return;
                }
            }
            case LEND -> deal.getMoneyFromList()
                    .forEach(e -> currencyService.moveBalance(chatId, e, BalanceType.OWN, BalanceType.DEBT, getCurrentAmountFrom(deal, e)));
            case DEBT_REPAYMENT -> {
                boolean hasSufficientBalance = deal.getMoneyTo().stream()
                        .allMatch(amount ->
                                amount.getAmount() <= currencyService.getBalance(
                                        amount.getCurrency(),
                                        BalanceType.DEBT
                                )
                        );
                if (hasSufficientBalance) {
                    deal.getMoneyToList()
                            .forEach(e -> currencyService.moveBalance(chatId, e, BalanceType.DEBT, BalanceType.OWN, getCurrentAmountTo(deal, e)));
                } else {
                    telegramSender.sendText(chatId, "Недостаточно средств: *" + BalanceType.DEBT.getDisplayName().toUpperCase() + "*");
                    deleteMsgs(chatId, userService.getMessageIdsToDeleteWithInit(chatId));
                    userService.resetUserState(user);
                    return;
                }
            }
        }

        menuService.sendBalancePlusMinusMessage(chatId);
        deleteMsgs(chatId, userService.getMessageIdsToDeleteWithInit(chatId));
        userService.resetUserState(user);
    }

    private long getCurrentAmountTo(Deal deal, Money money) {
        return deal.getMoneyTo().stream()
                .filter(x -> x.getCurrency().equals(money))
                .findFirst()
                .get()
                .getAmount();
    }

    private long getCurrentAmountFrom(Deal deal, Money money) {
        return deal.getMoneyFrom().stream()
                .filter(x -> x.getCurrency().equals(money))
                .findFirst()
                .get()
                .getAmount();
    }
}