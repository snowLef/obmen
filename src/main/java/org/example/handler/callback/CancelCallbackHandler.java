package org.example.handler.callback;

import lombok.RequiredArgsConstructor;
import org.example.infra.TelegramSender;
import org.example.model.User;
import org.example.model.enums.Status;
import org.example.service.DealService;
import org.example.service.UserService;
import org.example.ui.MenuService;
import org.example.util.MessageUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
@RequiredArgsConstructor
public class CancelCallbackHandler implements CallbackCommandHandler {
    private final UserService userService;
    private final TelegramSender telegramSender;
    private final MenuService menuService;
    private final MessageUtils messageUtils;

    @Override
    public boolean supports(String data) {
        return data.equals("cancel");
    }

    @Override
    public void handle(CallbackQuery query, User user) {
        Long chatId = query.getMessage().getChatId();

        // Отменяем сделку
        if (user.getCurrentDeal() != null) {
            user.setCurrentDeal(null);
            userService.save(user);
        }

        // Сбрасываем статус
        user.pushStatus(Status.IDLE);
        user.getStatusHistory().clear();

        menuService.sendMainMenu(chatId);
        telegramSender.sendText(chatId, "✖ Операция отменена");
        userService.save(user);

        messageUtils.deleteMsgs(chatId, userService.getMessageIdsToDeleteWithInit(chatId));
        userService.resetUserState(user);
        menuService.sendMainMenu(chatId);
    }
}