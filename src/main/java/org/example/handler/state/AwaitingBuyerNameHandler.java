package org.example.handler.state;

import lombok.RequiredArgsConstructor;
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

        DealType dealType = user.getCurrentDeal().getDealType();

        switch (dealType) {
            case BUY -> {
                userService.saveUserStatus(chatId, Status.AWAITING_DEAL_AMOUNT);
                Message botMsg = telegramSender.sendText(chatId, "Введите сумму в %s:".formatted(user.getCurrentDeal().getMoneyTo().get(0).getCurrency().getName()));
                userService.addMessageToDel(chatId, botMsg.getMessageId());
            }
            case SELL -> {
                userService.saveUserStatus(chatId, Status.AWAITING_DEAL_AMOUNT);
                Message botMsg = telegramSender.sendText(chatId, "Введите сумму в %s:".formatted(user.getCurrentDeal().getMoneyFrom().get(0).getCurrency().getName()));
                userService.addMessageToDel(chatId, botMsg.getMessageId());
            }
            case CUSTOM -> {
                userService.saveUserStatus(chatId, Status.AWAITING_FIRST_CURRENCY);
                menuService.sendSelectCurrency(chatId, "Выберите валюту получения");
            }
            case CHANGE_BALANCE -> {
                userService.saveUserStatus(chatId, Status.AWAITING_CHANGE_BALANCE_TYPE);
                menuService.sendChangeBalanceMenu(chatId);
            }
            case TRANSPOSITION, INVOICE -> {
                userService.saveUserStatus(chatId, Status.AWAITING_CITY_NAME);
                Message botMsg = telegramSender.sendText(chatId, "Откуда/куда?");
                userService.addMessageToDel(chatId, botMsg.getMessageId());
            }
        }
    }

    @Override
    public Status getSupportedStatus() {
        return Status.AWAITING_BUYER_NAME;
    }
}

