package org.example.config;


import lombok.RequiredArgsConstructor;
import org.example.constants.BotCommands;
import org.example.handler.command.CommandContext;
import org.example.handler.command.CommandHandler;
import org.example.infra.TelegramSender;
import org.example.model.enums.ChangeBalanceType;
import org.example.model.enums.DealType;
import org.example.model.enums.Money;
import org.example.model.enums.Status;
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
        map.put("Купить Доллар", ctx -> start(ctx, RUB, USD, BUY));
        map.put("Продать Доллар", ctx -> start(ctx, USD, RUB, SELL));
        map.put("Купить Евро",    ctx -> start(ctx, RUB, EUR, BUY));
        map.put("Продать Евро",   ctx -> start(ctx, EUR, RUB, SELL));
        map.put("Купить Белый Доллар", ctx -> start(ctx, RUB, USDW, BUY));
        map.put("Продать Белый Доллар",ctx -> start(ctx, USDW, RUB, SELL));
        map.put("Купить Tether",  ctx -> start(ctx, RUB, USDT, BUY));
        map.put("Продать Tether", ctx -> start(ctx, USDT, RUB, SELL));

        // Сервисные
        map.put("Меню",    ctx -> menuService.sendMainMenu(ctx.chatId()));
        map.put("/start",  ctx -> menuService.sendMainMenu(ctx.chatId()));

        map.put("Сложный обмен", ctx -> {
            userService.saveUserStatus(ctx.chatId(), Status.AWAITING_BUYER_NAME);
            var deal = new org.example.model.Deal();
            deal.setDealType(DealType.CUSTOM);
            userService.saveUserCurrentDeal(ctx.chatId(), deal);

            var botMsg = telegramSender.sendText(ctx.chatId(), BotCommands.ASK_FOR_NAME);
            userService.addMessageToDel(ctx.chatId(), ctx.msgId());
            userService.addMessageToDel(ctx.chatId(), botMsg.getMessageId());
        });

        map.put("+/-", ctx -> {
            userService.addMessageToDel(ctx.chatId(), ctx.msgId());
            userService.startDeal(ctx.chatId(), null, null, CHANGE_BALANCE);
            userService.saveUserStatus(ctx.chatId(), Status.AWAITING_BUYER_NAME);
            telegramSender.sendText(ctx.chatId(), "Введите имя:");
        });

        map.put("Принимаем +",   ctx -> handleChangeBalance(ctx, ChangeBalanceType.GET));
        map.put("Отдаем +",       ctx -> handleChangeBalance(ctx, ChangeBalanceType.GIVE));
        map.put("Даем в долг",    ctx -> handleChangeBalance(ctx, ChangeBalanceType.LEND));
        map.put("Возврат долга",  ctx -> handleChangeBalance(ctx, ChangeBalanceType.DEBT_REPAYMENT));

        map.put("Баланс", ctx -> menuService.sendBalance(ctx.chatId()));

        return map;
    }

    private void handleChangeBalance(CommandContext ctx, ChangeBalanceType type) {
        long chatId = ctx.chatId();
        var user = userService.getOrCreate(chatId);
        user.setChangeBalanceType(type);
        user.setStatus(Status.AWAITING_FIRST_CURRENCY);
        userService.save(user);
        menuService.sendSelectCurrency(chatId, "Выберите валюту:");
    }

    private void start(CommandContext ctx, Money from, Money to, DealType dealType) {
        long chatId = ctx.chatId();
        Message message = ctx.message();

        userService.startDeal(chatId, from, to, dealType);
        userService.saveUserStatus(chatId, Status.AWAITING_BUYER_NAME);
        userService.addMessageToDel(chatId, message.getMessageId());

        var botMsg = telegramSender.sendText(chatId, BotCommands.ASK_FOR_NAME);
        userService.addMessageToDel(chatId, botMsg.getMessageId());
    }
}
