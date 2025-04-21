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
public class AwaitingEachCurrencyAmountHandlerFrom implements UserStateHandler {

    private final TelegramSender telegramSender;
    private final UserService userService;
    private final DealService dealService;
    private final MenuService menuService;

    @Override
    public void handle(Message message, User user) {
        String text = message.getText();
        Long chatId = user.getChatId();
        Deal deal = user.getCurrentDeal();

        // Список валют, по которым нужно ввести суммы
        List<Money> currencies = deal.getMoneyFrom().stream()
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
            double amount = Double.parseDouble(text);

            // Обновляем сумму по текущей валюте
//            deal.getMoneyFrom().removeIf(ca -> ca.getCurrency() == currentCurrency);
//            deal.getMoneyFrom().add(new CurrencyAmount(currentCurrency, amount));
            deal.getMoneyFrom().stream()
                    .filter(e -> e.getCurrency().equals(currentCurrency))
                    .findFirst().orElseThrow(() -> new RuntimeException("Не найдена валюта в бд в deal.getMoneyFrom()"))
                    .setAmount(amount);

            // Переход к следующей валюте
            index++;
            if (index < currencies.size()) {
                user.setCurrentCurrencyIndex(index);
                userService.save(user);

                Message message1 = telegramSender.sendText(chatId, "[Выдача] Введите сумму для %s:".formatted(currencies.get(index).getName()));
                userService.addMessageToDel(chatId, message1.getMessageId());
            } else {
                user.setStatus(Status.AWAITING_APPROVE);
                user.setCurrentCurrencyIndex(0);
                userService.save(user);

                menuService.sendTranspositionOrInvoiceApprove(chatId);
            }

            dealService.save(deal);

        } catch (NumberFormatException e) {
            telegramSender.sendText(chatId, "Некорректная сумма. Попробуйте еще раз:");
        }
        userService.addMessageToDel(chatId, message.getMessageId());
    }

    @Override
    public Status getSupportedStatus() {
        return Status.AWAITING_AMOUNT_FOR_EACH_CURRENCY_FROM;
    }

    private void askNextCurrency(Long chatId, Money currency) {
        telegramSender.sendText(chatId, "Введите сумму для " + currency.getName() + ":");
    }
}
