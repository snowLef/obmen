package org.example.handler.callback;

import lombok.RequiredArgsConstructor;
import org.example.infra.TelegramSender;
import org.example.model.Deal;
import org.example.model.User;
import org.example.model.enums.DealStatus;
import org.example.repository.DealRepository;
import org.example.service.ExchangeProcessor;
import org.example.ui.InlineKeyboardBuilder;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.springframework.stereotype.Component;

import static org.example.constants.BotCommands.APPROVE;

@Component
@RequiredArgsConstructor
public class ApproveCallbackHandler implements CallbackCommandHandler {

    private final ExchangeProcessor exchangeProcessor;
    private final DealRepository dealRepo;
    private final TelegramSender telegramSender;

    @Override
    public boolean supports(String data) {
        return data.startsWith(APPROVE);
    }

    @Override
    public void handle(CallbackQuery query, User user) {
        // Разбираем dealId и messageId из callbackData: "fix:<dealId>:<msgId>"
        String[] parts = query.getData().split(":");
        long dealId = Long.parseLong(parts[1]);

        long chatId = query.getMessage().getChatId();

        Deal deal = dealRepo.findById(dealId)
                .orElseThrow(() -> new IllegalArgumentException("Сделка не найдена: " + dealId));


        if (deal.getStatus() == DealStatus.FIX) {
            // убираем inline-кнопки
            telegramSender.editReplyMarkup(user.getChatId(), deal.getMessageId(),
                    InlineKeyboardBuilder.builder().build()
            );
        }

        if (deal.getStatus() != DealStatus.APPLIED) {
            exchangeProcessor.approve(chatId, deal);
            deal.setApprovedBy("%s %s %s".formatted(query.getFrom().getFirstName(), query.getFrom().getLastName(), query.getFrom().getUserName()));
            dealRepo.save(deal);
        } else {
            telegramSender.sendText(chatId, "Ошибка! Сделка уже проведена!");
        }

    }

}