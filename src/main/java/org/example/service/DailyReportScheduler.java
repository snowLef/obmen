package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.infra.TelegramSender;
import org.example.infra.TelegramSenderImpl;
import org.example.model.Deal;
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

    // ID чата/группы, куда отправлять отчёт
    private final long REPORT_CHAT_ID = -1002619678847L;

    @Scheduled(cron = "0 0 22 * * *") // каждый день в 22:00
public void sendDailyReport() {
        // текущая дата
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay().minusNanos(1);

        // достаём сделки за текущий день (по полю createdAt)
        List<Deal> todaysDeals = dealRepository.findAllByCreatedAtBetween(startOfDay, endOfDay);

        if (todaysDeals.isEmpty()) {
            telegramSender.sendText(REPORT_CHAT_ID, "Нет сделок за " + today);
            return;
        }

        byte[] report = excelReportService.generateDealReport(todaysDeals);

        InputFile inputFile = new InputFile(new ByteArrayInputStream(report), "Отчет_" + today + ".xlsx");

        telegramSender.sendDocument(REPORT_CHAT_ID, inputFile, "Отчет за " + today);
    }
}
