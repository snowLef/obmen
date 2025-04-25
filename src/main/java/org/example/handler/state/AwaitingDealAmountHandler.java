package org.example.handler.state;

import lombok.RequiredArgsConstructor;
import org.example.infra.TelegramSender;
import org.example.model.Deal;
import org.example.model.User;
import org.example.model.enums.ChangeBalanceType;
import org.example.model.enums.DealType;
import org.example.model.enums.Status;
import org.example.service.UserService;
import org.example.ui.MenuService;
import org.example.util.MessageUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class AwaitingDealAmountHandler implements UserStateHandler {

    private final UserService userService;
    private final TelegramSender telegramSender;
    private final MenuService menuService;
    private final MessageUtils messageUtils;

    @Override
    public void handle(Message message, User user) {
        long chatId = message.getChatId();
        int msgId = message.getMessageId();
        String text = message.getText();
        Deal deal = user.getCurrentDeal();
        DealType dealType = user.getCurrentDeal().getDealType();
        String money = "";

        if (dealType == DealType.BUY) {
            money = deal.getMoneyTo().get(0).getCurrency().getName();
        } else if (dealType == DealType.SELL) {
            money = deal.getMoneyFrom().get(0).getCurrency().getName();

        }


        try {
            long amount = Math.round(Long.parseLong(text));
            telegramSender.editMsg(chatId, user.getMessageToEdit(), "Сумма: " + messageUtils.formatWithSpacesAndDecimals(amount) + " " + money);


            switch (dealType) {
                case BUY -> {
                    deal.getMoneyTo().get(0).setAmount(amount);
                    user.pushStatus(Status.AWAITING_EXCHANGE_RATE);
                    userService.save(user);
                    sendEnterRateMessage(chatId);
                }
                case SELL -> {
                    deal.getMoneyFrom().get(0).setAmount(amount);
                    user.pushStatus(Status.AWAITING_EXCHANGE_RATE);
                    userService.save(user);
                    sendEnterRateMessage(chatId);
                }
                case CUSTOM -> {
                    deal.setCurrentAmount(amount);
                    user.pushStatus(Status.AWAITING_SELECT_AMOUNT);
                    userService.save(user);
                    menuService.sendSelectAmountType(chatId);
                }
                case PLUS_MINUS -> {
                    deal.getMoneyTo().get(0).setAmount(amount);
                    user.pushStatus(Status.AWAITING_APPROVE);
                    userService.save(user);
                    menuService.sendApproveMenu(chatId);
                }
                case MOVING_BALANCE -> {
                    deal.getMoneyFrom().get(0).setAmount(amount);
                    deal.getMoneyTo().get(0).setAmount(amount);
                    user.pushStatus(Status.AWAITING_APPROVE);
                    userService.save(user);
                    menuService.sendApproveMenu(chatId);
                }
                case CHANGE_BALANCE -> {
                    if (user.getChangeBalanceType() == ChangeBalanceType.ADD) {
                        deal.getMoneyTo().get(0).setAmount(amount);
                    } else if (user.getChangeBalanceType() == ChangeBalanceType.WITHDRAWAL) {
                        deal.getMoneyFrom().get(0).setAmount(amount);
                    }
                    user.pushStatus(Status.AWAITING_COMMENT);
                    userService.save(user);
                    telegramSender.sendTextWithKeyboard(chatId, "Введите комментарий: ");
                }
            }

            userService.addMessageToDel(chatId, msgId);

        } catch (NumberFormatException e) {
            telegramSender.sendTextWithKeyboard(chatId, "Неверный формат суммы.");
        }
    }

    private void sendEnterRateMessage(long chatId) {
        telegramSender.sendTextWithKeyboard(chatId, "Введите курс: ");
    }

    @Override
    public Status getSupportedStatus() {
        return Status.AWAITING_DEAL_AMOUNT;
    }
}