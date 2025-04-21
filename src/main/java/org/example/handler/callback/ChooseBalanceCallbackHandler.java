package org.example.handler.callback;

import lombok.RequiredArgsConstructor;
import org.example.infra.TelegramSender;
import org.example.model.User;
import org.example.model.enums.BalanceType;
import org.example.model.enums.CurrencyType;
import org.example.model.enums.Money;
import org.example.model.enums.Status;
import org.example.service.UserService;
import org.example.ui.MenuService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class ChooseBalanceCallbackHandler implements CallbackCommandHandler {

    private final TelegramSender telegramSender;
    private final UserService userService;
    private final MenuService menuService;

    @Override
    public boolean supports(String data) {
        return Arrays.stream(BalanceType.values())
                .anyMatch(m -> m.name().equalsIgnoreCase(data));
    }

    public void handle(CallbackQuery query, User user) {
        Long chatId = query.getMessage().getChatId();
        String data = query.getData();
        BalanceType type = null;

        switch (data) {
            case "OWN" -> type = BalanceType.OWN;
            case "FOREIGN" -> type = BalanceType.FOREIGN;
            case "DEBT" -> type = BalanceType.DEBT;
        }

        if (user.getStatus().equals(Status.AWAITING_CHOOSE_BALANCE_FROM)) {
            user.setBalanceFrom(type);
            user.pushStatus(Status.AWAITING_CHOOSE_BALANCE_TO);
            telegramSender.editMsg(chatId, user.getMessageToEdit(), "Будет списано с: %s".formatted(user.getBalanceFrom().getDisplayName()));
            userService.save(user);
            menuService.sendSelectBalance(chatId, "Куда перевести?");
        } else if (user.getStatus().equals(Status.AWAITING_CHOOSE_BALANCE_TO)) {
            user.setBalanceTo(type);
            user.pushStatus(Status.AWAITING_FIRST_CURRENCY);
            telegramSender.editMsg(chatId, user.getMessageToEdit(), "Будет переведено на: %s".formatted(user.getBalanceTo().getDisplayName()));
            userService.save(user);
            menuService.sendSelectCurrency(chatId, "Выберите валюту получения");
        }
    }
}
