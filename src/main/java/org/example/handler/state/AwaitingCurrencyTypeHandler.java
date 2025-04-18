package org.example.handler.state;

import lombok.RequiredArgsConstructor;
import org.example.model.User;
import org.example.model.enums.CurrencyType;
import org.example.model.enums.Status;
import org.example.service.UserService;
import org.example.ui.MenuService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class AwaitingCurrencyTypeHandler implements UserStateHandler {

    private final UserService userService;
    private final MenuService menuService;

    @Override
    public void handle(Message message, User user) {
        long chatId = message.getChatId();
        int msgId = message.getMessageId();
        String text = message.getText();

        CurrencyType selectedType = parseCurrencyType(text);
        if (selectedType == null) {
            menuService.sendSelectCurrencyType(chatId); // повторное меню
            return;
        }

        user.setCurrencyType(selectedType);
        user.setStatus(Status.AWAITING_EXCHANGE_RATE);
        userService.save(user);

        menuService.sendEnterExchangeRate(chatId);
        userService.addMessageToDel(chatId, msgId);
    }

    private CurrencyType parseCurrencyType(String text) {
        return switch (text) {
            case "Деление" -> CurrencyType.DIVISION;
            case "Умножение" -> CurrencyType.MULTIPLICATION;
            default -> null;
        };
    }

    @Override
    public Status getSupportedStatus() {
        return Status.AWAITING_CURRENCY_TYPE;
    }
}
