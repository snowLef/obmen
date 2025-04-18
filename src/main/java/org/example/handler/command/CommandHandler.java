package org.example.handler.command;

@FunctionalInterface
public interface CommandHandler {
    void handle(CommandContext context);
}