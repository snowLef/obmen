package org.example.config;


import org.example.constants.BotCommands;
import org.example.handler.command.CommandContext;
import org.example.handler.command.CommandHandler;
import org.example.infra.TelegramSender;
import org.example.model.CurrencyAmount;
import org.example.model.Deal;
import org.example.model.User;
import org.example.model.enums.*;
import org.example.repository.DealRepository;
import org.example.service.ExcelReportService;
import org.example.service.ExchangeProcessor;
import org.example.service.UserService;
import org.example.ui.MenuService;
import org.example.util.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.example.model.enums.DealType.*;
import static org.example.model.enums.Money.*;

@Configuration
public class CommandMapConfig {

    private UserService userService;
    private TelegramSender telegramSender;
    private MenuService menuService;
    private ExchangeProcessor exchangeProcessor;
    private DealRepository dealRepo;
    private ExcelReportService reportService;
    private MessageUtils messageUtils;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setMenuService(MenuService menuService) {
        this.menuService = menuService;
    }

    @Autowired
    public void setTelegramSender(TelegramSender telegramSender) {
        this.telegramSender = telegramSender;
    }

    @Autowired
    public void setExchangeProcessor(ExchangeProcessor exchangeProcessor) {
        this.exchangeProcessor = exchangeProcessor;
    }

    @Autowired
    public void setDealRepo(DealRepository dealRepo) {
        this.dealRepo = dealRepo;
    }

    @Autowired
    public void setReportService(ExcelReportService reportService) {
        this.reportService = reportService;
    }

    @Bean
    public Map<String, CommandHandler> commandMap() {
        Map<String, CommandHandler> map = new LinkedHashMap<>();

        // Курсы
        map.put("Купить USD $", ctx -> start(ctx, RUB, USD, BUY));
        map.put("Продать USD $", ctx -> start(ctx, USD, RUB, SELL));
        map.put("Купить EUR €", ctx -> start(ctx, RUB, EUR, BUY));
        map.put("Продать EUR €", ctx -> start(ctx, EUR, RUB, SELL));
        map.put("Купить USD (Б)", ctx -> start(ctx, RUB, USDW, BUY));
        map.put("Продать USD (Б)", ctx -> start(ctx, USDW, RUB, SELL));
        map.put("Купить USDT", ctx -> start(ctx, RUB, USDT, BUY));
        map.put("Продать USDT", ctx -> start(ctx, USDT, RUB, SELL));
        map.put("Купить Y.E.", ctx -> start(ctx, RUB, YE, BUY));
        map.put("Продать Y.E.", ctx -> start(ctx, YE, RUB, SELL));

        // Сервисные
        map.put("Меню", ctx -> menuService.sendMainMenu(ctx.chatId()));
        map.put("/start", ctx -> menuService.sendMainMenu(ctx.chatId()));
        map.put("Фикс", this::handleFixDeals);
        map.put("Отчет", this::handleReport);

        map.put("Валютный обмен", this::handleCustomChange);
        map.put("Перестановка", ctx -> handleTranspositionOrInvoice(ctx, TRANSPOSITION));
        map.put("Invoice", ctx -> handleTranspositionOrInvoice(ctx, INVOICE));

        map.put("+/-", this::handlePlusMinus);
        map.put("Перемещение", this::movingTheBalance);
        map.put("Изменение", this::changeTheBalance);
        map.put("Баланс", ctx -> {
            menuService.sendBalance(ctx.chatId());
            menuService.sendMainMenu(ctx.chatId());
        });

        map.put("/cancel", this::cancel);

        return map;
    }

    private void handleReport(CommandContext ctx) {
        long chatId = ctx.chatId();
        // Вытягиваем все сделки (или фильтруем нужные)
        List<Deal> deals = dealRepo.findAll();
        byte[] excel = reportService.generateDealReport(deals);
        telegramSender.sendExcelReport(chatId, excel, "full-report.xlsx");
    }

    private void handleFixDeals(CommandContext ctx) {
        long chatId = ctx.message().getChatId();
        String username = ctx.message().getChat().getId().toString().substring(4);
        List<Deal> fixed = dealRepo.findByStatus(DealStatus.FIX);

        if (fixed.isEmpty()) {
            telegramSender.sendText(chatId, "У вас нет зафиксированных сделок.");
            return;
        }

        StringBuilder sb = new StringBuilder("<b>СДЕЛКИ ФИКС:</b>\n\n");

        for (Deal deal : fixed) {
            int origMsgId = deal.getMessageId();  // сохранили при фиксации
            String link = "https://t.me/c/" + username + "/" + origMsgId;

            sb.append(String.format(
                    "<a href=\"%s\">%s/%s</a>: ",
                    link, deal.getCreatedAt().format(DateTimeFormatter.ofPattern("MMdd")), deal.getId()
            ));
            // детали сделки
            deal.getMoneyFrom().forEach(f ->
                    sb
                            .append(" -")
                            .append(messageUtils.formatWithSpacesAndDecimals(f.getAmount()))
                            .append(" ")
                            .append(f.getCurrency())
            );
            sb.append(" → ");
            deal.getMoneyTo().forEach(t ->
                    sb
                            .append(" +")
                            .append(messageUtils.formatWithSpacesAndDecimals(t.getAmount()))
                            .append(" ")
                            .append(t.getCurrency())
            );
            sb.append("\n");
        }

        // Отправляем как HTML, чтобы ссылки работали
        telegramSender.sendWithMarkup(chatId, sb.toString(), null, "HTML");
    }

