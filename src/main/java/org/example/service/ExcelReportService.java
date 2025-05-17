package org.example.service;

import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.example.model.CurrencyAmount;
import org.example.model.Deal;
import org.example.model.enums.DealStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
public class ExcelReportService {

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Генерирует XLSX-отчет по списку сделок.
     *
     * @param deals все сделки (включая отмененные)
     * @return содержимое файла в виде byte[]
     */
    public byte[] generateDealReport(List<Deal> deals) {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet("Сделки");

            String[] headers = {
                    "№ сделки", "Создал, автор", "Дата/время", "Операция", "Имя клиента",
                    "Получено, сумма", "Получено, валюта", "Получено, баланс",
                    "Курс",
                    "Выдано, сумма", "Выдано, валюта", "Выдано, баланс",
                    "Комментарий", "Отменено", "Дата/время отмены", "Автор отмены", "Провел, автор"
            };

            // Стили
            XSSFCellStyle boldStyle = wb.createCellStyle();
            XSSFFont boldFont = wb.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);

            XSSFCellStyle grayStyle = wb.createCellStyle();
            XSSFColor gray = new XSSFColor(new java.awt.Color(240, 240, 240), null);
            grayStyle.setFillForegroundColor(gray);
            grayStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Заголовок
            XSSFRow header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                XSSFCell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(boldStyle);
            }

            // Автофильтр (со 2-го столбца до последнего)
            sheet.setAutoFilter(new CellRangeAddress(0, 0, 1, headers.length - 1));

            int rowIdx = 1;
            int dealIndex = 0;

            deals.sort(Comparator.comparing(Deal::getId).reversed());

            for (Deal d : deals) {
                if (d.getStatus() == DealStatus.NEW || d.getStatus() == DealStatus.FIX) continue;

                int maxCount = Math.max(d.getMoneyFrom().size(), d.getMoneyTo().size());
                boolean useGray = dealIndex % 2 == 0;
                dealIndex++;

                for (int i = 0; i < maxCount; i++) {
                    XSSFRow row = sheet.createRow(rowIdx++);

                    // стиль строки (если нужно)
                    XSSFCellStyle rowStyle = null;
                    if (useGray) {
                        rowStyle = wb.createCellStyle();
                        rowStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                        rowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    }

                    // 0. Номер сделки
                    XSSFCell cell0 = row.createCell(0);
                    cell0.setCellValue("%s/%s".formatted(d.getCreatedAt().format(DateTimeFormatter.ofPattern("MMdd")), d.getId()));
                    if (rowStyle != null) cell0.setCellStyle(rowStyle);

                    // 1. Автор
                    XSSFCell cell1 = row.createCell(1);
                    cell1.setCellValue(d.getCreatedBy());
                    if (rowStyle != null) cell1.setCellStyle(rowStyle);

                    // 2. Дата/время
                    XSSFCell cell2 = row.createCell(2);
                    cell2.setCellValue(d.isApproved() ? dtf.format(d.getCreatedAt()) : "");
                    if (rowStyle != null) cell2.setCellStyle(rowStyle);

                    // 3. Операция
                    XSSFCell cell3 = row.createCell(3);
                    cell3.setCellValue(d.getDealType() == null ? "" : d.getDealType().getType());
                    if (rowStyle != null) cell3.setCellStyle(rowStyle);

                    // 4. Имя клиента
                    XSSFCell cell4 = row.createCell(4);
                    cell4.setCellValue(d.getBuyerName() != null ? d.getBuyerName() : "");
                    if (rowStyle != null) cell4.setCellStyle(rowStyle);

                    // 5-7. Получено
                    if (i < d.getMoneyTo().size()) {
                        CurrencyAmount ca = d.getMoneyTo().get(i);
                        row.createCell(5).setCellValue(ca.getAmount());
                        row.createCell(6).setCellValue(ca.getCurrency().name());
                        row.createCell(7).setCellValue(d.getBalanceTypeFrom().getDisplayName());
                    } else {
                        row.createCell(5).setCellValue("");
                        row.createCell(6).setCellValue("");
                        row.createCell(7).setCellValue("");
                    }
                    for (int col = 5; col <= 7; col++) {
                        if (rowStyle != null) row.getCell(col).setCellStyle(rowStyle);
                    }

                    // 8. Курс
                    XSSFCell cell8 = row.createCell(8);
                    cell8.setCellValue(d.getExchangeRate() != null ? d.getExchangeRate() : 0.0);
                    if (rowStyle != null) cell8.setCellStyle(rowStyle);

                    // 9-11. Выдано
                    if (i < d.getMoneyFrom().size()) {
                        CurrencyAmount ca = d.getMoneyFrom().get(i);
                        row.createCell(9).setCellValue(ca.getAmount());
                        row.createCell(10).setCellValue(ca.getCurrency().name());
                        row.createCell(11).setCellValue(d.getBalanceTypeTo().getDisplayName());
                    } else {
                        row.createCell(9).setCellValue("");
                        row.createCell(10).setCellValue("");
                        row.createCell(11).setCellValue("");
                    }
                    for (int col = 9; col <= 11; col++) {
                        if (rowStyle != null) row.getCell(col).setCellStyle(rowStyle);
                    }

                    // 12. Комментарий
                    XSSFCell cell12 = row.createCell(12);
                    cell12.setCellValue(d.getComment() != null ? d.getComment() : "");
                    if (rowStyle != null) cell12.setCellStyle(rowStyle);

                    // 13. Отменено
                    XSSFCell cell13 = row.createCell(13);
                    cell13.setCellValue((!d.isApproved() && d.getStatus().name().equals("CANCELLED")) ? "Да" : "");
                    if (rowStyle != null) cell13.setCellStyle(rowStyle);

                    // 14. Дата/время отмены
                    XSSFCell cell14 = row.createCell(14);
                    cell14.setCellValue(d.getCancelledAt() != null ? dtf.format(d.getCancelledAt()) : "");
                    if (rowStyle != null) cell14.setCellStyle(rowStyle);

                    // 15. Автор отмены
                    XSSFCell cell15 = row.createCell(15);
                    cell15.setCellValue(d.getCancelledBy() != null ? d.getCancelledBy() : "");
                    if (rowStyle != null) cell15.setCellStyle(rowStyle);


                    XSSFCell cell16 = row.createCell(16);
                    cell16.setCellValue(d.getApprovedBy());
                    if (rowStyle != null) cell16.setCellStyle(rowStyle);
                }

            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
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
