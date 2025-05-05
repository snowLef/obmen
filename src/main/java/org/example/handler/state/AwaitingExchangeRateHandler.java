package org.example.handler.state;

import lombok.RequiredArgsConstructor;
import org.example.infra.TelegramSender;
import org.example.model.Deal;
import org.example.model.User;
import org.example.model.enums.AmountType;
import org.example.model.enums.CurrencyType;
import org.example.model.enums.Status;
import org.example.repository.DealRepository;
import org.example.service.UserService;
import org.example.ui.MenuService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class AwaitingExchangeRateHandler implements UserStateHandler {

    private final UserService userService;
    private final TelegramSender telegramSender;
    private final MenuService menuService;
    private final DealRepository dealRepository;

    @Override
    public void handle(Message message, User user) {
        long chatId = message.getChatId();
        int msgId = message.getMessageId();
        String text = message.getText();

        telegramSender.editMsg(chatId, user.getMessageToEdit(), "Курс: " + text);

        try {
            double rate = Double.parseDouble(text.replace(",", "."));
            Deal deal = user.getCurrentDeal();
            deal.setExchangeRate(rate);
            user.setCurrentDeal(deal);

            double to = deal.getAmountTo() * rate;
            double from = deal.getAmountFrom() * rate;

            switch (deal.getDealType()) {
                case CUSTOM -> handleCustomRate(chatId, user, rate);
                case BUY -> {
                    deal.setAmountFrom(Math.round(to));
                    deal.setExchangeRate(rate);
                    user.setCurrentDeal(deal);
                    user.pushStatus(Status.AWAITING_APPROVE);
                    userService.save(user);
                    menuService.sendApproveMenu(chatId);
                }
                case SELL -> {
                    deal.setAmountTo(Math.round(from));
                    deal.setExchangeRate(rate);
                    user.setCurrentDeal(deal);
                    user.pushStatus(Status.AWAITING_APPROVE);
                    userService.save(user);
                    menuService.sendApproveMenu(chatId);
                }
            }

            userService.addMessageToDel(chatId, msgId);
        } catch (NumberFormatException e) {
            Message botMsg = telegramSender.sendText(chatId, "Неверный формат курса.");
            userService.addMessageToDel(chatId, botMsg.getMessageId());
        }
    }

    private void handleCustomRate(long chatId, User user, double rate) {
        Deal deal = user.getCurrentDeal();
        user.pushStatus(Status.AWAITING_EXCHANGE_RATE_TYPE);
        user.setCurrentDeal(deal);
        userService.save(user);
        menuService.sendSelectCurrencyType(chatId);
    }

    @Override
    public Status getSupportedStatus() {
        return Status.AWAITING_EXCHANGE_RATE;
    }
}