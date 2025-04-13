package org.example.handler;

import org.example.constants.BotCommands;
import org.example.model.*;
import org.example.service.DealService;
import org.example.service.UserService;
import org.example.service.ExchangeProcessor;
import org.example.state.Status;
import org.example.util.MessageUtils;
import org.example.ui.MenuService;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

public class CallbackHandler {

    public void handle(CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getMessage().getChatId();
        String data = callbackQuery.getData();
        Integer msgId = callbackQuery.getMessage().getMessageId();


        switch (data) {
//            case BotCommands.BUY_USD -> start(chatId, Money.RUB, Money.USD, DealType.BUY, msgId);
//            case BotCommands.SELL_USD -> start(chatId, Money.RUB, Money.USD, DealType.SELL, msgId);
//
//            case BotCommands.BUY_EUR -> start(chatId, Money.RUB, Money.EUR, DealType.BUY, msgId);
//            case BotCommands.SELL_EUR -> start(chatId, Money.RUB, Money.EUR, DealType.SELL, msgId);
//
//            case BotCommands.BUY_USD_WHITE -> start(chatId, Money.RUB, Money.USDW, DealType.BUY, msgId);
//            case BotCommands.SELL_USD_WHITE -> start(chatId, Money.RUB, Money.USDW, DealType.SELL, msgId);
//
//            case BotCommands.BUY_USDT -> start(chatId, Money.RUB, Money.USDT, DealType.BUY, msgId);
//            case BotCommands.SELL_USDT -> start(chatId, Money.RUB, Money.USDT, DealType.SELL, msgId);

            case "USD" -> {
                User user = UserService.getUser(chatId);
                Deal deal = user.getCurrentDeal();
                if (user.getStatus() == Status.AWAITING_FIRST_CURRENCY) {
                    deal.setMoneyTo(Money.USD);
                    UserService.saveUserStatus(chatId,Status.AWAITING_SECOND_CURRENCY);
                    MessageUtils.editMsg(chatId, user.getMessageToEdit(), "Получение: " + deal.getMoneyTo().getName());
                    MenuService.sendSelectCurrency(chatId, "Выберите валюту выдачи");
                } else if (user.getStatus() == Status.AWAITING_SECOND_CURRENCY) {
                    deal.setMoneyFrom(Money.USD);
                    MessageUtils.editMsg(chatId, user.getMessageToEdit(),  "Выдача: " + deal.getMoneyFrom().getName());
                    UserService.saveUserStatus(chatId,Status.AWAITING_DEAL_AMOUNT);
                    Message msg = MessageUtils.sendText(chatId, "Введите сумму");
                    UserService.addMessageToDel(chatId, msg.getMessageId());
                }
                DealService.saveOrUpdate(deal);
            }

            case "EUR" -> {
                User user = UserService.getUser(chatId);
                Deal deal = user.getCurrentDeal();
                if (user.getStatus() == Status.AWAITING_FIRST_CURRENCY) {
                    deal.setMoneyTo(Money.EUR);
                    UserService.saveUserStatus(chatId,Status.AWAITING_SECOND_CURRENCY);
                    MessageUtils.editMsg(chatId, user.getMessageToEdit(), "Получение: " + deal.getMoneyTo().getName());
                    MenuService.sendSelectCurrency(chatId, "Выберите валюту выдачи");
                } else if (user.getStatus() == Status.AWAITING_SECOND_CURRENCY) {
                    deal.setMoneyFrom(Money.EUR);
                    MessageUtils.editMsg(chatId, user.getMessageToEdit(),  "Выдача: " + deal.getMoneyFrom().getName());
                    UserService.saveUserStatus(chatId, Status.AWAITING_DEAL_AMOUNT);
                    Message msg = MessageUtils.sendText(chatId, "Введите сумму");
                    UserService.addMessageToDel(chatId, msg.getMessageId());
                }
                DealService.saveOrUpdate(deal);
            }

            case BotCommands.BALANCE -> MenuService.sendBalance(chatId);
            case "yes" -> ExchangeProcessor.approve(chatId);
            case "no" -> {
                UserService.saveUserStatus(chatId, Status.IDLE);
                UserService.saveUserCurrentDeal(chatId, null);
                User user = UserService.getUser(chatId);
                user.setCurrentDeal(null);
                user.setStatus(Status.IDLE);
                user.setMessages(null);
                user.setMessageToEdit(null);
                UserService.saveOrUpdate(user);
                MessageUtils.sendText(chatId, "Сделка отменена.");
                MessageUtils.deleteMessages(chatId);
            }
            case "from" -> {
                User user = UserService.getUser(chatId);
                Deal deal = user.getCurrentDeal();

                deal.setAmountFrom(deal.getAmount());
                deal.setAmount((double) Math.round(deal.getAmount() * deal.getExchangeRate()));
                DealService.saveOrUpdate(deal);
                UserService.saveUserStatus(chatId, Status.AWAITING_APPROVE);
                MenuService.sendApproveMenu(chatId);
            }
            case "to" -> {
                User user = UserService.getUser(chatId);
                Deal deal = user.getCurrentDeal();

                deal.setAmountFrom(deal.getAmount() * deal.getExchangeRate());
                DealService.saveOrUpdate(deal);
                UserService.saveUserStatus(chatId, Status.AWAITING_APPROVE);
                MenuService.sendApproveMenu(chatId);
            }
            default -> MessageUtils.sendText(chatId, "Неизвестная команда.");
        }
    }

    private void start(Long chatId, Money from, Money to, DealType dealType, Integer msgId) {
        UserService.startDeal(chatId, from, to, dealType);
        UserService.saveUserStatus(chatId, Status.AWAITING_BUYER_NAME);
        UserService.addMessageToDel(chatId, msgId);
        Message botMsg = MessageUtils.sendText(chatId, BotCommands.ASK_FOR_NAME);
        UserService.addMessageToDel(chatId, botMsg.getMessageId());
    }

}
