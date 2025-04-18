package org.example.handler;

import lombok.RequiredArgsConstructor;
import org.example.handler.state.UserStateHandler;
import org.example.model.*;
import org.example.service.UserService;
import org.example.model.enums.Status;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class MessageHandler {

    private final UserService userService;
    private final Map<Status, UserStateHandler> stateHandlers;

    public void process(Message message) {
        if (message == null || message.getText() == null) return;

        long chatId = message.getChatId();
        User user = userService.getOrCreate(chatId);
        Status status = user.getStatus();

        System.out.println("[" + chatId + "] Статус: " + status + ", Текст: " + message.getText());

        UserStateHandler handler = stateHandlers.get(status);
        if (handler != null) {
            handler.handle(message, user);
        } else {
            System.err.println("Нет обработчика для статуса: " + status);
        }
    }

//    private void initCommandMap() {
//        commandMap.put("Купить Доллар", () -> start(RUB, USD, BUY));
//        commandMap.put("Продать Доллар", () -> start(USD, RUB, SELL));
//        commandMap.put("Купить Евро", () -> start(RUB, EUR, BUY));
//        commandMap.put("Продать Евро", () -> start(EUR, RUB, SELL));
//        commandMap.put("Купить Белый Доллар", () -> start(RUB, USDW, BUY));
//        commandMap.put("Продать Белый Доллар", () -> start(USDW, RUB, SELL));
//        commandMap.put("Купить Tether", () -> start(RUB, USDT, BUY));
//        commandMap.put("Продать Tether", () -> start(USDT, RUB, SELL));
//
//        commandMap.put("Меню", () -> menuService.sendMainMenu(chatId));
//        commandMap.put("/start", () -> menuService.sendMainMenu(chatId));
//
//        commandMap.put("Сложный обмен", () -> customChange(chatId, msgId));
//
//        commandMap.put("+/-", () -> handlePlusMinus(chatId, msgId));
//
//        commandMap.put("Принимаем +", () -> handleChangeBalance(chatId, ChangeBalanceType.GET));
//        commandMap.put("Отдаем +", () -> handleChangeBalance(chatId, ChangeBalanceType.GIVE));
//        commandMap.put("Даем в долг", () -> handleChangeBalance(chatId, ChangeBalanceType.LEND));
//        commandMap.put("Возврат долга", () -> handleChangeBalance(chatId, ChangeBalanceType.DEBT_REPAYMENT));
//
//        commandMap.put("Баланс", () -> menuService.sendBalance(chatId));
//    }
//
//    private void handleChangeBalance(long chatId, ChangeBalanceType changeBalanceType) {
//        switch (changeBalanceType) {
//            case GET -> user.setChangeBalanceType(ChangeBalanceType.GET);
//            case GIVE -> user.setChangeBalanceType(ChangeBalanceType.GIVE);
//            case LEND -> user.setChangeBalanceType(ChangeBalanceType.LEND);
//            case DEBT_REPAYMENT -> user.setChangeBalanceType(ChangeBalanceType.DEBT_REPAYMENT);
//        }
//        user.setStatus(Status.AWAITING_FIRST_CURRENCY);
//        userService.save(user);
//        menuService.sendSelectCurrency(chatId, "Выберите валюту:");
//    }
//
//    private void handlePlusMinus(long chatId, Integer msgId) {
//        userService.addMessageToDel(chatId, msgId);
//        userService.startDeal(chatId, null, null, CHANGE_BALANCE);
//        userService.saveUserStatus(chatId, Status.AWAITING_BUYER_NAME);
//        telegramSender.sendText(chatId, "Введите имя:");
//    }
//
//    private void handleIdleState(String text) {
//        Runnable command = commandMap.get(text);
//        if (command != null) {
//            command.run();
//        } else {
//            menuService.sendMainMenu(chatId);
//        }
//    }
//
//    private void handleExchangeRate(User user, String text) {
//        if (user.getCurrentDeal().getDealType() == CUSTOM) {
//            double rate = Double.parseDouble(text.replace(",", "."));
//            user.getCurrentDeal().setExchangeRate(rate);
//            user.setStatus(Status.AWAITING_APPROVE);
//
//            Double amountTo = user.getCurrentDeal().getAmountTo();
//            Double amountFrom = user.getCurrentDeal().getAmountFrom();
//
//            if (user.getAmountType() == AmountType.GIVE) {
//                if (user.getCurrencyType() == CurrencyType.DIVISION) {
//                    user.getCurrentDeal().setAmountTo(amountFrom / rate);
//                } else if (user.getCurrencyType() == CurrencyType.MULTIPLICATION) {
//                    user.getCurrentDeal().setAmountTo(amountFrom * rate);
//                }
//            } else if (user.getAmountType() == AmountType.RECEIVE) {
//                if (user.getCurrencyType() == CurrencyType.DIVISION) {
//                    user.getCurrentDeal().setAmountFrom(amountTo / rate);
//                } else if (user.getCurrencyType() == CurrencyType.MULTIPLICATION) {
//                    user.getCurrentDeal().setAmountFrom(amountTo * rate);
//                }
//            }
//
//            userService.save(user);
//            userService.addMessageToDel(chatId, msgId);
//            menuService.sendApproveMenu(chatId);
//        } else {
//            try {
//                double rate = Double.parseDouble(text.replace(",", "."));
//                if (user.getCurrentDeal().getDealType() == SELL) {
//                    user.getCurrentDeal().setAmountTo((double) Math.round(user.getCurrentDeal().getAmountFrom() * rate));
//                } else if (user.getCurrentDeal().getDealType() == BUY) {
//                    user.getCurrentDeal().setAmountFrom((double) Math.round(user.getCurrentDeal().getAmountTo() * rate));
//                }
//                user.getCurrentDeal().setExchangeRate(rate);
//                user.setStatus(Status.AWAITING_APPROVE);
//                userService.save(user);
//                menuService.sendApproveMenu(chatId);
//                userService.addMessageToDel(chatId, msgId);
//            } catch (NumberFormatException e) {
//                Message botMsg = telegramSender.sendText(chatId, "Неверный формат курса.");
//                userService.addMessageToDel(chatId, botMsg.getMessageId());
//            }
//        }
//    }
//
//
//    private void start(Money from, Money to, DealType dealType) {
//        userService.startDeal(chatId, from, to, dealType);
//        userService.saveUserStatus(chatId, Status.AWAITING_BUYER_NAME);
//        userService.addMessageToDel(chatId, msgId);
//        Message botMsg = telegramSender.sendText(chatId, BotCommands.ASK_FOR_NAME);
//        userService.addMessageToDel(chatId, botMsg.getMessageId());
//    }
//
//    private void handleDealAmount(String text) {
//        try {
//            double amount = Double.parseDouble(text);
//
//            if (user.getCurrentDeal().getDealType() == CUSTOM) {
//                user.setStatus(Status.AWAITING_SELECT_AMOUNT);
//                user.getCurrentDeal().setCurrentAmount(amount);
//                userService.save(user);
//                Message msg = menuService.sendSelectAmountType(chatId);
//                user.setMessageToEdit(msg.getMessageId());
//                userService.save(user);
//                userService.addMessageToDel(chatId, msg.getMessageId());
//            } else if (user.getCurrentDeal().getDealType() == SELL || user.getCurrentDeal().getDealType() == BUY) {
//                user.setStatus(Status.AWAITING_EXCHANGE_RATE);
//
//                if (user.getCurrentDeal().getDealType() == SELL) {
//                    user.getCurrentDeal().setAmountFrom(amount);
//                } else if (user.getCurrentDeal().getDealType() == BUY) {
//                    user.getCurrentDeal().setAmountTo(amount);
//                }
//
//                userService.save(user);
//                Message message = telegramSender.sendText(chatId, "Введите курс: ");
//                userService.addMessageToDel(chatId, message.getMessageId());
//            } else if (user.getCurrentDeal().getDealType() == CHANGE_BALANCE) {
//                user.getCurrentDeal().setAmountTo(amount);
//                user.setStatus(Status.AWAITING_APPROVE);
//                userService.save(user);
//                menuService.sendApproveMenu(chatId);
//            }
//
//            userService.addMessageToDel(chatId, msgId);
//        } catch (NumberFormatException e) {
//            Message botMsg = telegramSender.sendText(chatId, "Неверный формат суммы.");
//            userService.addMessageToDel(chatId, botMsg.getMessageId());
//        }
//    }
//
//    private void customChange(Long chatId, Integer msgId) {
//        userService.saveUserStatus(chatId, Status.AWAITING_BUYER_NAME);
//        Deal deal = new Deal();
//        deal.setDealType(CUSTOM);
//        userService.saveUserCurrentDeal(chatId, deal);
//        userService.addMessageToDel(chatId, msgId);
//        Message botMsg = telegramSender.sendText(chatId, BotCommands.ASK_FOR_NAME);
//        userService.addMessageToDel(chatId, botMsg.getMessageId());
//    }

    //    private void handleBuyerName(User user, String text) {
//        userService.saveBuyerName(chatId, text);
//        userService.addMessageToDel(chatId, msgId);
//
//        if (user.getCurrentDeal().getDealType() == BUY || user.getCurrentDeal().getDealType() == SELL || user.getCurrentDeal().getDealType() == CUSTOM) {
//            if (user.getCurrentDeal().getDealType() == CUSTOM) {
//                userService.saveUserStatus(chatId, Status.AWAITING_FIRST_CURRENCY);
//               // userService.addMessageToDel(chatId, msgId);
//                menuService.sendSelectCurrency(chatId, "Выберите валюту получения");
//            } else {
//                userService.saveUserStatus(chatId, Status.AWAITING_DEAL_AMOUNT);
//                if (user.getCurrentDeal().getDealType() == SELL) {
//                    Message botMsg = telegramSender.sendText(chatId, "Введите сумму в %s:".formatted(user.getCurrentDeal().getMoneyFrom().getName()));
//                   // userService.addMessageToDel(chatId, botMsg.getMessageId());
//                } else if (user.getCurrentDeal().getDealType() == BUY) {
//                    Message botMsg = telegramSender.sendText(chatId, "Введите сумму в %s:".formatted(user.getCurrentDeal().getMoneyTo().getName()));
//                   // userService.addMessageToDel(chatId, botMsg.getMessageId());
//                }
//                //userService.addMessageToDel(chatId, msgId);
//            }
//        } else if (user.getCurrentDeal().getDealType() == CHANGE_BALANCE) {
//            //userService.addMessageToDel(chatId, msgId);
//            userService.saveUserStatus(chatId, Status.AWAITING_CHANGE_BALANCE_TYPE);
//            menuService.sendChangeBalanceMenu(chatId);
//        }
//    }

}
