package org.example.handler.callback;

import lombok.RequiredArgsConstructor;
import org.example.infra.TelegramSender;
import org.example.model.Deal;
import org.example.model.User;
import org.example.repository.BalanceEventRepository;
import org.example.repository.DealRepository;
import org.example.service.DealService;
import org.example.service.ExchangeProcessor;
import org.example.ui.InlineKeyboardBuilder;
import org.example.ui.MenuService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Component
@RequiredArgsConstructor
public class ConfirmCancelCallbackHandler implements CallbackCommandHandler {

    private final ExchangeProcessor exchangeProcessor;
    private final TelegramSender telegramSender;
    private final DealRepository dealRepo;


    @Override
    public void handle(CallbackQuery query, User user) {
        long chatId = query.getMessage().getChatId();

        String[] parts = query.getData().split(":");
        if (parts.length < 2) return;

        String action = parts[0];
        long dealId  = Long.parseLong(parts[1]);
        int origMsgId = Integer.parseInt(parts[2]);

        Message message1 = (Message) query.getMessage();
        message1.getText();

        switch (action) {
            case "confirm_cancel" -> {
                Deal deal = dealRepo.findById(dealId)
                        .orElseThrow(() -> new IllegalArgumentException("Сделка не найдена: " + dealId));

                deal.setCancelledBy("%s %s %s".formatted(query.getFrom().getFirstName(), query.getFrom().getLastName(), query.getFrom().getUserName()));
                dealRepo.save(deal);

                exchangeProcessor.cancel(chatId, dealId);
                telegramSender.editMsg(chatId, origMsgId, "Сделка отменена\n" + message1.getText());
            }

            case "cancel_cancel" -> {
                // возвращаем кнопку "Отменить сделку"
                InlineKeyboardMarkup markup = InlineKeyboardBuilder.builder()
                        .button("Отменить сделку", "show_cancel:" + dealId + ":" + origMsgId)
                        .build();
                telegramSender.editReplyMarkup(chatId, query.getMessage().getMessageId(), markup);
            }
        }
    }

    @Override
    public boolean supports(String callback) {
        return callback.startsWith("confirm_cancel:") || callback.startsWith("cancel_cancel:");
    }

}

