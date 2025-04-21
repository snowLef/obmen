package org.example.handler.callback;

import lombok.RequiredArgsConstructor;
import org.example.model.User;
import org.example.service.ExchangeProcessor;
import org.example.ui.MenuService;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApproveCallbackHandler implements CallbackCommandHandler {

    private final ExchangeProcessor exchangeProcessor;
    private final MenuService menuService;

    @Override
    public boolean supports(String data) {
        return "yes".equals(data);
    }

    @Override
    public void handle(CallbackQuery query, User user) {
        exchangeProcessor.approve(user.getChatId());
        menuService.sendMainMenu(user.getChatId());
    }
}