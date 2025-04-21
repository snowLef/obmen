package org.example.handler.state;

import lombok.RequiredArgsConstructor;
import org.example.infra.TelegramSender;
import org.example.model.CurrencyAmount;
import org.example.model.Deal;
import org.example.model.User;
import org.example.model.enums.Money;
import org.example.model.enums.Status;
import org.example.service.DealService;
import org.example.service.UserService;
import org.example.ui.MenuService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AwaitingEachCurrencyAmountHandlerTo implements UserStateHandler {

    private final TelegramSender telegramSender;
    private final UserService userService;
    private final DealService dealService;

    @Override
    public void handle(Message message, User user) {
        String text = message.getText();
        Long chatId = user.getChatId();
        Deal deal = user.getCurrentDeal();

        // Список валют, по которым нужно ввести суммы
        List<Money> currencies = deal.getMoneyTo().stream()
                .map(CurrencyAmount::getCurrency)
                .toList();

        Integer index = user.getCurrentCurrencyIndex(); // индекс текущей валюты
        if (index >= currencies.size()) {
            telegramSender.sendText(chatId, "Ошибка: индекс валюты вне диапазона.");
            return;
        }

        Money currentCurrency = currencies.get(index);

        // Парсим введённую сумму
        try {
            int amount = Math.round(Float.parseFloat(text));

            // Обновляем сумму по текущей валюте
            deal.getMoneyTo().stream()
                    .filter(e -> e.getCurrency().equals(currentCurrency))
                    .findFirst().orElseThrow(() -> new RuntimeException("Не найдена валюта в бд в deal.getMoneyFrom()"))
                    .setAmount(amount);

            // Переход к следующей валюте
            index++;
            if (index < currencies.size()) {
                user.setCurrentCurrencyIndex(index);
                userService.save(user);

                telegramSender.sendText(chatId, "[Получение] Введите сумму для %s:".formatted(currencies.get(index).getName()));
            } else {
                user.pushStatus(Status.AWAITING_AMOUNT_FOR_EACH_CURRENCY_FROM);
                user.setCurrentCurrencyIndex(0);
                userService.save(user);
                String nextCurrency = user.getCurrentDeal().getMoneyFrom().get(0).getCurrency().getName();
                telegramSender.sendText(chatId, "[Выдача] Введите сумму для %s:".formatted(nextCurrency));
            }

            dealService.save(deal);

        } catch (NumberFormatException e) {
            telegramSender.sendText(chatId, "Некорректная сумма. Попробуйте еще раз:");
        }
        userService.addMessageToDel(chatId, message.getMessageId());
    }

    @Override
    public Status getSupportedStatus() {
        return Status.AWAITING_AMOUNT_FOR_EACH_CURRENCY_TO;
    }

}
