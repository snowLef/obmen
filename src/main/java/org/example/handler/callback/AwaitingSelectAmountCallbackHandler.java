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
            user.pushStatus(Status.AWAITING_EXCHANGE_RATE);
            user.getCurrentDeal().getMoneyFrom().get(0).setAmount(user.getCurrentDeal().getCurrentAmount());
            userService.save(user);
        } else if ("receive".equals(data)) {
            user.setAmountType(AmountType.RECEIVE);
            user.pushStatus(Status.AWAITING_EXCHANGE_RATE);
            user.getCurrentDeal().getMoneyTo().get(0).setAmount(user.getCurrentDeal().getCurrentAmount());
            userService.save(user);
        }

            telegramSender.sendTextWithKeyboard(chatId, "Введите курс:");
            if (user.getAmountType() == AmountType.GIVE) {
                telegramSender.editMsg(chatId, user.getMessageToEdit(),
                        user.getCurrentDeal().getCurrentAmount() + " *" + user.getCurrentDeal().getMoneyFrom().get(0).getCurrency().getName() + "* отдать");
            } else if (user.getAmountType() == AmountType.RECEIVE) {
                telegramSender.editMsg(chatId, user.getMessageToEdit(),
                        user.getCurrentDeal().getCurrentAmount() + " *" + user.getCurrentDeal().getMoneyTo().get(0).getCurrency().getName() + "* забрать");
            } else {
                menuService.sendSelectAmountType(chatId);
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