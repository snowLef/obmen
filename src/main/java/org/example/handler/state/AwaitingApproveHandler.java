package org.example.handler.state;

import lombok.RequiredArgsConstructor;
import org.example.model.User;
import org.example.model.enums.Status;
import org.example.service.ExchangeProcessor;
import org.example.service.UserService;
import org.example.ui.MenuService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class AwaitingApproveHandler implements UserStateHandler {

    private final UserService userService;
    private final MenuService menuService;
    private final ExchangeProcessor exchangeProcessor;

    @Override
    public void handle(Message message, User user) {
        long chatId = message.getChatId();
        String input = message.getText();
        int msgId = message.getMessageId();

        if (input.equals("✅ Подтвердить")) {
            exchangeProcessor.approve(chatId, user.getCurrentDeal());
            user.pushStatus(Status.IDLE);
            userService.save(user);
            menuService.sendDealCompletedWithCancel(chatId, user.getCurrentDeal());

        } else if (input.equals("❌ Отменить")) {
            user.setCurrentDeal(null);
            user.pushStatus(Status.IDLE);
            userService.save(user);
            menuService.sendMainMenu(chatId);
        } else {
            menuService.sendApproveMenu(chatId); // Повторно показать меню, если ввод неизвестен
        }

        userService.addMessageToDel(chatId, msgId);
    }

    @Override
    public Status getSupportedStatus() {
        return Status.AWAITING_APPROVE;
    }
}

