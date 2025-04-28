package org.example.infra;

import lombok.extern.slf4j.Slf4j;
import org.example.bot.ObmenBot;
import org.example.service.UserService;
import org.example.ui.MenuService;
import org.example.util.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class TelegramSenderImpl implements TelegramSender {

    private ObmenBot obmenBot;
    private MenuService menuService;
    private UserService userService;
    private MessageUtils messageUtils;

    @Autowired
    public void setObmenBot(ObmenBot obmenBot) {
        this.obmenBot = obmenBot;
    }

    @Autowired
    public void setMenuService(MenuService menuService) {
        this.menuService = menuService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setMessageUtils(MessageUtils messageUtils) {
        this.messageUtils = messageUtils;
    }

    @Override
    public Message send(SendMessage message) {
        try {
            return obmenBot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message", e);
        }
        return null;
    }

    @Override
    public Message sendText(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), messageUtils.escapeMarkdown(text));
        message.setParseMode("MarkdownV2");
        Message returnMsg;
        try {
            returnMsg = obmenBot.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        return returnMsg;
    }

    @Override
    public void editMsg(Long chatId, Integer messageToEdit, String s) {
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageToEdit);
        message.setText(messageUtils.escapeMarkdown(s));
        message.setParseMode("MarkdownV2");
        try {
            obmenBot.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void editMsgWithKeyboard(Long chatId, Integer messageToEdit, String s) {
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageToEdit);
        message.setText(s);
        message.setReplyMarkup(menuService.addBackCancelButtons(menuService.createFullCurrencyKeyboard()));
        try {
            obmenBot.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendTextWithKeyboard(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), messageUtils.escapeMarkdown(text));
        message.setReplyMarkup(menuService.addBackCancelButtons(null));
        message.setParseMode("MarkdownV2");
        Message returnMsg;
        try {
            returnMsg = obmenBot.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        userService.addMessageToDel(chatId, returnMsg.getMessageId());
        userService.addMessageToEdit(chatId, returnMsg.getMessageId());
    }

    @Override
    public void deleteMessage(Long chatId, Integer messageToDelete) {
        try {
            obmenBot.deleteMessage(new DeleteMessage(chatId.toString(), messageToDelete));
            System.out.println("Сообщение удалено " + messageToDelete);
        } catch (TelegramApiException e) {
            System.out.println("Ошибка удаления сообщения " + messageToDelete);
        }
    }

    public Message sendInlineKeyboard(Long chatId, String text, InlineKeyboardMarkup markup) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .replyMarkup(markup)
                .build();
        try {
            return obmenBot.execute(sendMessage);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
