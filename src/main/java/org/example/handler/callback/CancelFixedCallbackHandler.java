package org.example.handler.callback;

import lombok.RequiredArgsConstructor;
import org.example.infra.TelegramSender;
import org.example.model.Deal;
import org.example.model.User;
import org.example.model.enums.DealStatus;
import org.example.repository.DealRepository;
import org.example.ui.InlineKeyboardBuilder;
import org.example.ui.MenuService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class CancelFixedCallbackHandler implements CallbackCommandHandler {

    private final DealRepository dealRepo;
    private final TelegramSender sender;
    private final MenuService menuService;

    @Override
    public boolean supports(String data) {
        return data != null && data.startsWith("cancel_fixed:");
    }

    @Override
    public void handle(CallbackQuery query, User user) {
        String[] parts = query.getData().split(":");
        long dealId = Long.parseLong(parts[1]);
        int origMsg = parts.length >= 3
                ? Integer.parseInt(parts[2])
                : query.getMessage().getMessageId();

        Deal deal = dealRepo.findById(dealId)
                .orElseThrow(() -> new IllegalArgumentException("Сделка не найдена: " + dealId));

        deal.setStatus(DealStatus.CANCELLED);
        dealRepo.save(deal);

        // убираем inline-кнопки
        sender.editReplyMarkup(user.getChatId(), deal.getMessageId(),
                InlineKeyboardBuilder.builder().build()
        );

        sender.sendText(user.getChatId(), "*%s/%s ФИКС отменен*".formatted(deal.getCreatedAt().format(DateTimeFormatter.ofPattern("MMdd")), deal.getId()));
        menuService.sendMainMenu(user.getChatId());
    }
}

