package org.example.ui;

import org.example.constants.BotCommands;
import org.example.model.Money;
import org.example.model.User;
import org.example.service.CurrencyService;
import org.example.service.UserService;
import org.example.util.MessageUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.example.model.Money.*;

public class MenuService {

    public static void sendMainMenu(long chatId) {
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
        keyboard.add(customRow);

        // Пятый ряд
        KeyboardRow row5 = new KeyboardRow();
        row5.add("Баланс");
        keyboard.add(row5);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true); // Опционально: подгоняет размер кнопок
        keyboardMarkup.setOneTimeKeyboard(false); // Опционально: скрывает клавиатуру после использования

        message.setReplyMarkup(keyboardMarkup);

        Message msg = MessageUtils.sendMsg(message);
        UserService.addMessageToDel(chatId, msg.getMessageId());
    }

    public static void sendApproveMenu(long chatId) {
        User user = UserService.getUser(chatId);
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("""
                        Подтвердить?
                        %s -> %s
                        Имя: %s
                        Сумма получения: %s %s
                        Курс: %s
                        Сумма выдачи: %s %s
                        """.formatted(
                        user.getCurrentDeal().getMoneyFrom(), user.getCurrentDeal().getMoneyTo(),
                        user.getCurrentDeal().getBuyerName(),
                        user.getCurrentDeal().getAmountTo(), user.getCurrentDeal().getMoneyTo().getName(),
                        user.getCurrentDeal().getExchangeRate(),
                        Math.round(user.getCurrentDeal().getAmountFrom()), user.getCurrentDeal().getMoneyFrom().getName()
                )
        );

        List<InlineKeyboardButton> row = List.of(
                createButton("Да", BotCommands.APPROVE_YES),
                createButton("Нет", BotCommands.APPROVE_NO)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(row));
        message.setReplyMarkup(markup);

        Message msg = MessageUtils.sendMsg(message);
        UserService.addMessageToDel(chatId, msg.getMessageId());
    }

    public static void sendBalance(long chatId) {
        StringBuilder text = new StringBuilder("Баланс:\n");
        Arrays.stream(Money.values()).forEach(currency -> {
            double amount = CurrencyService.getBalance(currency);
            String formattedAmount = MessageUtils.formatWithSpacesAndDecimals(String.valueOf(amount));
            text.append("%s: %s\n".formatted(currency.getName(), formattedAmount));
        });

        MessageUtils.sendText(chatId, text.toString());
    }

    private static InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    public static Message sendSelectCurrency(long chatId, String text) {
        //List<Currency> currencies = CurrencyService.getAllCurrency();
        List<Money> currencies = List.of(USDT, USD, EUR, USDW, YE);
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

        Message msg = MessageUtils.sendMsg(message);
        UserService.addMessageToDel(chatId, msg.getMessageId());
        UserService.addMessageToEdit(chatId, msg.getMessageId());
        return msg;
    }

    public static Message sendSelectAmountType(long chatId) {
        User user = UserService.getUser(chatId);
        InlineKeyboardButton buttonGive = createButton("%s %s Забрать"
                        .formatted(
                                user.getCurrentDeal().getCurrentAmount(),
                                user.getCurrentDeal().getMoneyTo()
                        ),
                "receive");
        InlineKeyboardButton buttonReceive = createButton("%s %s Отдать"
                        .formatted(
                                user.getCurrentDeal().getCurrentAmount(),
                                user.getCurrentDeal().getMoneyFrom()
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

        return MessageUtils.sendMsg(message);
    }

    public static Message sendSelectCurrencyType(long chatId) {
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

        return MessageUtils.sendMsg(message);
    }

}
