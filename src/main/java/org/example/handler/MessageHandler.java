package org.example.handler;

import org.example.constants.BotCommands;
import org.example.model.Deal;
import org.example.model.DealType;
import org.example.model.Money;
import org.example.state.Status;
import org.example.model.User;
import org.example.service.UserService;
import org.example.util.MessageUtils;
import org.example.ui.MenuService;
import org.telegram.telegrambots.meta.api.objects.Message;

public class MessageHandler {

    public void handle(Message message) {
        long chatId = message.getChatId();
        String text = message.getText();
        Integer msgId = message.getMessageId();
        User user;

        if (UserService.getUser(chatId) == null) {
            user = new User();
            user.setChatId(chatId);
            user.setStatus(Status.IDLE);
            UserService.save(user);
        } else {
            user = UserService.getUser(chatId);
        }

        Status status = UserService.getStatus(chatId);

        switch (status) {

            case IDLE -> {
                switch (text) {
                    case "Меню", "/start":
                        MenuService.sendMainMenu(chatId);
                        break;
                    case "Купить Доллар":
                        start(chatId, Money.RUB, Money.USD, DealType.BUY, msgId);
                        break;
                    case "Продать Доллар":
                        start(chatId, Money.RUB, Money.USD, DealType.SELL, msgId);
                        break;
                    case "Купить Евро":
                        start(chatId, Money.RUB, Money.EUR, DealType.BUY, msgId);
                        break;
                    case "Продать Евро":
                        start(chatId, Money.RUB, Money.EUR, DealType.SELL, msgId);
                        break;
                    case "Купить Белый Доллар":
                        start(chatId, Money.RUB, Money.USDW, DealType.BUY, msgId);
                        break;
                    case "Продать Белый Доллар":
                        start(chatId, Money.RUB, Money.USDW, DealType.SELL, msgId);
                        break;
                    case "Купить Tether":
                        start(chatId, Money.RUB, Money.USDT, DealType.BUY, msgId);
                        break;
                    case "Продать Tether":
                        start(chatId, Money.RUB, Money.USDT, DealType.SELL, msgId);
                        break;
                    case "Сложный обмен":
                        customChange(chatId, msgId);
                        break;
                    case "Баланс":
                        MenuService.sendBalance(chatId);
                        break;
                    default:
                        MessageUtils.sendText(chatId, "Неизвестная команда.");
                }
            }

            case AWAITING_BUYER_NAME -> {
                UserService.saveBuyerName(chatId, text);
                if (user.getCurrentDeal().getIsCustom()) {
                    UserService.saveUserStatus(chatId, Status.AWAITING_FIRST_CURRENCY);
                    UserService.addMessageToDel(chatId, message.getMessageId());
                    MenuService.sendSelectCurrency(chatId, "Выберите валюту получения");
                } else {
                    UserService.saveUserStatus(chatId, Status.AWAITING_DEAL_AMOUNT);
                    Message botMsg = MessageUtils.sendText(chatId, "Введите сумму в %s:".formatted(user.getCurrentDeal().getMoneyTo().getName()));
                    UserService.addMessageToDel(chatId, botMsg.getMessageId());
                    UserService.addMessageToDel(chatId, message.getMessageId());
                }
            }

            case AWAITING_DEAL_AMOUNT -> {
                try {
                    double amount = Double.parseDouble(text);
                    UserService.saveTransactionAmount(chatId, amount);
                    UserService.saveUserStatus(chatId, Status.AWAITING_EXCHANGE_RATE);
                    Message botMsg = MessageUtils.sendText(chatId, "Введите курс: %s -> %s".formatted(user.getCurrentDeal().getMoneyFrom(), user.getCurrentDeal().getMoneyTo()));
                    UserService.addMessageToDel(chatId, botMsg.getMessageId());
                    UserService.addMessageToDel(chatId, message.getMessageId());

                } catch (NumberFormatException e) {
                    Message botMsg = MessageUtils.sendText(chatId, "Неверный формат суммы.");
                    UserService.addMessageToDel(chatId, botMsg.getMessageId());
                }
            }

            case AWAITING_EXCHANGE_RATE -> {
                if (user.getCurrentDeal().getIsCustom()) {
                    double rate = Double.parseDouble(text.replace(",", "."));
                    UserService.saveExchangeRate(chatId, rate);
                    UserService.saveUserStatus(chatId, Status.AWAITING_CUSTOM_APPROVE);
                    UserService.addMessageToDel(chatId, message.getMessageId());
                    MenuService.sendCustomSelectAmount(chatId);
                } else {
                    try {
                        double rate = Double.parseDouble(text.replace(",", "."));
                        user.getCurrentDeal().setAmountFrom((double) Math.round(user.getCurrentDeal().getAmount() * rate));
                        user.getCurrentDeal().setExchangeRate(rate);
                        user.setStatus(Status.AWAITING_APPROVE);
                        UserService.saveOrUpdate(user);
                        MenuService.sendApproveMenu(chatId);
                        UserService.addMessageToDel(chatId, message.getMessageId());
                    } catch (NumberFormatException e) {
                        Message botMsg = MessageUtils.sendText(chatId, "Неверный формат курса.");
                        UserService.addMessageToDel(chatId, botMsg.getMessageId());
                    }
                }
            }

            case AWAITING_CUSTOM_APPROVE -> {
            }

            case AWAITING_FIRST_CURRENCY -> {
            }

            case AWAITING_SECOND_CURRENCY -> {
            }

            default -> {
                //MenuService.sendMainMenu(chatId);
            }
        }
    }

    private void start(Long chatId, Money from, Money to, DealType dealType, Integer msgId) {
        UserService.startDeal(chatId, from, to, dealType);
        UserService.saveUserStatus(chatId, Status.AWAITING_BUYER_NAME);
        UserService.addMessageToDel(chatId, msgId);
        Message botMsg = MessageUtils.sendText(chatId, BotCommands.ASK_FOR_NAME);
        UserService.addMessageToDel(chatId, botMsg.getMessageId());
    }

    private void customChange(Long chatId, Integer msgId) {
        UserService.saveUserStatus(chatId, Status.AWAITING_BUYER_NAME);
        Deal deal = new Deal();
        deal.setIsCustom(true);
        deal.setDealType(DealType.BUY);
        UserService.saveUserCurrentDeal(chatId, deal);
        UserService.addMessageToDel(chatId, msgId);
        Message botMsg = MessageUtils.sendText(chatId, BotCommands.ASK_FOR_NAME);
        UserService.addMessageToDel(chatId, botMsg.getMessageId());
    }

}
