package org.example.ui;

import lombok.RequiredArgsConstructor;
import org.example.constants.BotCommands;
import org.example.model.*;
import org.example.model.enums.*;
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

    public void sendChangeBalanceMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите опцию:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        // Первый ряд
        KeyboardRow row1 = new KeyboardRow();
        row1.add(ChangeBalanceType.ADD.getType());
        row1.add(ChangeBalanceType.WITHDRAWAL.getType());
        keyboard.add(row1);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true); // Опционально: подгоняет размер кнопок
        keyboardMarkup.setOneTimeKeyboard(true); // Опционально: скрывает клавиатуру после использования

        message.setReplyMarkup(keyboardMarkup);

        Message msg = telegramSender.send(message);
        userService.addMessageToDel(chatId, msg.getMessageId());
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

    public InlineKeyboardMarkup createFullCurrencyKeyboard() {
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        for (Money money : Money.values()) {
            buttons.add(InlineKeyboardButton.builder()
                    .text(money.getName())
                    .callbackData(money.getName())
                    .build());
        }

        buttons.add(InlineKeyboardButton.builder()
                .text("✅ Готово")
                .callbackData("done")
                .build());

        return InlineKeyboardMarkup.builder().keyboard(List.of(buttons)).build();
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

    public void sendPlusMinusMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите опцию:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        // Первый ряд
        KeyboardRow row1 = new KeyboardRow();
        row1.add(PlusMinusType.GET.getType());
        row1.add(PlusMinusType.GIVE.getType());
        keyboard.add(row1);

        // Второй ряд
        KeyboardRow row2 = new KeyboardRow();
        row2.add(PlusMinusType.LEND.getType());
        row2.add(PlusMinusType.DEBT_REPAYMENT.getType());
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
        customRow.add("Валютный обмен");
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
            issuedCurrencies.append(String.format("*%s %s*", moneyFrom.getAmount(), moneyFrom.getCurrency().getName()));
        }

        for (int i = 0; i < deal.getMoneyTo().size(); i++) {
            CurrencyAmount moneyTo = deal.getMoneyTo().get(i);

            // Добавляем валюту и сумму получения
            if (!receivedCurrencies.isEmpty()) {
                receivedCurrencies.append(" + ");
            }
            receivedCurrencies.append(String.format("*%s %s*", moneyTo.getAmount(), moneyTo.getCurrency().getName()));
        }

        if (deal.getDealType() == DealType.PLUS_MINUS) {
            message.setText(messageUtils.escapeMarkdown("""
                                    Подтвердить?
                                    *%s*
                                    Имя: %s
                                    Сумма: %s %s
                                    """.formatted(
                                    user.getPlusMinusType().getType().toUpperCase(),
                                    deal.getBuyerName(),
                                    receivedCurrencies, issuedCurrencies
                            )
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
        } else if (deal.getDealType() == DealType.MOVING_BALANCE) {
            message.setText(messageUtils.escapeMarkdown("""
                                    Подтвердить?
                                    *%s*
                                    %s - %s
                                    Сумма: *%s %s*
                                    """.formatted(
                                    deal.getDealType().getType(),
                                    user.getBalanceFrom().getDisplayName(), user.getBalanceTo().getDisplayName(),
                                    messageUtils.formatWithSpacesAndDecimals(deal.getMoneyTo().get(0).getAmount()), deal.getMoneyTo().get(0).getCurrency()
                            )
                    )
            );
        } else if (deal.getDealType() == DealType.BUY) {
            message.setText(messageUtils.escapeMarkdown("""
                                    Подтвердить?
                                    *%s %s*
                                    Имя: %s
                                    Получено: *%s %s*
                                    Курс: %s
                                    Выдано: *%s %s*
                                    """.formatted(
                                    deal.getDealType().getType(), deal.getMoneyTo().get(0).getCurrency().getName(),
                                    deal.getBuyerName(),
                                    messageUtils.formatWithSpacesAndDecimals(deal.getMoneyTo().get(0).getAmount()), deal.getMoneyTo().get(0).getCurrency(),
                                    deal.getExchangeRate(),
                                    messageUtils.formatWithSpacesAndDecimals(deal.getMoneyFrom().get(0).getAmount()), deal.getMoneyFrom().get(0).getCurrency()
                            )
                    )
            );
        } else if (deal.getDealType() == DealType.SELL) {
            message.setText(messageUtils.escapeMarkdown("""
                                    Подтвердить?
                                    *%s %s*
                                    Имя: %s
                                    Получено: *%s %s*
                                    Курс: %s
                                    Выдано: *%s %s*
                                    """.formatted(
                                    deal.getDealType().getType(), deal.getMoneyFrom().get(0).getCurrency().getName(),
                                    deal.getBuyerName(),
                                    messageUtils.formatWithSpacesAndDecimals(deal.getMoneyTo().get(0).getAmount()), deal.getMoneyTo().get(0).getCurrency(),
                                    deal.getExchangeRate(),
                                    messageUtils.formatWithSpacesAndDecimals(deal.getMoneyFrom().get(0).getAmount()), deal.getMoneyFrom().get(0).getCurrency()
                            )
                    )
            );
        } else if (deal.getDealType() == DealType.CHANGE_BALANCE) {
            String type = user.getChangeBalanceType() == ChangeBalanceType.ADD ? "Получено" : "Выдано";
            long amount = user.getChangeBalanceType() == ChangeBalanceType.ADD ? deal.getMoneyTo().get(0).getAmount() : deal.getMoneyFrom().get(0).getAmount();
            message.setText(messageUtils.escapeMarkdown("""
                                    Подтвердить?
                                    *ИЗМЕНЕНИЕ БАЛАНСА*
                                    %s *%s*
                                    %s *%s %s*
                                    Комментарий: %s
                                    """.formatted(
                                    user.getChangeBalanceType().getType(), deal.getMoneyFrom().get(0).getCurrency().getName(),
                                    type, messageUtils.formatWithSpacesAndDecimals(amount), deal.getMoneyFrom().get(0).getCurrency().getName(),
                                    deal.getComment()
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
                                    deal.getDealType().getType(), deal.getMoneyTo().get(0).getCurrency().getName(),
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
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(row));
        message.setReplyMarkup(addBackCancelButtons(markup));

        Message message1 = telegramSender.send(message);
        userService.addMessageToDel(chatId, message1.getMessageId());
    }

    public void sendBalance(long chatId) {
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
            String formattedAmount = messageUtils.formatWithSpacesAndDecimals(amount);
            // Валюта жирная, сумма обычная
            builder.append("> *")
                    .append(currency.name().toUpperCase())
                    .append(":* ")
                    .append(formattedAmount)
                    .append("\n");
        }
    }

    public void sendTranspositionOrInvoiceApprove(long chatId) {
        User user = userService.getUser(chatId);
        Deal deal = user.getCurrentDeal();
        SendMessage message = new SendMessage();
        message.setParseMode("MarkdownV2");
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
            issuedCurrencies.append(String.format("*%s %s*", moneyFrom.getAmount(), moneyFrom.getCurrency().getName()));
        }

        for (int i = 0; i < deal.getMoneyTo().size(); i++) {
            CurrencyAmount moneyTo = deal.getMoneyTo().get(i);

            // Добавляем валюту и сумму получения
            if (!receivedCurrencies.isEmpty()) {
                receivedCurrencies.append(" + ");
            }
            receivedCurrencies.append(String.format("*%s %s*", moneyTo.getAmount(), moneyTo.getCurrency().getName()));
        }

        message.setText(messageUtils.escapeMarkdown("""
                                Подтвердить?
                                *%s*
                                Клиент: %s
                                %s
                                Получено: %s
                                Выдано: %s
                                """.formatted(
                                dealType.toUpperCase(),
                                deal.getBuyerName(),
                                deal.getCityFromTo(),
                                receivedCurrencies,
                                issuedCurrencies
                        )
                )
        );

        List<InlineKeyboardButton> row = List.of(
                createButton("Да ✅", BotCommands.APPROVE_YES)
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
        message.setParseMode("MarkdownV2");
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
            issuedCurrencies.append(String.format("*%s %s*", moneyFrom.getAmount(), moneyFrom.getCurrency().getName()));
        }

        for (int i = 0; i < deal.getMoneyTo().size(); i++) {
            CurrencyAmount moneyTo = deal.getMoneyTo().get(i);

            // Добавляем валюту и сумму получения
            if (!receivedCurrencies.isEmpty()) {
                receivedCurrencies.append(" + ");
            }
            receivedCurrencies.append(String.format("*%s %s*", moneyTo.getAmount(), moneyTo.getCurrency().getName()));
        }

        message.setText(messageUtils.escapeMarkdown("""
                                Сделка завершена ✅
                                *%s*
                                Клиент: %s
                                %s
                                Получено: %s
                                Выдано: %s
                                """.formatted(
                                dealType.toUpperCase(),
                                deal.getBuyerName(),
                                deal.getCityFromTo(),
                                receivedCurrencies,
                                issuedCurrencies
                        )
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
        if (userService.getUser(chatId).getCurrentDeal().getDealType() == DealType.PLUS_MINUS) {
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

    public Message sendSelectFullCurrency(long chatId, String text) {
        List<Money> currencies = null;
        if (userService.getUser(chatId).getCurrentDeal().getDealType() == DealType.PLUS_MINUS) {
            currencies = Arrays.stream(values()).toList();
        } else {
            currencies = List.of(Money.values());
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
                                user.getCurrentDeal().getMoneyTo().get(0).getCurrency().getName()
                        ),
                "receive");
        InlineKeyboardButton buttonReceive = createButton("%s %s Отдать"
                        .formatted(
                                user.getCurrentDeal().getCurrentAmount(),
                                user.getCurrentDeal().getMoneyFrom().get(0).getCurrency().getName()
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
                %s - %s
                Сумма: %s %s
                """.formatted(
                balanceFrom, balanceTo,
                deal.getMoneyTo().get(0).getAmount(), deal.getMoneyTo().get(0).getCurrency()));
    }

    public void sendChangedBalanceMessage(long chatId) {
        User user = userService.getUser(chatId);
        Deal deal = user.getCurrentDeal();
        String type = user.getChangeBalanceType() == ChangeBalanceType.ADD ? "Получено" : "Выдано";
        long amount = user.getChangeBalanceType() == ChangeBalanceType.ADD ? deal.getMoneyTo().get(0).getAmount() : deal.getMoneyFrom().get(0).getAmount();
        String balanceTo = BalanceType.OWN.getDisplayName();
        telegramSender.sendText(chatId, """
                        ✅*ИЗМЕНЕНИЕ БАЛАНСА*
                        %s *%s*
                        *%s %s*
                        Комментарий: %s
                        """.formatted(
                        user.getChangeBalanceType().getType(), deal.getMoneyFrom().get(0).getCurrency().getName(),
                        messageUtils.formatWithSpacesAndDecimals(amount), deal.getMoneyFrom().get(0).getCurrency().getName(),
                        deal.getComment()
                )
        );
    }

    public void sendBalancePlusMinusMessage(long chatId) {
        User user = userService.getUser(chatId);
        Deal deal = user.getCurrentDeal();
        String changeType = user.getPlusMinusType().getType();

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
            issuedCurrencies.append(String.format("*%s %s*", moneyFrom.getAmount(), moneyFrom.getCurrency().getName()));
        }

        for (int i = 0; i < deal.getMoneyTo().size(); i++) {
            CurrencyAmount moneyTo = deal.getMoneyTo().get(i);

            // Добавляем валюту и сумму получения
            if (!receivedCurrencies.isEmpty()) {
                receivedCurrencies.append(" + ");
            }
            receivedCurrencies.append(String.format("*%s %s*", moneyTo.getAmount(), moneyTo.getCurrency().getName()));
        }

        telegramSender.sendText(chatId, """
                        Баланс изменен ✅
                        %s
                        Имя: %s
                        Сумма: %s %s
                        """.formatted(
                        changeType,
                        deal.getBuyerName(),
                        receivedCurrencies, issuedCurrencies
                )
        );
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
                                    deal.getDealType().getType(), deal.getMoneyFrom().get(0).getCurrency().getName(),
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
