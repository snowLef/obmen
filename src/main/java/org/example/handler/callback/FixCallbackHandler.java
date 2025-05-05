package org.example.handler.callback;

import lombok.RequiredArgsConstructor;
import org.example.infra.TelegramSender;
import org.example.model.Deal;
import org.example.model.User;
import org.example.model.enums.DealStatus;
import org.example.model.enums.Status;
import org.example.repository.DealRepository;
import org.example.service.UserService;
import org.example.ui.InlineKeyboardBuilder;
import org.example.ui.MenuService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class FixCallbackHandler implements CallbackCommandHandler {

    private final DealRepository dealRepo;
    private final TelegramSender sender;
    private final UserService userService;
    private final MenuService menuService;
    private final TelegramSender telegramSender;

    @Override
    public boolean supports(String data) {
        return data != null && data.startsWith("fix:");
    }

    @Override
    public void handle(CallbackQuery query, User user) {
        long chatId = query.getMessage().getChatId();
        // Разбираем dealId и messageId из callbackData: "fix:<dealId>:<msgId>"
        String[] parts = query.getData().split(":");
        long dealId = Long.parseLong(parts[1]);
        int origMsg = parts.length >= 3
                ? Integer.parseInt(parts[2])
                : query.getMessage().getMessageId();

        Deal deal = dealRepo.findById(dealId)
                .orElseThrow(() -> new IllegalArgumentException("Сделка не найдена: " + dealId));

        // 1) Меняем статус на FIX
        deal.setStatus(DealStatus.FIX);
        dealRepo.save(deal);

        // 2) Редактируем клавиатуру: ✅ Провести / ❌ Отменить
        InlineKeyboardMarkup kb = InlineKeyboardBuilder.builder()
                .button("✅ Провести", "approve:" + dealId)
                .button("❌ Отменить", "cancel_fixed:" + dealId)
                .build();

        userService.saveUserStatus(chatId, Status.IDLE);

        String text = ((Message) query.getMessage()).getText();
        // 3) Даем явный ответ
        Message message = sender.sendWithMarkup(
                chatId,
                "*%s/%s ФИКС*\n".formatted(deal.getCreatedAt().format(DateTimeFormatter.ofPattern("MMdd")), deal.getId()) +
                        "*Баланс не изменён.*"
                        + text.replace("Подтвердить?", ""),
                kb);

        deal.setMessageId(message.getMessageId());
        dealRepo.save(deal);

        telegramSender.deleteMessages(chatId, userService.getMessageIdsToDeleteWithInit(chatId));
        menuService.sendBalance(chatId);
        menuService.sendMainMenu(chatId);
    }
}
