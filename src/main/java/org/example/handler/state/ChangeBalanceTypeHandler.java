package org.example.handler.state;

import org.example.model.User;
import org.example.model.enums.ChangeBalanceType;
import org.example.model.enums.Status;
import org.example.service.UserService;
import org.example.ui.MenuService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class ChangeBalanceTypeHandler implements UserStateHandler {

    private final UserService userService;
    private final MenuService menuService;

    public ChangeBalanceTypeHandler(UserService userService, MenuService menuService) {
        this.userService = userService;
        this.menuService = menuService;
    }

    @Override
    public void handle(Message message, User user) {
        long chatId = message.getChatId();
        String input = message.getText();
        int msgId = message.getMessageId();

        ChangeBalanceType balanceType = null;

        switch (input) {
            case "Принимаем +":
                balanceType = ChangeBalanceType.GET;
                break;
            case "Отдаем +":
                balanceType = ChangeBalanceType.GIVE;
                break;
            case "Даем в долг":
                balanceType = ChangeBalanceType.LEND;
                break;
            case "Возврат долга":
                balanceType = ChangeBalanceType.DEBT_REPAYMENT;
                break;
            default:
                menuService.sendChangeBalanceMenu(chatId);  // Повторно показать меню, если введено что-то не то
                return;
        }

        user.setChangeBalanceType(balanceType);
        user.pushStatus(Status.AWAITING_FIRST_CURRENCY);
        userService.save(user);
        menuService.sendSelectCurrency(chatId, "Выберите валюту:");

        userService.addMessageToDel(chatId, msgId);
    }

    @Override
    public Status getSupportedStatus() {
        return Status.AWAITING_CHANGE_BALANCE_TYPE;
    }
}
