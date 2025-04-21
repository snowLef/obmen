package org.example.handler.callback;

import lombok.RequiredArgsConstructor;
import org.example.model.User;
import org.example.service.ExchangeProcessor;
import org.example.ui.MenuService;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NoCallbackHandler implements CallbackCommandHandler {

    private final ExchangeProcessor exchangeProcessor;
    private final MenuService menuService;

    @Override
    public boolean supports(String data) {
        return "no".equals(data);
    }

    @Override
    public void handle(CallbackQuery query, User user) {
        exchangeProcessor.cancel(user.getChatId());
        menuService.sendMainMenu(user.getChatId());
    }
}
