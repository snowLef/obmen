package org.example.handler.callback;

import lombok.RequiredArgsConstructor;
import org.example.infra.TelegramSender;
import org.example.model.CurrencyAmount;
import org.example.model.Deal;
import org.example.model.User;
import org.example.model.enums.CurrencyType;
import org.example.model.enums.DealType;
import org.example.model.enums.Money;
import org.example.model.enums.Status;
import org.example.service.DealService;
import org.example.service.UserService;
import org.example.ui.MenuService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ExchangeRateTypeCallbackHandler implements CallbackCommandHandler {

    private final TelegramSender telegramSender;
    private final UserService userService;
    private final MenuService menuService;

    @Override
    public boolean supports(String data) {
        return switch (data) {
            case "division", "multiplication" -> true;
            default -> false;
        };
    }

    public void handle(CallbackQuery query, User user) {
        Long chatId = query.getMessage().getChatId();
        String data = query.getData();
        CurrencyType type = null;

        switch (data) {
            case "division" -> type = CurrencyType.DIVISION;
            case "multiplication" -> type = CurrencyType.MULTIPLICATION;
        }

        if (user.getStatus().equals(Status.AWAITING_EXCHANGE_RATE_TYPE)) {
            user.setCurrencyType(type);
            user.pushStatus(Status.AWAITING_APPROVE);
            telegramSender.editMsg(chatId, user.getMessageToEdit(), "Формула расчета: %s".formatted(user.getCurrencyType().getText()));
            userService.save(user);
//            telegramSender.sendTextWithKeyboard(chatId, "Введите курс:");
            menuService.sendApproveMenu(chatId);
        }
    }
}
