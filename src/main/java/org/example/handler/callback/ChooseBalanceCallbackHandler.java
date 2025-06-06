package org.example.handler.callback;

import lombok.RequiredArgsConstructor;
import org.example.infra.TelegramSender;
import org.example.model.Deal;
import org.example.model.User;
import org.example.model.enums.BalanceType;
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
        Deal deal = user.getCurrentDeal();

        switch (data) {
            case "OWN" -> type = BalanceType.OWN;
            case "FOREIGN" -> type = BalanceType.FOREIGN;
            case "DEBT" -> type = BalanceType.DEBT;
        }

        if (user.getStatus().equals(Status.AWAITING_CHOOSE_BALANCE_FROM)) {
            deal.setBalanceTypeFrom(type);
            if (type == BalanceType.OWN) {
                deal.setBalanceTypeTo(BalanceType.FOREIGN);
            } else if (type == BalanceType.FOREIGN) {
                deal.setBalanceTypeTo(BalanceType.OWN);
            }
            user.pushStatus(Status.AWAITING_FIRST_CURRENCY);
            telegramSender.editMsg(chatId, user.getMessageToEdit(), "Будет списано с: %s".formatted(deal.getBalanceTypeFrom().getDisplayName()));
            userService.save(user);
            menuService.sendSelectFullCurrency(chatId, "Выберите валюту получения");
        }
    }
}
