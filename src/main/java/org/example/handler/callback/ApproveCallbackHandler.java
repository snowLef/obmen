package org.example.handler.callback;

import lombok.RequiredArgsConstructor;
import org.example.model.Deal;
import org.example.model.User;
import org.example.repository.DealRepository;
import org.example.service.ExchangeProcessor;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.springframework.stereotype.Component;

import static org.example.constants.BotCommands.APPROVE;

@Component
@RequiredArgsConstructor
public class ApproveCallbackHandler implements CallbackCommandHandler {

    private final ExchangeProcessor exchangeProcessor;
    private final DealRepository dealRepo;

    @Override
    public boolean supports(String data) {
        return data.startsWith(APPROVE);
    }

    @Override
    public void handle(CallbackQuery query, User user) {
        // Разбираем dealId и messageId из callbackData: "fix:<dealId>:<msgId>"
        String[] parts = query.getData().split(":");
        long dealId = Long.parseLong(parts[1]);

        Deal deal = dealRepo.findById(dealId)
                .orElseThrow(() -> new IllegalArgumentException("Сделка не найдена: " + dealId));

        exchangeProcessor.approve(user.getChatId(), deal);
    }

}