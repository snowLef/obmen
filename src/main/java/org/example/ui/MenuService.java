package org.example.ui;

import lombok.RequiredArgsConstructor;
import org.example.constants.BotCommands;
import org.example.model.*;
import org.example.model.enums.BalanceType;
import org.example.model.enums.ChangeBalanceType;
import org.example.model.enums.DealType;
import org.example.model.enums.Money;
import org.example.service.CurrencyService;
import org.example.infra.TelegramSender;
import org.example.service.UserService;
import org.example.util.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.model.enums.Money.*;

@Service
@RequiredArgsConstructor
public class MenuService {

    private UserService userService;
    private CurrencyService currencyService;
    private MessageUtils messageUtils;
    private TelegramSender telegramSender;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setCurrencyService(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @Autowired
    public void setMessageUtils(MessageUtils messageUtils) {
        this.messageUtils = messageUtils;
    }

    @Autowired
    public void setTelegramSender(TelegramSender telegramSender) {
        this.telegramSender = telegramSender;
    }

    public String formatSelectedCurrencies(List<Money> currencies) {
        return currencies.stream()
                .map(Money::getName)
                .collect(Collectors.joining(" + "));
    }

    public String getEnterAmountMessage(Money currency) {
        return "[Получение] Введите сумму для " + currency.getName() + ":";
    }

    public String getSelectedCurrenciesMessage(String prefix, List<Money> currencies) {
        return "[" + prefix + "] Выбрано: " + formatSelectedCurrencies(currencies);
    }

    public Message sendSelectMultiplyCurrency(Long chatId, String text) {
        InlineKeyboardMarkup markup = createCurrencyKeyboard();
        Message msg = telegramSender.sendInlineKeyboard(chatId, text, markup);
        userService.addMessageToDel(chatId, msg.getMessageId()); // мб не нужен
        userService.addMessageToEdit(chatId, msg.getMessageId());
        return msg;
    }

    public InlineKeyboardMarkup createCurrencyKeyboard() {
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        for (Money money : Money.values()) {
            if (money != RUB) {
                buttons.add(InlineKeyboardButton.builder()
                        .text(money.getName())
                        .callbackData(money.getName())
                        .build());
            }
        }

        buttons.add(InlineKeyboardButton.builder()
                .text("✅ Готово")
                .callbackData("done")
                .build());

        return InlineKeyboardMarkup.builder().keyboard(List.of(buttons)).build();
    }

    public void sendChangeBalanceMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите опцию:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        // Первый ряд
        KeyboardRow row1 = new KeyboardRow();
        row1.add(ChangeBalanceType.GET.getType());
        row1.add(ChangeBalanceType.GIVE.getType());
        keyboard.add(row1);

        // Второй ряд
        KeyboardRow row2 = new KeyboardRow();
        row2.add(ChangeBalanceType.LEND.getType());
        row2.add(ChangeBalanceType.DEBT_REPAYMENT.getType());
        keyboard.add(row2);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true); // Опционально: подгоняет размер кнопок
        keyboardMarkup.setOneTimeKeyboard(true); // Опционально: скрывает клавиатуру после использования

        message.setReplyMarkup(keyboardMarkup);

