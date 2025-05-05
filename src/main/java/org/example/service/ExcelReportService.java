package org.example.service;

import org.apache.poi.xssf.usermodel.*;
import org.example.model.CurrencyAmount;
import org.example.model.Deal;
import org.example.model.enums.BalanceType;
import org.example.model.enums.DealStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelReportService {

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Генерирует XLSX-отчет по списку сделок.
     * @param deals все сделки (включая отмененные)
     * @return содержимое файла в виде byte[]
     */
    public byte[] generateDealReport(List<Deal> deals) {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet("Сделки");

            // 2.1 Шапка
            String[] headers = {
                    "№ сделки","Автор (ник)","Дата/время","Операция","Имя клиента",
                    "Получено, сумма","Получено, валюта","Получено, баланс",
                    "Курс",
                    "Выдано, сумма","Выдано, валюта","Выдано, баланс",
                    "Комментарий","Отменено","Дата/время отмены","Автор отмены"
            };
            XSSFRow header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
                sheet.autoSizeColumn(i);
            }

            // 2.2 Данные
            int rowIdx = 1;
            for (Deal d : deals) {

                if (d.getStatus() == DealStatus.NEW) continue;

                XSSFRow row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(d.getId());

                row.createCell(1).setCellValue(
                        d.getBuyerName() != null ? d.getBuyerName() : ""
                );

                row.createCell(2).setCellValue(
                        d.isApproved()
                                ? dtf.format(d.getCreatedAt())
                                : ""
                );
                row.createCell(3).setCellValue(d.getDealType().name());

                row.createCell(4).setCellValue(
                        d.getBuyerName() != null ? d.getBuyerName() : ""
                );

                CurrencyAmount from = d.getMoneyFrom().isEmpty() ? null : d.getMoneyFrom().get(0);
                CurrencyAmount to   = d.getMoneyTo().isEmpty()   ? null : d.getMoneyTo().get(0);

                if (from != null) {
                    row.createCell(5).setCellValue(from.getAmount());
                    row.createCell(6).setCellValue(from.getCurrency().name());
                    row.createCell(7).setCellValue(d.getBalanceTypeFrom().name());
                }

                row.createCell(8).setCellValue(
                        d.getExchangeRate() != null ? d.getExchangeRate() : 0.0
                );

                if (to != null) {
                    row.createCell(9).setCellValue(to.getAmount());
                    row.createCell(10).setCellValue(to.getCurrency().name());
                    row.createCell(11).setCellValue(d.getBalanceTypeTo().name());
                }

                row.createCell(12).setCellValue(
                        d.getComment() != null ? d.getComment() : ""
                );

                row.createCell(13).setCellValue(
                        (!d.isApproved() && d.getStatus().name().equals("CANCELLED")) ? "Да" : "Нет"
                );

                row.createCell(14).setCellValue(
                        d.getCancelledAt() != null
                                ? dtf.format(d.getCancelledAt())
                                : "-"
                );
                row.createCell(15).setCellValue(
                        d.getCancelledBy() != null
                                ? d.getCancelledBy()
                                : ""
                );

                // авторазмер колонок
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                wb.write(bos);
                return bos.toByteArray();
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при формировании Excel", e);
        }
    }
}
