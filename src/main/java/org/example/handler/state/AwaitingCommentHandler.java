package org.example.handler.state;

import lombok.RequiredArgsConstructor;
import org.example.infra.TelegramSender;
import org.example.model.User;
import org.example.model.enums.DealType;
import org.example.model.enums.Status;
import org.example.service.UserService;
import org.example.ui.MenuService;
import org.example.util.MessageUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class AwaitingCommentHandler implements UserStateHandler {

    private final UserService userService;
    private final TelegramSender telegramSender;
    private final MenuService menuService;
    private final MessageUtils messageUtils;

    @Override
    public void handle(Message message, User user) {
        long chatId = message.getChatId();
        int msgId = message.getMessageId();
        String text = message.getText();

        user.getCurrentDeal().setComment(text);
        userService.save(user);
        userService.addMessageToDel(chatId, msgId);
        userService.addMessageToEdit(chatId, msgId);

        telegramSender.editMsg(chatId, user.getMessageToEdit(), "Комментарий: " + messageUtils.escapeMarkdown(text));

        userService.saveUserStatus(chatId, Status.AWAITING_APPROVE);
        menuService.sendApproveMenu(chatId);
    }

    @Override
    public Status getSupportedStatus() {
        return Status.AWAITING_COMMENT;
    }
}