        Message msg = telegramSender.send(message);
        userService.addMessageToDel(chatId, msg.getMessageId());
    }

    public void sendMainMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите опцию:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        // Первый ряд
        KeyboardRow row1 = new KeyboardRow();
        row1.add("Купить Доллар");
        row1.add("Продать Доллар");
        keyboard.add(row1);

        // Второй ряд
        KeyboardRow row2 = new KeyboardRow();
        row2.add("Купить Евро");
        row2.add("Продать Евро");
        keyboard.add(row2);

        // Третий ряд
        KeyboardRow row3 = new KeyboardRow();
        row3.add("Купить Белый Доллар");
        row3.add("Продать Белый Доллар");
        keyboard.add(row3);

        // Четвертый ряд
        KeyboardRow row4 = new KeyboardRow();
        row4.add("Купить Tether");
        row4.add("Продать Tether");
        keyboard.add(row4);

        KeyboardRow customRow = new KeyboardRow();
        customRow.add("Сложный обмен");
        customRow.add("Перестановка");
        customRow.add("Invoice");
        keyboard.add(customRow);

        // Пятый ряд
        KeyboardRow row5 = new KeyboardRow();
        row5.add("+/-");
        row5.add("Баланс");
        keyboard.add(row5);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true); // Опционально: подгоняет размер кнопок
        keyboardMarkup.setOneTimeKeyboard(false); // Опционально: скрывает клавиатуру после использования

        message.setReplyMarkup(keyboardMarkup);

        Message msg = telegramSender.send(message);
        userService.addMessageToDel(chatId, msg.getMessageId());
    }

    public void sendApproveMenu(long chatId) {
        User user = userService.getUser(chatId);
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        if (user.getCurrentDeal().getDealType() == DealType.CHANGE_BALANCE) {
            message.setText("""
                            Подтвердить?
                            Имя: %s
                            Операция: %s
                            Сумма: %s %s
                            """.formatted(
                            user.getCurrentDeal().getBuyerName(),
                            user.getChangeBalanceType().getType(),
                            user.getCurrentDeal().getMoneyTo().get(0).getAmount(), user.getCurrentDeal().getMoneyTo().get(0).getCurrency().getName()
                    )
            );
        } else {
            message.setText("""
                            Подтвердить?
                            Имя: %s
                            Сумма получения: %s %s
                            Курс: %s
                            Сумма выдачи: %s %s
                            """.formatted(
                            user.getCurrentDeal().getBuyerName(),
                            user.getCurrentDeal().getMoneyTo().get(0).getAmount(), user.getCurrentDeal().getMoneyTo().get(0).getCurrency().getName(),
                            user.getCurrentDeal().getExchangeRate(),
                            Math.round(user.getCurrentDeal().getMoneyFrom().get(0).getAmount()), user.getCurrentDeal().getMoneyFrom().get(0).getCurrency().getName()
                    )
            );
        }

        List<InlineKeyboardButton> row = List.of(
                createButton("Да", BotCommands.APPROVE_YES),
                createButton("Нет", BotCommands.APPROVE_NO)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(row));
        message.setReplyMarkup(markup);

        Message msg = telegramSender.send(message);
        userService.addMessageToDel(chatId, msg.getMessageId());
    }

    public void sendBalance(long chatId) {
        StringBuilder own = new StringBuilder("Баланс наш:\n");
        StringBuilder foreign = new StringBuilder("Баланс чужой:\n");
        StringBuilder debt = new StringBuilder("Долг:\n");

        for (Money currency : Money.values()) {
            for (BalanceType type : BalanceType.values()) {
                double amount = currencyService.getBalance(currency, type);
                if (amount > 0) {
                    String formattedAmount = messageUtils.formatWithSpacesAndDecimals(String.valueOf(amount));
                    String line = "%s: %s\n".formatted(currency.getNameForBalance(), formattedAmount);
                    switch (type) {
                        case OWN -> own.append(line);
                        case FOREIGN -> foreign.append(line);
                        case DEBT -> debt.append(line);
                    }
                }
            }
        }

        StringBuilder result = new StringBuilder();
        if (own.length() > "Баланс наш:\n".length()) result.append(own).append("\n");
        if (foreign.length() > "Баланс чужой:\n".length()) result.append(foreign).append("\n");
        if (debt.length() > "Долг:\n".length()) result.append(debt);

        telegramSender.sendText(chatId, result.toString().trim());
    }

    public void sendTranspositionOrInvoiceApprove(long chatId) {
        User user = userService.getUser(chatId);
        Deal deal = user.getCurrentDeal();
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        String dealType = deal.getDealType().getType();

        // Формируем строку для получения валют
        StringBuilder receivedCurrencies = new StringBuilder();
        // Формируем строку для выдачи валют
        StringBuilder issuedCurrencies = new StringBuilder();

        for (int i = 0; i < deal.getMoneyFrom().size(); i++) {
            CurrencyAmount moneyFrom = deal.getMoneyFrom().get(i);

            // Добавляем валюту и сумму выдачи
            if (!issuedCurrencies.isEmpty()) {
                issuedCurrencies.append(" + ");
            }
            issuedCurrencies.append(String.format("%s %s", moneyFrom.getAmount(), moneyFrom.getCurrency().getName()));
        }

        for (int i = 0; i < deal.getMoneyTo().size(); i++) {
            CurrencyAmount moneyTo = deal.getMoneyTo().get(i);

            // Добавляем валюту и сумму получения
            if (!receivedCurrencies.isEmpty()) {
                receivedCurrencies.append(" + ");
            }
            receivedCurrencies.append(String.format("%s %s", moneyTo.getAmount(), moneyTo.getCurrency().getName()));
        }

        message.setText("""
                Подтвердить?
                %s
                Клиент: %s
                %s
                Получено: %s
                Выдано: %s
                """.formatted(
                dealType,
                deal.getBuyerName(),
                deal.getCityFromTo(),
                receivedCurrencies,
                issuedCurrencies
                )
        );

        List<InlineKeyboardButton> row = List.of(
                createButton("Да", BotCommands.APPROVE_YES),
                createButton("Нет", BotCommands.APPROVE_NO)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(row));
        message.setReplyMarkup(markup);

        Message msg = telegramSender.send(message);
        userService.addMessageToDel(chatId, msg.getMessageId());
    }

    public void sendTranspositionOrInvoiceComplete(long chatId) {
        User user = userService.getUser(chatId);
        Deal deal = user.getCurrentDeal();
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        String dealType = deal.getDealType().getType();

        // Формируем строку для получения валют
        StringBuilder receivedCurrencies = new StringBuilder();
        // Формируем строку для выдачи валют
        StringBuilder issuedCurrencies = new StringBuilder();

        for (int i = 0; i < deal.getMoneyFrom().size(); i++) {
            CurrencyAmount moneyFrom = deal.getMoneyFrom().get(i);

            // Добавляем валюту и сумму выдачи
            if (!issuedCurrencies.isEmpty()) {
                issuedCurrencies.append(" + ");
            }
            issuedCurrencies.append(String.format("%s %s", moneyFrom.getAmount(), moneyFrom.getCurrency().getName()));
        }

        for (int i = 0; i < deal.getMoneyTo().size(); i++) {
            CurrencyAmount moneyTo = deal.getMoneyTo().get(i);

            // Добавляем валюту и сумму получения
            if (!receivedCurrencies.isEmpty()) {
                receivedCurrencies.append(" + ");
            }
            receivedCurrencies.append(String.format("%s %s", moneyTo.getAmount(), moneyTo.getCurrency().getName()));
        }

        message.setText("""
                Перестановка успешно проведена
                %s
                Клиент: %s
                %s
                Получено: %s
                Выдано: %s
                """.formatted(
                        dealType,
                        deal.getBuyerName(),
                        deal.getCityFromTo(),
                        receivedCurrencies,
                        issuedCurrencies
                )
        );

        telegramSender.send(message);
    }


    private InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    public Message sendSelectCurrency(long chatId, String text) {
        List<Money> currencies = null;
        if (userService.getUser(chatId).getCurrentDeal().getDealType() == DealType.CHANGE_BALANCE) {
            currencies = Arrays.stream(values()).toList();
        } else {
            currencies = List.of(USDT, USD, EUR, USDW, YE);
        }
        List<InlineKeyboardButton> buttons = currencies.stream()
                .map(x -> createButton(x.getName(), x.getName()))
                .toList();

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(buttons);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);

        Message msg = telegramSender.send(message);
        userService.addMessageToDel(chatId, msg.getMessageId());
        userService.addMessageToEdit(chatId, msg.getMessageId());
        return msg;
    }

    public Message sendSelectAmountType(long chatId) {
        User user = userService.getUser(chatId);
        InlineKeyboardButton buttonGive = createButton("%s %s Забрать"
                        .formatted(
                                user.getCurrentDeal().getCurrentAmount(),
                                user.getCurrentDeal().getMoneyTo().get(0).getCurrency()
                        ),
                "receive");
        InlineKeyboardButton buttonReceive = createButton("%s %s Отдать"
                        .formatted(
                                user.getCurrentDeal().getCurrentAmount(),
                                user.getCurrentDeal().getMoneyFrom().get(0).getCurrency()
                        ),
                "give");

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Какую сумму хотите ввести?");

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(
                List.of(
                        buttonGive, buttonReceive
                )
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);

        return telegramSender.send(message);
    }

    public Message sendSelectCurrencyType(long chatId) {
        InlineKeyboardButton buttonGive = createButton("/курс", "division");
        InlineKeyboardButton buttonReceive = createButton("*курс", "multiplication");

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Формула расчета");

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(
                List.of(
                        buttonGive, buttonReceive
                )
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);

        return telegramSender.send(message);
    }

    public void sendBalanceChangedMessage(long chatId) {
        User user = userService.getUser(chatId);
        Deal deal = user.getCurrentDeal();
        String changeType = user.getChangeBalanceType().getType();
        telegramSender.sendText(chatId, """
                Баланс изменен ✅
                Имя: %s
                %s
                Сумма: %s %s
                """.formatted(
                deal.getBuyerName(),
                changeType,
                deal.getMoneyTo().get(0).getAmount(), deal.getMoneyTo().get(0).getCurrency()));
    }

    public void sendDealCompletedMessage(long chatId) {
        Deal deal = userService.getUser(chatId).getCurrentDeal();
        telegramSender.sendText(chatId, """
                Сделка завершена ✅
                Имя: %s
                Сумма получена: %s %s
                Курс: %s
                Сумма выдана: %s %s
                """.formatted(
                deal.getBuyerName(),
                Math.round(deal.getMoneyTo().get(0).getAmount()), deal.getMoneyTo().get(0).getCurrency(),
                deal.getExchangeRate(),
                Math.round(deal.getMoneyFrom().get(0).getAmount()), deal.getMoneyFrom().get(0).getCurrency()));
    }

    public Message sendEnterExchangeRate(long chatId) {
        return telegramSender.sendText(chatId, "Введите курс:");
    }

}
