package org.example.handler.callback;

import lombok.RequiredArgsConstructor;
import org.example.infra.TelegramSender;
import org.example.model.User;
import org.example.model.enums.AmountType;
import org.example.model.enums.Status;
import org.example.service.UserService;
import org.example.ui.MenuService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class AwaitingSelectAmountCallbackHandler implements CallbackCommandHandler {

    private final UserService userService;
    private final MenuService menuService;
    private final TelegramSender telegramSender;

    @Override
    public void handle(CallbackQuery query, User user) {
        Long chatId = query.getMessage().getChatId();
        String data = query.getData();

        if ("give".equals(data)) {
            user.setAmountType(AmountType.GIVE);
            user.setStatus(Status.AWAITING_EXCHANGE_RATE_TYPE);
            user.getCurrentDeal().getMoneyFrom().get(0).setAmount(user.getCurrentDeal().getCurrentAmount());
            userService.save(user);
            Message message = menuService.sendSelectCurrencyType(chatId);
            userService.addMessageToDel(chatId, message.getMessageId());
        } else if ("receive".equals(data)) {
            user.setAmountType(AmountType.RECEIVE);
            user.setStatus(Status.AWAITING_EXCHANGE_RATE_TYPE);
            user.getCurrentDeal().getMoneyTo().get(0).setAmount(user.getCurrentDeal().getCurrentAmount());
            userService.save(user);
            Message message = menuService.sendSelectCurrencyType(chatId);
            userService.addMessageToDel(chatId, message.getMessageId());
        } else {
            menuService.sendSelectAmountType(chatId); // повторно покажем кнопки
        }
    }

    @Override
    public boolean supports(String data) {
        return switch (data) {
            case "give", "receive" -> true;
            default -> false;
        };
    }
}