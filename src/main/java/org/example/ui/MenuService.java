package org.example.ui;

import org.example.constants.BotCommands;
import org.example.model.*;
import org.example.model.enums.*;
import org.example.repository.DealRepository;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.example.model.enums.BalanceType.*;
import static org.example.model.enums.Money.*;
import static org.example.ui.KeyboardUtils.buildInlineKeyboard;
import static org.example.ui.KeyboardUtils.buildReplyKeyboard;

@Service
//@RequiredArgsConstructor
public class MenuService {

    private UserService userService;
    private CurrencyService currencyService;
    private MessageUtils messageUtils;
    private TelegramSender telegramSender;
    private DealRepository dealRepo;

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

    @Autowired
    public void setDealRepo(DealRepository dealRepo) {
        this.dealRepo = dealRepo;
    }

    public void sendChangeBalanceMenu(long chatId) {
        KeyboardRow row = new KeyboardRow();
        row.add(ChangeBalanceType.ADD.getType());
        row.add(ChangeBalanceType.WITHDRAWAL.getType());
        List<KeyboardRow> rows = List.of(row);

        ReplyKeyboardMarkup keyboard = buildReplyKeyboard(rows);

        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text("Выберите опцию:")
                .replyMarkup(keyboard)
                .build();

        Message sent = telegramSender.send(message);
        userService.addMessageToDel(chatId, sent.getMessageId());
    }

    public void sendSelectBalance(long chatId, String text) {
        List<InlineKeyboardButton> row = List.of(
                createButton(OWN.getDisplayName(), OWN.name()),
                createButton(FOREIGN.getDisplayName(), FOREIGN.name())
        );

        InlineKeyboardMarkup base = buildInlineKeyboard(List.of(row));
        InlineKeyboardMarkup withNav = addBackCancelButtons(base);

        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .replyMarkup(withNav)
                .build();

        Message sent = telegramSender.send(message);
        userService.addMessageToDel(chatId, sent.getMessageId());
        userService.addMessageToEdit(chatId, sent.getMessageId());
    }

