package org.example.handler.state;

import lombok.RequiredArgsConstructor;
import org.example.constants.BotCommands;
import org.example.infra.TelegramSender;
import org.example.model.User;
import org.example.model.enums.DealType;
import org.example.model.enums.Status;
import org.example.service.UserService;
import org.example.ui.MenuService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class AwaitingBuyerNameHandler implements UserStateHandler {

    private final UserService userService;
    private final TelegramSender telegramSender;
    private final MenuService menuService;

    @Override
    public void handle(Message message, User user) {
        long chatId = message.getChatId();
        int msgId = message.getMessageId();
        String text = message.getText();

        userService.saveBuyerName(chatId, text);
        userService.addMessageToDel(chatId, msgId);
        userService.addMessageToEdit(chatId, msgId);

        telegramSender.editMsg(chatId, user.getMessageToEdit(), "Имя: " + text);

        DealType dealType = user.getCurrentDeal().getDealType();

        switch (dealType) {
            case BUY -> {
                userService.saveUserStatus(chatId, Status.AWAITING_DEAL_AMOUNT);
                telegramSender.sendTextWithKeyboard(chatId, "Введите сумму в *%s:*".formatted(user.getCurrentDeal().getMoneyTo().get(0).getCurrency().getName()));
            }
            case SELL -> {
                userService.saveUserStatus(chatId, Status.AWAITING_DEAL_AMOUNT);
                telegramSender.sendTextWithKeyboard(chatId, "Введите сумму в *%s:*".formatted(user.getCurrentDeal().getMoneyFrom().get(0).getCurrency().getName()));
            }
            case CUSTOM -> {
                userService.saveUserStatus(chatId, Status.AWAITING_FIRST_CURRENCY);
                menuService.sendSelectCurrency(chatId, "Выберите валюту получения");
            }
            case PLUS_MINUS -> {
//                userService.saveUserStatus(chatId, Status.AWAITING_PLUS_MINUS_TYPE);
//                menuService.sendPlusMinusMenu(chatId);
                userService.saveUserStatus(chatId, Status.AWAITING_FIRST_CURRENCY);
                menuService.sendSelectCurrency(chatId, "Выберите валюту +/-");
            }
            case TRANSPOSITION, INVOICE -> {
                userService.saveUserStatus(chatId, Status.AWAITING_CITY_NAME);
                telegramSender.sendTextWithKeyboard(chatId, BotCommands.ASK_FOR_CITY);
            }
        }
    }

    @Override
    public Status getSupportedStatus() {
        return Status.AWAITING_BUYER_NAME;
    }
}

