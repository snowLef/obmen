package org.example.handler.callback;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.constants.BotCommands;
import org.example.infra.TelegramSender;
import org.example.model.User;
import org.example.model.enums.DealType;
import org.example.model.enums.Status;
import org.example.service.UserService;
import org.example.ui.MenuService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BackCallbackHandler implements CallbackCommandHandler {
    private final UserService userService;
    private final TelegramSender telegramSender;
    private final MenuService menuService;

    @Override
    public boolean supports(String data) {
        return data.equals("back");
    }

    @Override
    public void handle(CallbackQuery query, User user) {
        Long chatId = query.getMessage().getChatId();
        Status previousStatus = user.popStatus(); // Восстанавливаем предыдущий статус

        if (previousStatus == null) {
            telegramSender.sendText(chatId, "Невозможно вернуться назад");
            return;
        }

        List<Integer> msgs = userService.getMessageIdsToDeleteWithInit(chatId);
        int maxValue = Collections.max(msgs); // Находим максимум
        int maxIndex = msgs.indexOf(maxValue); // Находим его индекс

        telegramSender.deleteMessage(chatId, maxValue);
        msgs.remove(maxIndex);
        user.setMessages(msgs);
        userService.save(user);

        switch (previousStatus) {
            case AWAITING_BUYER_NAME -> {
                user.pushStatus(Status.AWAITING_BUYER_NAME);
                user.setPreviousStatus(Status.IDLE);
                userService.save(user);
                telegramSender.sendTextWithKeyboard(chatId, BotCommands.ASK_FOR_NAME);
            }
            case AWAITING_DEAL_AMOUNT -> {
                user.pushStatus(Status.AWAITING_DEAL_AMOUNT);
                user.setPreviousStatus(null);
                userService.save(user);
                telegramSender.sendTextWithKeyboard(chatId, BotCommands.ASK_FOR_AMOUNT);
            }
            case AWAITING_FIRST_CURRENCY -> {
                user.pushStatus(Status.AWAITING_FIRST_CURRENCY);
                user.setPreviousStatus(null);
                userService.save(user);
                if (List.of(DealType.BUY, DealType.SELL, DealType.CUSTOM).contains(user.getCurrentDeal().getDealType())) {
                    menuService.sendSelectCurrency(chatId, "Выберите валюту получения:");
                } else {
                    menuService.sendSelectFullCurrency(chatId, "Выберите валюту получения:");
                }
            }
            case AWAITING_SECOND_CURRENCY -> {
                user.pushStatus(Status.AWAITING_SECOND_CURRENCY);
                user.setPreviousStatus(null);
                userService.save(user);
                menuService.sendSelectCurrency(chatId, "Выберите валюту выдачи:");
            }
            case AWAITING_SELECT_AMOUNT -> {
                user.pushStatus(Status.AWAITING_SELECT_AMOUNT);
                user.setPreviousStatus(null);
                userService.save(user);
                menuService.sendSelectAmountType(chatId);
            }
            case AWAITING_EXCHANGE_RATE_TYPE -> {
                user.pushStatus(Status.AWAITING_EXCHANGE_RATE_TYPE);
                user.setPreviousStatus(null);
                userService.save(user);
                menuService.sendSelectCurrencyType(chatId);
            }
            case AWAITING_CITY_NAME -> {
                user.pushStatus(Status.AWAITING_CITY_NAME);
                user.setPreviousStatus(Status.IDLE);
                userService.save(user);
                telegramSender.sendTextWithKeyboard(chatId, BotCommands.ASK_FOR_CITY);
            }
            case AWAITING_APPROVE -> {
                user.setPreviousStatus(null);
                if (user.getCurrentDeal().getDealType() == DealType.TRANSPOSITION
                        || user.getCurrentDeal().getDealType() == DealType.INVOICE) {
                    user.pushStatus(Status.AWAITING_APPROVE);
                    user.getCurrentDeal().setCurrentCurrencyIndex(0);
                    userService.save(user);
                    menuService.sendTranspositionOrInvoiceApprove(chatId);
                }
            }
            case AWAITING_EXCHANGE_RATE -> {
                user.pushStatus(Status.AWAITING_EXCHANGE_RATE);
                user.setPreviousStatus(null);
                userService.save(user);
                menuService.sendEnterExchangeRate(chatId);
            }
            case AWAITING_AMOUNT_FOR_EACH_CURRENCY_FROM -> {
                user.pushStatus(Status.AWAITING_AMOUNT_FOR_EACH_CURRENCY_FROM);
                user.getCurrentDeal().setCurrentCurrencyIndex(0);
                user.setPreviousStatus(null);
                userService.save(user);
                telegramSender.sendTextWithKeyboard(chatId, "[Выдано] Введите сумму для "
                        + user.getCurrentDeal().getMoneyFromList().get(0).getName() + ":");
            }
            case AWAITING_AMOUNT_FOR_EACH_CURRENCY_TO -> {
                user.pushStatus(Status.AWAITING_AMOUNT_FOR_EACH_CURRENCY_TO);
                user.getCurrentDeal().setCurrentCurrencyIndex(0);
                user.setPreviousStatus(null);
                userService.save(user);
                telegramSender.sendTextWithKeyboard(chatId, "[Получено] Введите сумму для "
                        + user.getCurrentDeal().getMoneyToList().get(0).getName() + ":");
            }
            case AWAITING_CHOOSE_BALANCE_FROM -> {
                user.pushStatus(Status.AWAITING_CHOOSE_BALANCE_FROM);
                user.setPreviousStatus(null);
                userService.save(user);
                menuService.sendSelectBalance(chatId, "Откуда списать?");
            }
            case AWAITING_COMMENT -> {
                user.pushStatus(Status.AWAITING_COMMENT);
                user.setPreviousStatus(null);
                userService.save(user);
                telegramSender.sendTextWithKeyboard(chatId, "Введите комментарий: ");
            }
            default -> {
                userService.resetUserState(user);
                menuService.sendMainMenu(chatId);
            }
        }
    }
}