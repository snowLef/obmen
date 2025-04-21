package org.example.handler.callback;

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
                menuService.sendSelectCurrency(chatId, "Выберите валюту получения:");
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
                if (user.getCurrentDeal().getDealType() == DealType.TRANSPOSITION
                        || user.getCurrentDeal().getDealType() == DealType.INVOICE) {
                    user.pushStatus(Status.AWAITING_APPROVE);
                    user.setCurrentCurrencyIndex(0);
                    userService.save(user);
                    menuService.sendTranspositionOrInvoiceApprove(chatId);
                } else {

                }
            }
            case AWAITING_EXCHANGE_RATE -> {
                user.pushStatus(Status.AWAITING_EXCHANGE_RATE);
                userService.save(user);
                menuService.sendEnterExchangeRate(chatId);
            }
            case AWAITING_AMOUNT_FOR_EACH_CURRENCY_FROM -> {
                user.pushStatus(Status.AWAITING_AMOUNT_FOR_EACH_CURRENCY_FROM);
                user.setCurrentCurrencyIndex(0);
                userService.save(user);
                menuService.sendTranspositionOrInvoiceApprove(chatId);
            }
            default -> {
                userService.resetUserState(user);
                menuService.sendMainMenu(chatId);
            }
        }
    }
}