    private void cancel(CommandContext ctx) {
        exchangeProcessor.cancel(ctx.chatId());
    }

    private void changeTheBalance(CommandContext ctx) {
        User user = userService.getUser(ctx.chatId());
        user.pushStatus(Status.AWAITING_CHANGE_BALANCE_TYPE);
        Deal deal = new Deal();
        deal.setBalanceTypeFrom(BalanceType.OWN);
        deal.setBalanceTypeTo(BalanceType.OWN);
        deal.setDealType(CHANGE_BALANCE);
        deal.setCreatedBy("%s %s %s".formatted(ctx.message().getFrom().getFirstName(), ctx.message().getFrom().getLastName(), ctx.message().getFrom().getUserName()));
        user.setCurrentDeal(deal);
        userService.save(user);
        userService.addMessageToDel(ctx.chatId(), ctx.msgId());
        menuService.sendChangeBalanceMenu(ctx.chatId());
    }

    private void movingTheBalance(CommandContext ctx) {
        User user = userService.getUser(ctx.chatId());
        user.pushStatus(Status.AWAITING_CHOOSE_BALANCE_FROM);
        Deal deal = new Deal();
        deal.setDealType(MOVING_BALANCE);
        deal.setCreatedBy("%s %s %s".formatted(ctx.message().getFrom().getFirstName(), ctx.message().getFrom().getLastName(), ctx.message().getFrom().getUserName()));
        user.setCurrentDeal(deal);
        userService.save(user);
        menuService.sendSelectBalance(ctx.chatId(), "Откуда списать?");
    }

    private void handleTranspositionOrInvoice(CommandContext ctx, DealType dealType) {
        userService.addMessageToDel(ctx.chatId(), ctx.msgId());
        User user = userService.getUser(ctx.chatId());
        Deal deal = new Deal();
        deal.setBalanceTypeFrom(BalanceType.OWN);
        deal.setBalanceTypeTo(BalanceType.OWN);
        deal.setDealType(dealType);
        deal.setCreatedBy("%s %s %s".formatted(ctx.message().getFrom().getFirstName(), ctx.message().getFrom().getLastName(), ctx.message().getFrom().getUserName()));
        user.setCurrentDeal(deal);
        user.pushStatus(Status.AWAITING_BUYER_NAME);
        userService.save(user);
        telegramSender.sendTextWithKeyboard(ctx.chatId(), BotCommands.ASK_FOR_NAME);
    }

    private void handlePlusMinus(CommandContext ctx) {
        userService.saveUserStatus(ctx.chatId(), Status.AWAITING_PLUS_MINUS_TYPE);
        userService.addMessageToDel(ctx.chatId(), ctx.msgId());
        userService.startDeal(ctx.chatId(), null, null, PLUS_MINUS, ctx.message());
        User user = userService.getUser(ctx.chatId());
        user.getCurrentDeal().setCreatedBy("%s %s %s".formatted(ctx.message().getFrom().getFirstName(), ctx.message().getFrom().getLastName(), ctx.message().getFrom().getUserName()));
        userService.save(user);
        menuService.sendPlusMinusMenu(ctx.chatId());
    }

    private void handleCustomChange(CommandContext ctx) {
        userService.saveUserStatus(ctx.chatId(), Status.AWAITING_BUYER_NAME);
        Deal deal = new Deal();
        deal.setBalanceTypeFrom(BalanceType.OWN);
        deal.setBalanceTypeTo(BalanceType.OWN);
        deal.setCreatedBy("%s %s %s".formatted(ctx.message().getFrom().getFirstName(), ctx.message().getFrom().getLastName(), ctx.message().getFrom().getUserName()));
        deal.setDealType(CUSTOM);
        userService.saveUserCurrentDeal(ctx.chatId(), deal);

        telegramSender.sendTextWithKeyboard(ctx.chatId(), BotCommands.ASK_FOR_NAME);
        userService.addMessageToDel(ctx.chatId(), ctx.msgId());
    }

    private void start(CommandContext ctx, Money from, Money to, DealType dealType) {
        long chatId = ctx.chatId();
        Message message = ctx.message();

        userService.startDeal(chatId, new CurrencyAmount(from, 0), new CurrencyAmount(to, 0), dealType, ctx.message());
        User user = userService.getUser(ctx.chatId());
        user.getCurrentDeal().setCreatedBy("%s %s %s".formatted(ctx.message().getFrom().getFirstName(), ctx.message().getFrom().getLastName(), ctx.message().getFrom().getUserName()));
        userService.save(user);
        userService.saveUserStatus(chatId, Status.AWAITING_BUYER_NAME);
        userService.addMessageToDel(chatId, message.getMessageId());
        telegramSender.sendTextWithKeyboard(ctx.chatId(), BotCommands.ASK_FOR_NAME);
    }

    @Autowired
    public void setMessageUtils(MessageUtils messageUtils) {
        this.messageUtils = messageUtils;
    }
}
