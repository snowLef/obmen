package org.example.handler.state;

import org.example.handler.command.CommandContext;
import org.example.handler.command.CommandHandler;
import org.example.model.User;
import org.example.model.enums.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Map;

@Component
public class IdleStateHandler implements UserStateHandler {

    private Map<String, CommandHandler> commandMap;

    @Autowired
    public void setCommandMap(Map<String, CommandHandler> commandMap) {
        this.commandMap = commandMap;
    }


    @Override
    public void handle(Message message, User user) {
        var context = new CommandContext(message);
        CommandHandler handler = commandMap.get(message.getText());

        if (handler != null) {
            handler.handle(context);
        } else {
            // Например, fallback или вывод меню
        }
    }

    @Override
    public Status getSupportedStatus() {
        return Status.IDLE;
    }
}

