package org.example.interfaces;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateProcessor {
    // Оставляем только методы обработки обновлений
    void process(Update update);
}
