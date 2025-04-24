package org.example.config;


import org.example.constants.BotCommands;
import org.example.handler.command.CommandContext;
import org.example.handler.command.CommandHandler;
import org.example.infra.TelegramSender;
import org.example.model.CurrencyAmount;
import org.example.model.Deal;
import org.example.model.User;
import org.example.model.enums.*;
import org.example.service.UserService;
import org.example.ui.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.example.model.enums.DealType.*;
import static org.example.model.enums.Money.*;

@Configuration
public class CommandMapConfig {

    private UserService userService;
    private TelegramSender telegramSender;
    private MenuService menuService;

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

    @Bean
    public Map<String, CommandHandler> commandMap() {
        Map<String, CommandHandler> map = new LinkedHashMap<>();

        // Курсы
        map.put("Купить USD", ctx -> start(ctx, RUB, USD, BUY));
        map.put("Продать USD", ctx -> start(ctx, USD, RUB, SELL));
        map.put("Купить EUR", ctx -> start(ctx, RUB, EUR, BUY));
        map.put("Продать EUR", ctx -> start(ctx, EUR, RUB, SELL));
        map.put("Купить USD (Б)", ctx -> start(ctx, RUB, USDW, BUY));
        map.put("Продать USD (Б)", ctx -> start(ctx, USDW, RUB, SELL));
        map.put("Купить USDT", ctx -> start(ctx, RUB, USDT, BUY));
        map.put("Продать USDT", ctx -> start(ctx, USDT, RUB, SELL));

        // Сервисные
        map.put("Меню", ctx -> menuService.sendMainMenu(ctx.chatId()));
        map.put("/start", ctx -> menuService.sendMainMenu(ctx.chatId()));

        map.put("Валютный обмен", this::handleCustomChange);
        map.put("Перестановка", ctx -> handleTranspositionOrInvoice(ctx, TRANSPOSITION));
        map.put("Invoice", ctx -> handleTranspositionOrInvoice(ctx, INVOICE));

        map.put("+/-", this::handlePlusMinus);
        map.put("Перемещение", this::movingTheBalance);
        map.put("Изменение", this::changeTheBalance);
        map.put("Баланс", ctx -> menuService.sendBalance(ctx.chatId()));

        map.put("Принимаем +", ctx -> handlePlusMinusBalance(ctx, PlusMinusType.GET));
        map.put("Отдаем +", ctx -> handlePlusMinusBalance(ctx, PlusMinusType.GIVE));
        map.put("Даем в долг", ctx -> handlePlusMinusBalance(ctx, PlusMinusType.LEND));
        map.put("Возврат долга", ctx -> handlePlusMinusBalance(ctx, PlusMinusType.DEBT_REPAYMENT));

//        map.put("Пополнение", ctx -> handleAddOrWithdrawalBalance(ctx, ChangeBalanceType.ADD));
//        map.put("Вывод", ctx -> handleAddOrWithdrawalBalance(ctx, ChangeBalanceType.WITHDRAWAL));

        return map;
    }

//    private void handleAddOrWithdrawalBalance(CommandContext ctx, ChangeBalanceType type) {
//        long chatId = ctx.chatId();
//        User user = userService.getOrCreate(chatId);
//        user.setChangeBalanceType(type);
//        user.pushStatus(Status.AWAITING_FIRST_CURRENCY);
//        userService.save(user);
//        menuService.sendSelectFullCurrency(chatId, "Выберите валюту:");
//    }

    private void changeTheBalance(CommandContext ctx) {
        User user = userService.getUser(ctx.chatId());
        user.pushStatus(Status.AWAITING_CHANGE_BALANCE_TYPE);
        Deal deal = new Deal();
        deal.setDealType(CHANGE_BALANCE);
        user.setCurrentDeal(deal);
        userService.save(user);
        menuService.sendChangeBalanceMenu(ctx.chatId());
    }

    private void movingTheBalance(CommandContext ctx) {
        User user = userService.getUser(ctx.chatId());
        user.pushStatus(Status.AWAITING_CHOOSE_BALANCE_FROM);
        Deal deal = new Deal();
        deal.setDealType(MOVING_BALANCE);
        user.setCurrentDeal(deal);
        userService.save(user);
        menuService.sendSelectBalance(ctx.chatId(), "Откуда списать?");
    }

    private void handleTranspositionOrInvoice(CommandContext ctx, DealType dealType) {
        userService.addMessageToDel(ctx.chatId(), ctx.msgId());
        User user = userService.getUser(ctx.chatId());
        user.setCurrentDeal(new Deal());
        user.getCurrentDeal().setDealType(dealType);
        user.pushStatus(Status.AWAITING_BUYER_NAME);
        userService.save(user);
        telegramSender.sendTextWithKeyboard(ctx.chatId(), BotCommands.ASK_FOR_NAME);
    }

    private void handlePlusMinus(CommandContext ctx) {
        userService.saveUserStatus(ctx.chatId(), Status.AWAITING_BUYER_NAME);
        userService.addMessageToDel(ctx.chatId(), ctx.msgId());
        userService.startDeal(ctx.chatId(), null, null, PLUS_MINUS);
        telegramSender.sendTextWithKeyboard(ctx.chatId(), BotCommands.ASK_FOR_NAME);
    }

    private void handlePlusMinusBalance(CommandContext ctx, PlusMinusType type) {
        long chatId = ctx.chatId();
        User user = userService.getOrCreate(chatId);
        user.setPlusMinusType(type);
        user.pushStatus(Status.AWAITING_FIRST_CURRENCY);
        userService.save(user);
        menuService.sendSelectCurrency(chatId, "Выберите валюту:");
    }

    private void handleCustomChange(CommandContext ctx) {
        userService.saveUserStatus(ctx.chatId(), Status.AWAITING_BUYER_NAME);
        Deal deal = new Deal();
        deal.setDealType(DealType.CUSTOM);
        userService.saveUserCurrentDeal(ctx.chatId(), deal);

        telegramSender.sendTextWithKeyboard(ctx.chatId(), BotCommands.ASK_FOR_NAME);
        userService.addMessageToDel(ctx.chatId(), ctx.msgId());
    }

    private void start(CommandContext ctx, Money from, Money to, DealType dealType) {
        long chatId = ctx.chatId();
        Message message = ctx.message();

        userService.startDeal(chatId, new CurrencyAmount(from, 0), new CurrencyAmount(to, 0), dealType);
        userService.saveUserStatus(chatId, Status.AWAITING_BUYER_NAME);
        userService.addMessageToDel(chatId, message.getMessageId());
        telegramSender.sendTextWithKeyboard(ctx.chatId(), BotCommands.ASK_FOR_NAME);
    }
}
