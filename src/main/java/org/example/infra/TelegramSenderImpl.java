package org.example.infra;

import lombok.extern.slf4j.Slf4j;
import org.example.bot.ObmenBot;
import org.example.service.UserService;
import org.example.ui.InlineKeyboardBuilder;
import org.example.ui.MenuService;
import org.example.util.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.util.List;

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

    public void editMessageReplyMarkup(long chatId, int messageId, InlineKeyboardMarkup markup) {
        EditMessageReplyMarkup edit = EditMessageReplyMarkup.builder()
                .chatId(String.valueOf(chatId))
                .messageId(messageId)
                .replyMarkup(markup)
                .build();
        try {
            obmenBot.execute(edit);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }    }

    public Message sendWithMarkup(long chatId, String text, InlineKeyboardMarkup markup) {
        SendMessage msg = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(messageUtils.escapeMarkdown(text))
                .replyMarkup(markup)
                .parseMode("MarkdownV2")
                .build();
        try {
            return obmenBot.execute(msg);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public Message sendWithMarkup(long chatId, String text, InlineKeyboardMarkup markup, String parseMode) {
        SendMessage.SendMessageBuilder builder = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .replyMarkup(markup)
                .parseMode(parseMode);

        if ("MarkdownV2".equals(parseMode)) {
            builder.text(messageUtils.escapeMarkdown(text));
        } else {
            // любые другие режимы (обычно "HTML") — передаём текст без эскейпа
            builder.text(text);
        }

        SendMessage msg = builder.build();
        try {
            return obmenBot.execute(msg);
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

    public void deleteMessages(long chatId, List<Integer> ids) {
        ids.forEach(
                x -> deleteMessage(chatId, x)
        );
    }

    /**
     * Правит клавиатуру у уже отправленного сообщения.
     */
    public void editReplyMarkup(long chatId, int messageId, InlineKeyboardMarkup markup) {
        EditMessageReplyMarkup edit = EditMessageReplyMarkup.builder()
                .chatId(String.valueOf(chatId))
                .messageId(messageId)
                .replyMarkup(markup)
                .build();

        try {
            obmenBot.execute(edit);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка редактирования разметки", e);
        }
    }

    public void sendExcelReport(long chatId, byte[] data, String filename) {
        SendDocument doc = SendDocument.builder()
                .chatId(String.valueOf(chatId))
                .document(new InputFile(new ByteArrayInputStream(data), filename))
                .caption("Отчёт по сделкам")
                .build();

        try {
            obmenBot.execute(doc);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка отправки файла", e);
        }
    }
}
