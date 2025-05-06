package org.example.handler.callback;

import lombok.RequiredArgsConstructor;
import org.example.constants.BotCommands;
import org.example.model.CurrencyAmount;
import org.example.model.Deal;
import org.example.model.User;
import org.example.model.enums.*;
import org.example.service.DealService;
import org.example.service.ExchangeProcessor;
import org.example.service.UserService;
import org.example.ui.MenuService;
import org.example.infra.TelegramSender;
import org.example.util.MessageUtils;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CurrencySelectCallbackHandler implements CallbackCommandHandler {

    private final TelegramSender telegramSender;
    private final UserService userService;
    private final MenuService menuService;
    private final DealService dealService;
    private final ExchangeProcessor exchangeProcessor;

    @Override
    public boolean supports(String data) {
        if ("done".equals(data)) return true;
        return Arrays.stream(Money.values())
                .anyMatch(m -> m.getName().equalsIgnoreCase(data));
    }

    public void handle(CallbackQuery query, User user) {
        Long chatId = query.getMessage().getChatId();
        String data = query.getData();
        Deal deal = user.getCurrentDeal();

        if (!(query.getMessage() instanceof Message)) {
            telegramSender.sendText(chatId, "Не удалось обработать сообщение.");
            return;
        }

        if (deal.getDealType() == DealType.CUSTOM) {
            handleCustomDeal(query, user, deal, chatId);
        } else if (deal.getDealType() == DealType.MOVING_BALANCE) {
            handleChangeBalance(data, user, deal, chatId);
        } else if (deal.getDealType() == DealType.CHANGE_BALANCE) {
            handleChangeBalance(data, user, deal, chatId);
        } else if (deal.getDealType() == DealType.PLUS_MINUS) {
            handlePlusMinus(data, user, deal, chatId);
        } else {
            handleMultiSelect(data, user, deal, chatId);
        }
    }

    private void handlePlusMinus(String data, User user, Deal deal, Long chatId) {
        if (data.equals("done")) {
            handleDonePlusMinus(user, deal, chatId);
            return;
        }

        Money selected = Money.valueOfName(data);

        boolean isSelectingTo = List.of(PlusMinusType.GET, PlusMinusType.DEBT_REPAYMENT).contains(deal.getPlusMinusType());

        toggleCurrencySelection(deal, selected, isSelectingTo);

        List<Money> selectedList = isSelectingTo ? deal.getMoneyToList() : deal.getMoneyFromList();
        String selectedNames = selectedList.stream()
                .filter(Objects::nonNull)
                .map(Money::getName)
                .toList()
                .toString();
        String text = isSelectingTo ? "Получено " : "Выдано ";
        telegramSender.editMsgWithKeyboard(chatId, user.getMessageToEdit(), text + selectedNames);
        user.setCurrentDeal(deal);
        userService.save(user);
    }

    private void handleDonePlusMinus(User user, Deal deal, Long chatId) {
        boolean isSelectingTo = List.of(PlusMinusType.GIVE, PlusMinusType.LEND).contains(deal.getPlusMinusType());

        if (isSelectingTo) {
            user.pushStatus(Status.AWAITING_AMOUNT_FOR_EACH_CURRENCY_FROM);
            userService.save(user);
            telegramSender.editMsg(chatId, user.getMessageToEdit(), "[Выдано]: " +
                    deal.getMoneyFrom().stream()
                            .map(CurrencyAmount::getCurrency)
                            .filter(Objects::nonNull)
                            .map(Money::getName)
                            .toList());
            telegramSender.sendTextWithKeyboard(chatId, "[Выдано] Введите сумму для " + deal.getMoneyFromList().get(0).getName() + ":");
        } else {
            user.pushStatus(Status.AWAITING_AMOUNT_FOR_EACH_CURRENCY_TO);
            userService.save(user);
            telegramSender.editMsg(chatId, user.getMessageToEdit(), "[Получено]: " +
                    deal.getMoneyTo().stream()
                            .map(CurrencyAmount::getCurrency)
                            .filter(Objects::nonNull)
                            .map(Money::getName)
                            .toList());
            telegramSender.sendTextWithKeyboard(chatId, "[Получено] Введите сумму для " + deal.getMoneyToList().get(0).getName() + ":");
        }
    }

    private void handleCustomDeal(CallbackQuery query, User user, Deal deal, Long chatId) {
        Money selected = Money.valueOfName(query.getData());

        switch (user.getStatus()) {
            case AWAITING_FIRST_CURRENCY -> {
                deal.setMoneyTo(List.of(new CurrencyAmount(selected, 0)));
//                if (deal.getDealType() == DealType.PLUS_MINUS) {
//                    user.pushStatus(Status.AWAITING_DEAL_AMOUNT);
//                    dealService.save(deal);
//                    userService.save(user);
//                    telegramSender.sendTextWithKeyboard(chatId, BotCommands.ASK_FOR_AMOUNT);
//                } else
                    if (deal.getDealType() == DealType.MOVING_BALANCE) {
                    user.pushStatus(Status.AWAITING_DEAL_AMOUNT);
                    deal.setMoneyFrom(List.of(new CurrencyAmount(selected, 0)));
                    dealService.save(deal);
                    userService.save(user);
                    telegramSender.sendTextWithKeyboard(chatId, BotCommands.ASK_FOR_AMOUNT);
                } else if (deal.getDealType() == DealType.CHANGE_BALANCE) {
                    user.pushStatus(Status.AWAITING_DEAL_AMOUNT);
                    if (deal.getChangeBalanceType() == ChangeBalanceType.ADD) {
                        deal.setMoneyTo(List.of(new CurrencyAmount(selected, 0)));
                    } else if (deal.getChangeBalanceType() == ChangeBalanceType.WITHDRAWAL) {
                        deal.setMoneyFrom(List.of(new CurrencyAmount(selected, 0)));
                    }
                    telegramSender.editMsg(chatId, user.getMessageToEdit(), "Выбрано: *" + selected.getName() + "*");
                    user.setCurrentDeal(deal);
                    userService.save(user);
                    telegramSender.sendTextWithKeyboard(chatId, BotCommands.ASK_FOR_AMOUNT);
                } else {
                    Message message1 = menuService.sendSelectCurrency(chatId, "Выберите валюту выдачи");
                    telegramSender.editMsg(chatId, user.getMessageToEdit(), "Получено: *" + selected.getName() + "*");
                    user.setMessageToEdit(message1.getMessageId());
                    user.pushStatus(Status.AWAITING_SECOND_CURRENCY);
                    dealService.save(deal);
                    userService.save(user);
                }
            }
            case AWAITING_SECOND_CURRENCY -> {
                deal.setMoneyFrom(List.of(new CurrencyAmount(selected, 0)));
                telegramSender.editMsg(chatId, user.getMessageToEdit(), "Выдача: *" + selected.getNameForBalance() + "*");
                user.pushStatus(Status.AWAITING_DEAL_AMOUNT);
                dealService.save(deal);
                userService.save(user);
                telegramSender.sendTextWithKeyboard(chatId, BotCommands.ASK_FOR_AMOUNT);
            }
        }
    }

    private void handleChangeBalance(String data, User user, Deal deal, Long chatId) {
        Money selected = Money.valueOfName(data);
        deal.setMoneyTo(List.of(new CurrencyAmount(selected, 0)));
        deal.setMoneyFrom(List.of(new CurrencyAmount(selected, 0)));
        dealService.save(deal);
        userService.saveUserStatus(chatId, Status.AWAITING_DEAL_AMOUNT);
        telegramSender.editMsg(chatId, user.getMessageToEdit(), "Выбрано: *" + selected.getName() + "*");
        telegramSender.sendTextWithKeyboard(chatId, BotCommands.ASK_FOR_AMOUNT);
    }

    private void handleMultiSelect(String data, User user, Deal deal, Long chatId) {
        if (data.equals("done")) {
            handleDone(user, deal, chatId);
            return;
        }

        Money selected = Money.valueOfName(data);
        boolean isSelectingTo = user.getStatus() == Status.AWAITING_FIRST_CURRENCY;

        toggleCurrencySelection(deal, selected, isSelectingTo);

        List<Money> selectedList = isSelectingTo ? deal.getMoneyToList() : deal.getMoneyFromList();
        String selectedNames = selectedList.stream()
                .filter(Objects::nonNull)
                .map(Money::getName)
                .toList()
                .toString();

        telegramSender.editMsgWithKeyboard(chatId, user.getMessageToEdit(), "Выбрано: " + selectedNames);
        user.setCurrentDeal(deal);
        userService.save(user);
    }

    private void toggleCurrencySelection(Deal deal, Money selected, boolean isSelectingTo) {
        if (isSelectingTo) {
            if (deal.getMoneyToList().contains(selected)) {
                deal.removeMoneyTo(selected);
            } else {
                deal.addMoneyTo(selected);
            }
        } else {
            if (deal.getMoneyFromList().contains(selected)) {
                deal.removeMoneyFrom(selected);
            } else {
                deal.addMoneyFrom(selected);
            }
        }
    }

    private void handleDone(User user, Deal deal, Long chatId) {

        if (user.getStatus() == Status.AWAITING_FIRST_CURRENCY
                && !List.of(DealType.TRANSPOSITION, DealType.INVOICE).contains(deal.getDealType())) {
            userService.saveUserStatus(chatId, Status.AWAITING_SECOND_CURRENCY);
            telegramSender.editMsg(chatId, user.getMessageToEdit(), "[Получено]: " +
                    userService.getUser(chatId).getCurrentDeal().getMoneyTo().stream()
                            .map(CurrencyAmount::getCurrency)
                            .filter(Objects::nonNull)
                            .map(Money::getName)
                            .toList());
            menuService.sendSelectMultiplyCurrency(chatId, "Выберите несколько валют получения:");
        } else if (user.getStatus() == Status.AWAITING_FIRST_CURRENCY
                && List.of(DealType.TRANSPOSITION, DealType.INVOICE).contains(deal.getDealType())) {
            userService.saveUserStatus(chatId, Status.AWAITING_AMOUNT_FOR_EACH_CURRENCY_TO);
            telegramSender.editMsg(chatId, user.getMessageToEdit(), "[Получено]: " +
                    userService.getUser(chatId).getCurrentDeal().getMoneyTo().stream()
                            .map(CurrencyAmount::getCurrency)
                            .filter(Objects::nonNull)
                            .map(Money::getName)
                            .toList());
            telegramSender.sendTextWithKeyboard(chatId, "[Получено] Введите сумму для " + deal.getMoneyToList().get(0).getName() + ":");
        } else {
            List<Money> moneyToList = deal.getMoneyToList();
            List<Money> moneyFromList = deal.getMoneyFromList();
            if (deal.getDealType() == DealType.TRANSPOSITION || deal.getDealType() == DealType.INVOICE) {
                userService.saveUserStatus(chatId, Status.AWAITING_AMOUNT_FOR_EACH_CURRENCY_FROM);
                telegramSender.editMsg(chatId, user.getMessageToEdit(), "[Выдача]: " +
                        user.getCurrentDeal().getMoneyFrom().stream()
                                .map(CurrencyAmount::getCurrency)
                                .filter(Objects::nonNull)
                                .map(Money::getName)
                                .toList());
                telegramSender.sendTextWithKeyboard(chatId, "[Выдано] Введите сумму для " + moneyFromList.get(0).getName() + ":");
            } else if (!moneyToList.isEmpty()) {
                userService.saveUserStatus(chatId, Status.AWAITING_AMOUNT_FOR_EACH_CURRENCY_TO);
                telegramSender.editMsg(chatId, user.getMessageToEdit(), "[Получено]: " +
                        user.getCurrentDeal().getMoneyTo().stream()
                                .map(CurrencyAmount::getCurrency)
                                .filter(Objects::nonNull)
                                .map(Money::getName)
                                .toList());
                telegramSender.sendTextWithKeyboard(chatId, "[Получено] Введите сумму для " + moneyToList.get(0).getName() + ":");
            } else {
                telegramSender.sendText(chatId, "Ошибка: не выбрано ни одной валюты для выдачи.");
            }
        }
    }
}
