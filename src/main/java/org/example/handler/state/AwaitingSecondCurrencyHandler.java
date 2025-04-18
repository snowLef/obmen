package org.example.handler.state;

import lombok.RequiredArgsConstructor;
import org.example.model.User;
import org.example.model.enums.AmountType;
import org.example.model.enums.Money;
import org.example.model.enums.Status;
import org.example.service.UserService;
import org.example.ui.MenuService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class AwaitingSecondCurrencyHandler implements UserStateHandler {

    private final UserService userService;
    private final MenuService menuService;

    @Override
    public void handle(Message message, User user) {
        long chatId = message.getChatId();
        int msgId = message.getMessageId();
        String text = message.getText();

        try {
            Money selectedCurrency = Money.valueOfName(text);

            if (user.getAmountType() == AmountType.GIVE) {
                user.getCurrentDeal().setMoneyTo(selectedCurrency);
            } else if (user.getAmountType() == AmountType.RECEIVE) {
                user.getCurrentDeal().setMoneyFrom(selectedCurrency);
            }

            user.setStatus(Status.AWAITING_CURRENCY_TYPE);
            userService.save(user);
            menuService.sendSelectCurrencyType(chatId);
        } catch (IllegalArgumentException e) {
            menuService.sendSelectCurrency(chatId, "Выберите валюту из списка:");
        }

        userService.addMessageToDel(chatId, msgId);
    }

    @Override
    public Status getSupportedStatus() {
        return Status.AWAITING_SECOND_CURRENCY;
    }
}
