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

    public void sendSelectBalance(long chatId, String text) {
        List<BalanceType> balances = List.of(BalanceType.OWN, BalanceType.FOREIGN);

        List<InlineKeyboardButton> buttons = balances.stream()
                .map(x -> createButton(x.getDisplayName(), x.name()))
                .toList();

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(buttons);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        message.setReplyMarkup(addBackCancelButtons(markup));

        Message msg = telegramSender.send(message);
        userService.addMessageToDel(chatId, msg.getMessageId());
        userService.addMessageToEdit(chatId, msg.getMessageId());
    }

    public InlineKeyboardMarkup addBackCancelButtons(InlineKeyboardMarkup originalMarkup) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // Если клавиатура существовала — копируем её
        if (originalMarkup != null) {
            originalMarkup.getKeyboard();
            keyboard.addAll(originalMarkup.getKeyboard());
        }

        // Добавляем кнопки "Назад" и "Отмена"
        keyboard.add(List.of(
                InlineKeyboardButton.builder().text("◀️ Назад").callbackData("back").build(),
                InlineKeyboardButton.builder().text("❌ Отмена").callbackData("cancel").build()
        ));

        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public void sendSelectMultiplyCurrency(Long chatId, String text) {
        InlineKeyboardMarkup markup = createCurrencyKeyboard();
        Message msg = telegramSender.sendInlineKeyboard(chatId, text, addBackCancelButtons(markup));
        userService.addMessageToDel(chatId, msg.getMessageId()); // мб не нужен
        userService.addMessageToEdit(chatId, msg.getMessageId());
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
        row1.add("Купить USD");
        row1.add("Продать USD");
        keyboard.add(row1);

        // Второй ряд
        KeyboardRow row2 = new KeyboardRow();
        row2.add("Купить EUR");
        row2.add("Продать EUR");
        keyboard.add(row2);

        // Третий ряд
        KeyboardRow row3 = new KeyboardRow();
        row3.add("Купить USD (Б)");
        row3.add("Продать USD (Б)");
        keyboard.add(row3);

        // Четвертый ряд
        KeyboardRow row4 = new KeyboardRow();
        row4.add("Купить USDT");
        row4.add("Продать USDT");
        keyboard.add(row4);

        KeyboardRow customRow = new KeyboardRow();
        customRow.add("Сложный обмен");
        customRow.add("Перестановка");
        customRow.add("Invoice");
        keyboard.add(customRow);

        // Пятый ряд
        KeyboardRow row5 = new KeyboardRow();
        row5.add("+/-");
        row5.add("Перемещение");
        row5.add("Изменение");
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
        Deal deal = user.getCurrentDeal();
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setParseMode("MarkdownV2");

        if (deal.getDealType() == DealType.CHANGE_BALANCE) {
            message.setText("""
                            Подтвердить?
                            Имя: %s
                            Операция: %s
                            Сумма: %s %s
                            """.formatted(
                            deal.getBuyerName(),
                            user.getChangeBalanceType().getType(),
                            deal.getMoneyTo().get(0).getAmount(), deal.getMoneyTo().get(0).getCurrency().getName()
                    )
            );
        } else if (deal.getDealType() == DealType.CUSTOM) {
            message.setText(messageUtils.escapeMarkdown("""
                                    Подтвердить?
                                    *%s*
                                    Имя: %s
                                    Получено: *%s %s*
                                    Курс: %s
                                    Выдано: *%s %s*
                                    """.formatted(
                                    deal.getDealType().getType(),
                                    deal.getBuyerName(),
                                    messageUtils.formatWithSpacesAndDecimals(deal.getMoneyTo().get(0).getAmount()), deal.getMoneyTo().get(0).getCurrency(),
                                    deal.getExchangeRate(),
                                    messageUtils.formatWithSpacesAndDecimals(deal.getMoneyFrom().get(0).getAmount()), deal.getMoneyFrom().get(0).getCurrency()
                            )
                    )
            );
        } else {
            message.setText(messageUtils.escapeMarkdown("""
                                    Подтвердить?
                                    *%s %s*
                                    Имя: %s
                                    Получено: *%s %s*
                                    Курс: %s
                                    Выдано: *%s %s*
                                    """.formatted(
                                    deal.getDealType().getType(), deal.getMoneyTo().get(0).getCurrency().getNameForBalance(),
                                    deal.getBuyerName(),
                                    messageUtils.formatWithSpacesAndDecimals(deal.getMoneyTo().get(0).getAmount()), deal.getMoneyTo().get(0).getCurrency(),
                                    deal.getExchangeRate(),
                                    messageUtils.formatWithSpacesAndDecimals(deal.getMoneyFrom().get(0).getAmount()), deal.getMoneyFrom().get(0).getCurrency()
                            )
                    )
            );
        }

        List<InlineKeyboardButton> row = List.of(
                createButton("Да ✅", BotCommands.APPROVE_YES)
//                ,
//                createButton("Нет", BotCommands.APPROVE_NO)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(row));
        message.setReplyMarkup(addBackCancelButtons(markup));

        Message message1 = telegramSender.send(message);
        userService.addMessageToDel(chatId, message1.getMessageId());
    }

    public void sendBalance(long chatId) {
//        StringBuilder own = new StringBuilder("\"> __**НАШ БАЛАНС:**__\\n\"");
//        StringBuilder foreign = new StringBuilder("\\n> __**ЧУЖОЙ БАЛАНС:**__\\n");
//        StringBuilder debt = new StringBuilder("\\n> __**ДОЛГ:**__\\n");
//
//        for (Money currency : Money.values()) {
//            for (BalanceType type : BalanceType.values()) {
//                long amount = currencyService.getBalance(currency, type);
//                if (amount > 0) {
//                    String formattedAmount = messageUtils.formatWithSpacesAndDecimals(String.valueOf(amount));
//                    String line = "%s: %s\n".formatted(currency.getNameForBalance(), formattedAmount);
//                    switch (type) {
//                        case OWN -> own.append(line);
//                        case FOREIGN -> foreign.append(line);
//                        case DEBT -> debt.append(line);
//                    }
//                }
//            }
//        }
//
//        StringBuilder result = new StringBuilder();
//        if (own.length() > "Баланс наш:\n".length()) result.append(own).append("\n");
//        if (foreign.length() > "Баланс чужой:\n".length()) result.append(foreign).append("\n");
//        if (debt.length() > "Долг:\n".length()) result.append(debt);

        messageUtils.sendFormattedText(chatId, formatBalances());
    }

    private String formatBalances() {
        StringBuilder message = new StringBuilder();

        // Заголовки (жирный, подчёркнутый, верхний регистр)
        message.append("> __*НАШ БАЛАНС:*__\n");
        appendCurrencyLines(message, BalanceType.OWN);

        message.append("\n> __*ЧУЖОЙ БАЛАНС:*__\n");
        appendCurrencyLines(message, BalanceType.FOREIGN);

        message.append("\n> __*ДОЛГ:*__\n");
        appendCurrencyLines(message, BalanceType.DEBT);

        return message.toString();
    }

    private void appendCurrencyLines(StringBuilder builder, BalanceType type) {
        for (Money currency : Money.values()) {
            long amount = currencyService.getBalance(currency, type);
            if (amount > 0) {
                String formattedAmount = messageUtils.formatWithSpacesAndDecimals(amount);
                // Валюта жирная, сумма обычная
                builder.append("> *")
                        .append(currency.name().toUpperCase()) // или getNameForBalance()
                        .append(":* ")
                        .append(formattedAmount)
                        .append("\n");
            }
        }
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
        message.setReplyMarkup(addBackCancelButtons(markup));

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
        message.setReplyMarkup(addBackCancelButtons(markup));

        Message msg = telegramSender.send(message);
        userService.addMessageToDel(chatId, msg.getMessageId());
        userService.addMessageToEdit(chatId, msg.getMessageId());
        return msg;
    }

    public void sendSelectAmountType(long chatId) {
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
        message.setReplyMarkup(addBackCancelButtons(markup));
        Message msg = telegramSender.send(message);
        userService.addMessageToEdit(chatId, msg.getMessageId());
        userService.addMessageToDel(chatId, msg.getMessageId());
    }

    public void sendSelectCurrencyType(long chatId) {
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
        message.setReplyMarkup(addBackCancelButtons(markup));
        Message msgToReturn = telegramSender.send(message);
        userService.addMessageToEdit(chatId, msgToReturn.getMessageId());
        userService.addMessageToDel(chatId, msgToReturn.getMessageId());
    }

    public void sendBalanceMovedMessage(long chatId) {
        User user = userService.getUser(chatId);
        Deal deal = user.getCurrentDeal();
        String balanceFrom = user.getBalanceFrom().getDisplayName();
        String balanceTo = user.getBalanceTo().getDisplayName();
        telegramSender.sendText(chatId, """
                Баланс изменен ✅
                %s -> %s
                Сумма: %s %s
                """.formatted(
                balanceFrom, balanceTo,
                deal.getMoneyTo().get(0).getAmount(), deal.getMoneyTo().get(0).getCurrency()));
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
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setParseMode("MarkdownV2");
        message.setChatId(chatId);

        if (deal.getDealType() == DealType.CUSTOM) {
            message.setText(messageUtils.escapeMarkdown("""
                                    Сделка завершена ✅
                                    *%s*
                                    Имя: %s
                                    Получено: *%s %s*
                                    Курс: %s
                                    Выдано: *%s %s*
                                    """.formatted(
                                    deal.getDealType().getType(),
                                    deal.getBuyerName(),
                                    messageUtils.formatWithSpacesAndDecimals(deal.getMoneyTo().get(0).getAmount()), deal.getMoneyTo().get(0).getCurrency(),
                                    deal.getExchangeRate(),
                                    messageUtils.formatWithSpacesAndDecimals(deal.getMoneyFrom().get(0).getAmount()), deal.getMoneyFrom().get(0).getCurrency()
                            )
                    )
            );
        } else {
            message.setText(messageUtils.escapeMarkdown("""
                                    Сделка завершена ✅
                                    *%s %s*
                                    Имя: %s
                                    Получено: *%s %s*
                                    Курс: %s
                                    Выдано: *%s %s*
                                    """.formatted(
                                    deal.getDealType().getType(), deal.getMoneyFrom().get(0).getCurrency().getNameForBalance(),
                                    deal.getBuyerName(),
                                    messageUtils.formatWithSpacesAndDecimals(deal.getMoneyTo().get(0).getAmount()), deal.getMoneyTo().get(0).getCurrency(),
                                    deal.getExchangeRate(),
                                    messageUtils.formatWithSpacesAndDecimals(deal.getMoneyFrom().get(0).getAmount()), deal.getMoneyFrom().get(0).getCurrency()
                            )
                    )
            );
        }

        telegramSender.send(message);
    }

    public void sendEnterExchangeRate(long chatId) {
        telegramSender.sendTextWithKeyboard(chatId, "Введите курс:");
    }

}
