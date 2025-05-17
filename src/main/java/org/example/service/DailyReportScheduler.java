package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.infra.TelegramSenderImpl;
import org.example.model.Deal;
import org.example.model.enums.SettingKey;
import org.example.repository.DealRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyReportScheduler {

    private final DealRepository dealRepository;
    private final ExcelReportService excelReportService;
    private final TelegramSenderImpl telegramSender;
    private final SettingService settingService;

    @Scheduled(cron = "0 0 21 * * *")
    public void sendDailyReport() {
        long chatId = settingService.getLong(SettingKey.REPORT_CHAT_ID);
        // текущая дата
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay().minusNanos(1);

        // достаём сделки за текущий день (по полю createdAt)
        List<Deal> todaysDeals = dealRepository.findAllByCreatedAtBetween(startOfDay, endOfDay);

        if (todaysDeals.isEmpty()) {
            telegramSender.sendText(chatId, "Нет сделок за " + today);
            return;
        }

        byte[] report = excelReportService.generateDealReport(todaysDeals);

        InputFile inputFile = new InputFile(new ByteArrayInputStream(report), "Отчет_" + today + ".xlsx");

        telegramSender.sendDocument(chatId, inputFile, "Отчет за " + today);
    }
}
