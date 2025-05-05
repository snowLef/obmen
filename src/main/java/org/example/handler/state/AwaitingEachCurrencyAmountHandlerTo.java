package org.example.handler.state;

import lombok.RequiredArgsConstructor;
import org.example.infra.TelegramSender;
import org.example.model.CurrencyAmount;
import org.example.model.Deal;
import org.example.model.User;
import org.example.model.enums.DealType;
import org.example.model.enums.Money;
import org.example.model.enums.Status;
import org.example.service.DealService;
import org.example.service.UserService;
import org.example.ui.MenuService;
import org.example.util.MessageUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AwaitingEachCurrencyAmountHandlerTo implements UserStateHandler {

    private final TelegramSender telegramSender;
    private final UserService userService;
    private final DealService dealService;
    private final MenuService menuService;
    private final MessageUtils messageUtils;

    @Override
    public void handle(Message message, User user) {
        String text = message.getText();
        Long chatId = user.getChatId();
        Deal deal = user.getCurrentDeal();

        // Список валют, по которым нужно ввести суммы
        List<Money> currencies = deal.getMoneyTo().stream()
                .map(CurrencyAmount::getCurrency)
                .toList();

        int index = deal.getCurrentCurrencyIndex(); // индекс текущей валюты
        if (index >= currencies.size()) {
            telegramSender.sendTextWithKeyboard(chatId, "Ошибка: индекс валюты вне диапазона.");
            //menuService.sendSelectFullCurrency(chatId, "Выберите валюту");
            return;
        }

        Money currentCurrency = currencies.get(index);

        try {
            int amount = Math.round(Float.parseFloat(text));
            String formattedText = messageUtils.formatWithSpacesAndDecimals(amount);

            // Обновляем сумму по текущей валюте
            deal.getMoneyTo().stream()
                    .filter(e -> e.getCurrency().equals(currentCurrency))
                    .findFirst().orElseThrow(() -> new RuntimeException("Не найдена валюта в бд в deal.getMoneyFrom()"))
                    .setAmount(amount);

            // Переход к следующей валюте
            index++;
            if (index < currencies.size()) {
                deal.setCurrentCurrencyIndex(index);
                userService.save(user);

                if (deal.getDealType() == DealType.PLUS_MINUS) {
                    telegramSender.editMsg(chatId, user.getMessageToEdit(), "Получено: " + formattedText + " " + currentCurrency.getName());
                    telegramSender.sendTextWithKeyboard(chatId, "[+/-] Введите сумму для %s:".formatted(currencies.get(index).getName()));
                } else {
                    telegramSender.editMsg(chatId, user.getMessageToEdit(), "Получено: " + formattedText + " " + currentCurrency.getName());
                    telegramSender.sendTextWithKeyboard(chatId, "[Получено] Введите сумму для %s:".formatted(currencies.get(index).getName()));
                }

            } else if (deal.getDealType() == DealType.PLUS_MINUS) {
                telegramSender.editMsg(chatId, user.getMessageToEdit(), "Получено: " + formattedText + " " + currentCurrency.getName());
                user.pushStatus(Status.AWAITING_APPROVE);
                deal.setCurrentCurrencyIndex(0);
                user.setCurrentDeal(deal);
                userService.save(user);
                menuService.sendApproveMenu(chatId);
            } else if (deal.getDealType() == DealType.TRANSPOSITION || deal.getDealType() == DealType.INVOICE) {
                user.pushStatus(Status.AWAITING_SECOND_CURRENCY);
                deal.setCurrentCurrencyIndex(0);
                user.setCurrentDeal(deal);
                userService.save(user);
                telegramSender.editMsg(chatId, user.getMessageToEdit(), "Получено: " + formattedText + " " + currentCurrency.getName());
                Message message1 = menuService.sendSelectFullCurrency(chatId, "Выберите валюту выдачи");
                user.setMessageToEdit(message1.getMessageId());
            } else {
                user.pushStatus(Status.AWAITING_AMOUNT_FOR_EACH_CURRENCY_FROM);
                deal.setCurrentCurrencyIndex(0);
                user.setCurrentDeal(deal);
                userService.save(user);
                String nextCurrency = user.getCurrentDeal().getMoneyFrom().get(0).getCurrency().getName();
                telegramSender.sendTextWithKeyboard(chatId, "[Выдача] Введите сумму для %s:".formatted(nextCurrency));
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