    public InlineKeyboardMarkup addBackCancelButtons(InlineKeyboardMarkup original) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        if (original != null) {
            rows.addAll(original.getKeyboard());
        }
        rows.add(List.of(
                createButton("◀️ Назад", "back"),
                createButton("❌ Отмена", "cancel")
        ));
        return buildInlineKeyboard(rows);
    }

    public void sendSelectMultiplyCurrency(Long chatId, String text) {
        InlineKeyboardMarkup base = KeyboardUtils.buildCurrencyKeyboard(
                Arrays.asList(Money.values()), true
        );
        InlineKeyboardMarkup withNav = addBackCancelButtons(base);
        Message msg = telegramSender.sendInlineKeyboard(chatId, text, withNav);
        userService.addMessageToDel(chatId, msg.getMessageId()); // мб не нужен
        userService.addMessageToEdit(chatId, msg.getMessageId());
    }

    public InlineKeyboardMarkup createCurrencyKeyboard() {
        // пропускаем RUB
        List<Money> currencies = Arrays.stream(Money.values())
                .filter(m -> m != Money.RUB)
                .toList();

        return KeyboardUtils.buildCurrencyKeyboard(currencies, true);
    }

    public InlineKeyboardMarkup createFullCurrencyKeyboard() {
        return KeyboardUtils.buildCurrencyKeyboard(
                Arrays.asList(Money.values()), true
        );
    }

    public void sendPlusMinusMenu(long chatId) {
        List<KeyboardRow> rows = new ArrayList<>();

        // Первый ряд
        KeyboardRow row1 = new KeyboardRow();
        row1.add(PlusMinusType.GET.getType());
        row1.add(PlusMinusType.GIVE.getType());
        rows.add(row1);

        // Второй ряд
        KeyboardRow row2 = new KeyboardRow();
        row2.add(PlusMinusType.LEND.getType());
        row2.add(PlusMinusType.DEBT_REPAYMENT.getType());
        rows.add(row2);

        ReplyKeyboardMarkup keyboard = buildReplyKeyboard(rows);

        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text("Выберите опцию:")
                .replyMarkup(keyboard)
                .build();

        Message sent = telegramSender.send(message);
        userService.addMessageToDel(chatId, sent.getMessageId());
    }

    public void sendMainMenu(long chatId) {
        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("Купить USD $");
        row1.add("Продать USD $");
        rows.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Купить EUR €");
        row2.add("Продать EUR €");
        rows.add(row2);

        KeyboardRow row4 = new KeyboardRow();
        row4.add("Купить USDT");
        row4.add("Продать USDT");
        rows.add(row4);

        KeyboardRow row3 = new KeyboardRow();
        row3.add("Купить USD (Б)");
        row3.add("Продать USD (Б)");
        rows.add(row3);

        KeyboardRow row = new KeyboardRow();
        row.add("Купить Y.E.");
        row.add("Продать Y.E.");
        rows.add(row);

        KeyboardRow customRow = new KeyboardRow();
        customRow.add("Валютный обмен");
        customRow.add("Перестановка");
        customRow.add("Invoice");
        rows.add(customRow);

        KeyboardRow row5 = new KeyboardRow();
        row5.add("+/-");
        row5.add("Перемещение");
        row5.add("Изменение");
        row5.add("Баланс");
        rows.add(row5);

        ReplyKeyboardMarkup keyboard = buildReplyKeyboard(rows);

        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text("Выберите опцию:")
                .replyMarkup(keyboard)
                .build();

        Message sent = telegramSender.send(message);
        userService.addMessageToDel(chatId, sent.getMessageId());
    }

    public void sendApproveMenu(long chatId) {
        User user = userService.getUser(chatId);
        Deal deal = user.getCurrentDeal();
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setParseMode("MarkdownV2");

        String receivedCurrencies = messageUtils.formatCurrencyAmounts(deal.getMoneyTo());
        String issuedCurrencies = messageUtils.formatCurrencyAmounts(deal.getMoneyFrom());

        if (deal.getDealType() == DealType.PLUS_MINUS) {
            if (deal.getPlusMinusType() == PlusMinusType.LEND) {
                receivedCurrencies = "";
            } else if (deal.getPlusMinusType() == PlusMinusType.DEBT_REPAYMENT) {
                issuedCurrencies = "";
            }
            message.setText(messageUtils.escapeMarkdown("""
                                    Подтвердить?
                                    *%s*
                                    Имя: %s
                                    Сумма: %s %s
                                    """.formatted(
                                    deal.getPlusMinusType().getType().toUpperCase(),
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
                                    deal.getBalanceTypeFrom().getDisplayName(), deal.getBalanceTypeTo().getDisplayName(),
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
                                    messageUtils.formatWithSpacesAndDecimals(deal.getMoneyFrom().get(0).getAmount()), deal.getMoneyFrom().get(0).getCurrency().getName()
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
                                    messageUtils.formatWithSpacesAndDecimals(deal.getMoneyFrom().get(0).getAmount()), deal.getMoneyFrom().get(0).getCurrency().getName()
                            )
                    )
            );
        } else if (deal.getDealType() == DealType.CHANGE_BALANCE) {
            String type = deal.getChangeBalanceType() == ChangeBalanceType.ADD ? "Получено" : "Выдано";
            long amount = deal.getChangeBalanceType() == ChangeBalanceType.ADD ? deal.getMoneyTo().get(0).getAmount() : deal.getMoneyFrom().get(0).getAmount();
            message.setText(messageUtils.escapeMarkdown("""
                                    Подтвердить?
                                    *ИЗМЕНЕНИЕ БАЛАНСА*
                                    %s *%s*
                                    %s *%s %s*
                                    Комментарий: %s
                                    """.formatted(
                                    deal.getChangeBalanceType().getType(), deal.getMoneyFrom().get(0).getCurrency().getName(),
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
                createButton("Да ✅", BotCommands.APPROVE + ":" + deal.getId()),
                createButton("\uD83D\uDCCC Фикс", "fix:" + deal.getId())
        );

        InlineKeyboardMarkup base = buildInlineKeyboard(List.of(row));
        InlineKeyboardMarkup withNav = addBackCancelButtons(base);
        message.setReplyMarkup(withNav);

        Message message1 = telegramSender.send(message);
        userService.addMessageToDel(chatId, message1.getMessageId());
    }

    public void sendTranspositionOrInvoiceApprove(long chatId) {
        User user = userService.getUser(chatId);
        Deal deal = user.getCurrentDeal();
        SendMessage message = new SendMessage();
        message.setParseMode("MarkdownV2");
        message.setChatId(String.valueOf(chatId));
        String dealType = deal.getDealType().getType();

        String receivedCurrencies = messageUtils.formatCurrencyAmounts(deal.getMoneyTo());
        String issuedCurrencies = messageUtils.formatCurrencyAmounts(deal.getMoneyFrom());

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
                createButton("Да ✅", BotCommands.APPROVE + ":" + deal.getId()),
                createButton("\uD83D\uDCCC Фикс", "fix:" + deal.getId())
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(row));
        message.setReplyMarkup(addBackCancelButtons(markup));

        Message msg = telegramSender.send(message);
        userService.addMessageToDel(chatId, msg.getMessageId());
    }

    public void sendDealCompletedWithCancel(long chatId, Deal deal) {
        User user = userService.getUser(chatId);

        StringBuilder text = new StringBuilder(String.format("%s/%s ✅\n", deal.getCreatedAt().format(DateTimeFormatter.ofPattern("MMdd")), deal.getId()));

        switch (deal.getDealType()) {
            case TRANSPOSITION, INVOICE -> text.append(sendTranspositionOrInvoiceComplete(deal));
            case PLUS_MINUS -> text.append(sendBalancePlusMinusMessage(user, deal));
            case BUY, SELL, CUSTOM -> text.append(sendDealCompletedMessage(deal));
            case CHANGE_BALANCE -> text.append(sendChangedBalanceMessage(user, deal));
            case MOVING_BALANCE -> text.append(sendBalanceMovedMessage(user, deal));
        }

        InlineKeyboardMarkup kb = InlineKeyboardBuilder.builder()
                .button("Отменить сделку", "show_cancel:" + deal.getId() + ":" /* placeholder */)
                .build();

        // отправляем без callbackData сообщение, чтобы получить реальный msgId
        Message sent = telegramSender.sendWithMarkup(chatId, text.toString(), kb);
        int msgId = sent.getMessageId();

        // теперь обновим разметку, в которой callbackData содержит правильный msgId
        InlineKeyboardMarkup kbWithId = InlineKeyboardBuilder.builder()
                .button("Отменить сделку", "show_cancel:" + deal.getId() + ":" + msgId)
                .build();
        telegramSender.editReplyMarkup(chatId, msgId, kbWithId);
    }

    private String sendBalanceMovedMessage(User user, Deal deal) {
        String balanceFrom = deal.getBalanceTypeFrom().getDisplayName();
        String balanceTo = deal.getBalanceTypeTo().getDisplayName();
        return """
                Баланс изменен ✅
                %s - %s
                Сумма: %s %s
                """.formatted(
                balanceFrom, balanceTo,
                messageUtils.formatWithSpacesAndDecimals(deal.getMoneyTo().get(0).getAmount()), deal.getMoneyTo().get(0).getCurrency());
    }

    private String sendChangedBalanceMessage(User user, Deal deal) {
        long amount = deal.getChangeBalanceType() == ChangeBalanceType.ADD ? deal.getMoneyTo().get(0).getAmount() : deal.getMoneyFrom().get(0).getAmount();
        return """
                ✅*ИЗМЕНЕНИЕ БАЛАНСА*
                %s *%s*
                *%s %s*
                Комментарий: %s
                """.formatted(
                deal.getChangeBalanceType().getType(), deal.getMoneyFrom().get(0).getCurrency().getName(),
                messageUtils.formatWithSpacesAndDecimals(amount), deal.getMoneyFrom().get(0).getCurrency().getName(),
                deal.getComment()
        );
    }

    private String sendTranspositionOrInvoiceComplete(Deal deal) {
        String dealType = deal.getDealType().getType();

        String receivedCurrencies = messageUtils.formatCurrencyAmounts(deal.getMoneyTo());
        String issuedCurrencies = messageUtils.formatCurrencyAmounts(deal.getMoneyFrom());

        return """
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
        );
    }


    private String sendBalancePlusMinusMessage(User user, Deal deal) {
        String changeType = deal.getPlusMinusType().getType();

        String receivedCurrencies = messageUtils.formatCurrencyAmounts(deal.getMoneyTo());
        String issuedCurrencies = messageUtils.formatCurrencyAmounts(deal.getMoneyFrom());

        if (deal.getPlusMinusType() == PlusMinusType.LEND) {
            receivedCurrencies = "";
        } else if (deal.getPlusMinusType() == PlusMinusType.DEBT_REPAYMENT) {
            issuedCurrencies = "";
        }

        return """
                Баланс изменен ✅
                *%s*
                Имя: %s
                Сумма: %s %s
                """.formatted(
                changeType.toUpperCase(),
                deal.getBuyerName(),
                receivedCurrencies, issuedCurrencies
        );
    }

    private String sendDealCompletedMessage(Deal deal) {
        if (deal.getDealType() == DealType.CUSTOM) {
            return """
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
            );
        } else if (deal.getDealType() == DealType.BUY) {
            return """
                    Сделка завершена ✅
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
            );
        } else if (deal.getDealType() == DealType.SELL) {
            return """
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
            );
        } else {
            return """
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
            );
        }
    }

    private InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    public Message sendSelectCurrency(long chatId, String text) {
        // выбор набора в зависимости от типа сделки
        List<Money> currencies = userService.getUser(chatId)
                .getCurrentDeal().getDealType() == DealType.PLUS_MINUS
                ? Arrays.asList(Money.values())
                : List.of(USDT, USD, EUR, USDW, YE);

        InlineKeyboardMarkup base = KeyboardUtils.buildCurrencyKeyboard(currencies, false);
        InlineKeyboardMarkup withNav = addBackCancelButtons(base);

        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .replyMarkup(withNav)
                .build();

        Message sent = telegramSender.send(message);
        userService.addMessageToDel(chatId, sent.getMessageId());
        userService.addMessageToEdit(chatId, sent.getMessageId());
        return sent;
    }

    public Message sendSelectFullCurrency(long chatId, String text) {
        List<Money> currencies = Arrays.asList(Money.values());
        InlineKeyboardMarkup withNav = addBackCancelButtons(
                KeyboardUtils.buildCurrencyKeyboard(currencies, false)
        );

        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .replyMarkup(withNav)
                .build();

        Message sent = telegramSender.send(message);
        userService.addMessageToDel(chatId, sent.getMessageId());
        userService.addMessageToEdit(chatId, sent.getMessageId());
        return sent;
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

    public void sendEnterExchangeRate(long chatId) {
        telegramSender.sendTextWithKeyboard(chatId, "Введите курс:");
    }

    public void sendBalance(long chatId) {
        // 1) Собираем дельты зафиксированных сделок
        Map<BalanceType, Map<Money, Long>> fixedDeltas = calculateFixedDeltas();

        // 2) Отправляем уже отформатированный текст
        messageUtils.sendFormattedText(chatId, formatBalances(fixedDeltas));
    }

    private String formatBalances(Map<BalanceType, Map<Money, Long>> fixedDeltas) {
        StringBuilder message = new StringBuilder();

        message.append("> __*---НАШ БАЛАНС---:*__\n");
        appendCurrencyLines(message, BalanceType.OWN,     fixedDeltas.getOrDefault(BalanceType.OWN,     Map.of()));

        message.append("\n> __*ЧУЖОЙ БАЛАНС:*__\n");
        appendCurrencyLines(message, BalanceType.FOREIGN, fixedDeltas.getOrDefault(BalanceType.FOREIGN, Map.of()));

        message.append("\n> __*ДОЛГ:*__\n");
        appendCurrencyLines(message, BalanceType.DEBT,    fixedDeltas.getOrDefault(BalanceType.DEBT,    Map.of()));

        return message.toString();
    }

    private void appendCurrencyLines(StringBuilder builder,
                                     BalanceType type,
                                     Map<Money, Long> fixedForType)
    {
        for (Money currency : Money.values()) {
            long amount = currencyService.getBalance(currency, type);
            // Покажем строку, если есть либо основной баланс, либо фикс
            long fixed = fixedForType.getOrDefault(currency, 0L);
            if (amount != 0 || fixed != 0) {
                String fmtAmount = messageUtils.formatWithSpacesAndDecimals(amount);
                builder.append("> *")
                        .append(currency.name().toUpperCase())
                        .append(":* ")
                        .append(fmtAmount);

                if (fixed != 0) {
                    String fmtFixed = messageUtils.formatWithSpacesAndDecimals(fixed);
                    // плюс или минус автоматически в fmtFixed
                    builder.append(" (")
                            .append(fmtFixed)
                            .append(")");
                }
                builder.append("\n");
            }
        }
    }

    private Map<BalanceType, Map<Money, Long>> calculateFixedDeltas() {
        // собираем все FIX‐сделки
        List<Deal> fixedDeals = dealRepo.findByStatus(DealStatus.FIX);

        // структура: для каждого типа баланса — карта валют → дельта
        Map<BalanceType, Map<Money, Long>> result = new EnumMap<>(BalanceType.class);
        for (BalanceType t : BalanceType.values()) {
            result.put(t, new EnumMap<>(Money.class));
        }

        for (Deal d : fixedDeals) {
            BalanceType fromType = d.getBalanceTypeFrom();
            BalanceType toType   = d.getBalanceTypeTo();

            // вычитаем списанное
            for (CurrencyAmount from : d.getMoneyFrom()) {
                result.get(fromType)
                        .merge(from.getCurrency(), -from.getAmount(), Long::sum);
            }
            // добавляем поступившее
            for (CurrencyAmount to : d.getMoneyTo()) {
                result.get(toType)
                        .merge(to.getCurrency(), +to.getAmount(), Long::sum);
            }
        }

        return result;
    }

}
