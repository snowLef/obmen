package org.example.ui;

import lombok.RequiredArgsConstructor;
import org.example.constants.BotCommands;
import org.example.model.*;
import org.example.service.CurrencyService;
import org.example.service.TelegramSender;
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

import static org.example.model.Money.*;

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
                            user.getCurrentDeal().getAmountTo(), user.getCurrentDeal().getMoneyTo().getName()
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
                            user.getCurrentDeal().getAmountTo(), user.getCurrentDeal().getMoneyTo().getName(),
                            user.getCurrentDeal().getExchangeRate(),
                            Math.round(user.getCurrentDeal().getAmountFrom()), user.getCurrentDeal().getMoneyFrom().getName()
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
                    String line = "%s: %s\n".formatted(currency.getName(), formattedAmount);
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


    private InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    public void sendSelectCurrency(long chatId, String text) {
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
    }

    public Message sendSelectAmountType(long chatId) {
        User user = userService.getUser(chatId);
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

}
