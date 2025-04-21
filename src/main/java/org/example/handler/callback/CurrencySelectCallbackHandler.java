package org.example.handler.callback;

import lombok.RequiredArgsConstructor;
import org.example.model.CurrencyAmount;
import org.example.model.Deal;
import org.example.model.User;
import org.example.model.enums.DealType;
import org.example.model.enums.Money;
import org.example.model.enums.Status;
import org.example.service.DealService;
import org.example.service.UserService;
import org.example.ui.MenuService;
import org.example.infra.TelegramSender;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CurrencySelectCallbackHandler implements CallbackCommandHandler {

    private final TelegramSender telegramSender;
    private final UserService userService;
    private final MenuService menuService;
    private final DealService dealService;

    @Override
    public boolean supports(String data) {
        return switch (data) {
            case "Usd", "Eur", "UsdT", "UsdW", "Y.e.", "\nUsdT", "done" -> true;
            default -> false;
        };
    }

    public void handle(CallbackQuery query, User user) {
        Long chatId = query.getMessage().getChatId();
        String data = query.getData();
        Deal deal = user.getCurrentDeal();

        if (!(query.getMessage() instanceof Message)) {
            telegramSender.sendText(chatId, "Не удалось обработать сообщение.");
            return;
        }

        if (data.equals("done") || isMultiSelectCurrency(data)) {
            handleMultiSelect(data, user, deal, chatId);
            return;
        }

        switch (deal.getDealType()) {
            case CUSTOM -> handleCustomDeal(query, user, deal, chatId);
            case CHANGE_BALANCE -> handleChangeBalanceDeal(query, deal, chatId);
        }
    }

    private void handleChangeBalanceDeal(CallbackQuery query, Deal deal, Long chatId) {
        Money selected = Money.valueOfName(query.getData());
        deal.getMoneyTo().get(0).setCurrency(selected);
        dealService.save(deal);
        userService.saveUserStatus(chatId, Status.AWAITING_DEAL_AMOUNT);
        telegramSender.sendText(chatId, "Введите сумму: ");
    }

    private boolean isMultiSelectCurrency(String data) {
        return switch (data) {
            case "Usd", "Eur", "UsdT", "UsdW", "Y.e.", "\nUsdT", "done" -> true;
            default -> false;
        };
    }

    private void handleCustomDeal(CallbackQuery query, User user, Deal deal, Long chatId) {
        Money selected = Money.valueOfName(query.getData());

        switch (user.getStatus()) {
            case AWAITING_FIRST_CURRENCY -> {
                deal.setMoneyTo(List.of(new CurrencyAmount(selected, 0d)));
                if (deal.getDealType() == DealType.CHANGE_BALANCE) {
                    user.setStatus(Status.AWAITING_DEAL_AMOUNT);
                    telegramSender.sendText(chatId, "Введите сумму:");
                } else {
                    Message message1 = menuService.sendSelectCurrency(chatId, "Выберите валюту выдачи");
                    telegramSender.editMsg(chatId, user.getMessageToEdit(), "Получение: " + selected.getName());
                    user.setMessageToEdit(message1.getMessageId());
                    user.setStatus(Status.AWAITING_SECOND_CURRENCY);
                }
            }
            case AWAITING_SECOND_CURRENCY -> {
                deal.setMoneyFrom(List.of(new CurrencyAmount(selected, 0d)));
                telegramSender.editMsg(chatId, user.getMessageToEdit(), "Выдача: " + selected.getName());
                Message msg = telegramSender.sendText(chatId, "Введите сумму:");
                user.setStatus(Status.AWAITING_DEAL_AMOUNT);
                userService.addMessageToDel(chatId, msg.getMessageId());
            }
        }

        dealService.save(deal);
        userService.save(user);
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
        String selectedNames = selectedList.stream().map(Money::getName).toList().toString();
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
        if (user.getStatus() == Status.AWAITING_FIRST_CURRENCY) {
            userService.saveUserStatus(chatId, Status.AWAITING_SECOND_CURRENCY);
            telegramSender.editMsg(chatId, user.getMessageToEdit(), "[Получение] Выбрано: " + userService.getUser(chatId).getCurrentDeal().getMoneyTo().stream()
                    .map(CurrencyAmount::getCurrency)
                    .map(Money::getName)
                    .toList());
            menuService.sendSelectMultiplyCurrency(chatId, "Выберите несколько валют получения:");
        } else {
            userService.saveUserStatus(chatId, Status.AWAITING_AMOUNT_FOR_EACH_CURRENCY_TO);
            List<Money> moneyToList = deal.getMoneyToList();
            if (!moneyToList.isEmpty()) {
                telegramSender.editMsg(chatId, user.getMessageToEdit(), "[Выдача] Выбрано: " + userService.getUser(chatId).getCurrentDeal().getMoneyFrom().stream()
                        .map(CurrencyAmount::getCurrency)
                        .map(Money::getName)
                        .toList());
                Message message1 = telegramSender.sendText(chatId, "[Получение] Введите сумму для " + moneyToList.get(0).getName() + ":");
                userService.addMessageToDel(chatId, message1.getMessageId());

            } else {
                telegramSender.sendText(chatId, "Ошибка: не выбрано ни одной валюты для выдачи.");
            }
        }
    }
}
