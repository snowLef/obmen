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

        try {
            double rate = Double.parseDouble(text.replace(",", "."));
            Deal deal = user.getCurrentDeal();
            deal.setExchangeRate(rate);

            switch (deal.getDealType()) {
                case CUSTOM -> handleCustomRate(user, rate);
                case BUY -> deal.setAmountFrom((double) Math.round(deal.getAmountTo() * rate));
                case SELL -> deal.setAmountTo((double) Math.round(deal.getAmountFrom() * rate));
            }

            user.setStatus(Status.AWAITING_APPROVE);
            userService.save(user);
            userService.addMessageToDel(chatId, msgId);
            menuService.sendApproveMenu(chatId);

        } catch (NumberFormatException e) {
            Message botMsg = telegramSender.sendText(chatId, "Неверный формат курса.");
            userService.addMessageToDel(chatId, botMsg.getMessageId());
        }
    }

    private void handleCustomRate(User user, double rate) {
        Deal deal = user.getCurrentDeal();

        Double amountFrom = deal.getAmountFrom();
        Double amountTo = deal.getAmountTo();
        AmountType amountType = user.getAmountType();
        CurrencyType currencyType = user.getCurrencyType();

        if (amountType == AmountType.GIVE) {
            if (currencyType == CurrencyType.DIVISION) {
                deal.getMoneyTo().get(0).setAmount(amountFrom / rate);
            } else if (currencyType == CurrencyType.MULTIPLICATION) {
                deal.getMoneyTo().get(0).setAmount(amountFrom * rate);
            }
        } else if (amountType == AmountType.RECEIVE) {
            if (currencyType == CurrencyType.DIVISION) {
                deal.getMoneyFrom().get(0).setAmount(amountTo / rate);
            } else if (currencyType == CurrencyType.MULTIPLICATION) {
                deal.getMoneyFrom().get(0).setAmount(amountTo * rate);
            }
        }
        dealRepository.save(deal);
    }

    @Override
    public Status getSupportedStatus() {
        return Status.AWAITING_EXCHANGE_RATE;
    }
}