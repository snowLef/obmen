package org.example.handler.state;

import org.example.model.User;
import org.example.model.enums.PlusMinusType;
import org.example.model.enums.Status;
import org.example.repository.UserRepository;
import org.example.service.UserService;
import org.example.ui.MenuService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class PlusMinusTypeHandler implements UserStateHandler {

    private final UserService userService;
    private final MenuService menuService;
    private final UserRepository userRepository;

    public PlusMinusTypeHandler(UserService userService, MenuService menuService, UserRepository userRepository) {
        this.userService = userService;
        this.menuService = menuService;
        this.userRepository = userRepository;
    }

    @Override
    public void handle(Message message, User user) {
        long chatId = message.getChatId();
        String input = message.getText();
        int msgId = message.getMessageId();

        PlusMinusType balanceType = null;

        switch (input) {
            case "Принимаем +":
                balanceType = PlusMinusType.GET;
                break;
            case "Отдаем +":
                balanceType = PlusMinusType.GIVE;
                break;
            case "Даем в долг":
                balanceType = PlusMinusType.LEND;
                break;
            case "Возврат долга":
                balanceType = PlusMinusType.DEBT_REPAYMENT;
                break;
            default:
                menuService.sendPlusMinusMenu(chatId);  // Повторно показать меню, если введено что-то не то
                return;
        }

        user.setPlusMinusType(balanceType);
        userRepository.save(user);
        userService.saveUserStatus(chatId, Status.AWAITING_FIRST_CURRENCY);
        menuService.sendSelectCurrency(chatId, "Выберите валюту +/-");
        userService.addMessageToDel(chatId, msgId);
    }

    @Override
    public Status getSupportedStatus() {
        return Status.AWAITING_PLUS_MINUS_TYPE;
    }
}
