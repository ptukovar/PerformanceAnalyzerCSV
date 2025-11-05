package com.ptukovar;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
/*
*
* Jako Volitelé – Nepovinné rozšíření řešení jsem přidal možnost generování grafu průměrných měsíčních výkonů
* v zadaném období a pokud je zadán 3. parametr DAY tak se vygeneruje graf průmerných denních výkonů.
 *
* */
public class Main {
    private static String getNameOfDay(int dayOfWeek) {
        return switch (dayOfWeek) {
            case 1 -> "Pondělí";
            case 2 -> "Úterý";
            case 3 -> "Středa";
            case 4 -> "Čtvrtek";
            case 5 -> "Pátek";
            case 6 -> "Sobota";
            case 7 -> "Neděle";
            default -> throw new IllegalArgumentException("Neznámý den: " + dayOfWeek);
        };
    }
    final static String FILE_PATH = "src/main/resources/dataexport.csv";
    final static Logger logger = Logger.getLogger(Main.class.getName());
    final static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmm");

    public static void main(String[] args) throws IOException {
        if(args.length < 2) {
            System.out.println("Nebyly zadány všechny potřebné argumenty \n[0] - FROM \n[1] - TO \n[2] - DAY (volitelné)");
            System.exit(0);
        } else if (args.length > 3) {
            System.out.println("Bylo zadáno příliš mnoho argumentů");
            System.exit(0);
        }

        Map<String, Float> chartData = new HashMap<>();
        if(args.length == 2) {
            try{
                LocalDateTime dateTimeFrom = LocalDateTime.parse(args[0] + "01T0000", DATE_FORMATTER);
                LocalDate dateTo = YearMonth.parse(args[1], DateTimeFormatter.ofPattern("yyyyMM")).atEndOfMonth();
                LocalDateTime dateTimeTo = dateTo.atTime(23,59);
                if(dateTimeFrom.isAfter(dateTimeTo)) {
                    System.out.println("FROM nesmí být větší než TO");
                    System.exit(0);
                }

                try(BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
                    br.readLine();
                    String line;
                    float totalSum = 0;
                    float totalCount = 0;
                    float valueMonths = 0;
                    int currentMonth = 0;
                    int prevMonth = 0;
                    int countMonths = 0;

                    while ((line = br.readLine()) != null) {
                        String[] values = line.split(",");
                        String dateStr = values[0];
                        try{
                            LocalDateTime date = LocalDateTime.parse(dateStr, DATE_FORMATTER);
                            if (!date.isBefore(dateTimeFrom) && !date.isAfter(dateTimeTo)) {
                                String strValue = values[1];
                                if(!strValue.isEmpty()) {
                                    currentMonth = date.getMonth().getValue();
                                    if(prevMonth == 0) {
                                        prevMonth = currentMonth;
                                    }
                                    if(currentMonth != prevMonth) {
                                        logger.info("Průměr za " + (prevMonth) + ". měsíc: " + (valueMonths / countMonths));
                                        chartData.put(prevMonth + ". " + date.getYear(), valueMonths / countMonths);
                                        valueMonths = 0;
                                        countMonths = 0;
                                        prevMonth = currentMonth;
                                    }
                                    float value = Float.parseFloat(strValue);
                                    valueMonths += value;
                                    countMonths++;
                                    totalCount++;
                                    totalSum += value;
                                }
                            }
                        }catch (Exception e) {
                        }
                    }
                    if(countMonths > 0) {
                        logger.info("Průměr za " + (prevMonth) + ". měsíc: " + (valueMonths / countMonths));
                        chartData.put(prevMonth + ". " + dateTimeTo.getYear(), valueMonths / countMonths);
                    }
                    if(totalCount > 0) {
                        logger.info("Průměr za celé období: " + (totalSum / totalCount));
                    }

                }catch (Exception e) {
                    System.out.println("Chyba při čtení souboru: " + e.getMessage());
                    System.exit(0);
                }
            }catch (Exception e) {
                System.out.println("Chybný formát argumentů yyyyMM");
                System.out.println(e.getMessage());
                System.exit(0);
            }

            DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
            for(Map.Entry<String, Float> entry : chartData.entrySet()) {
                dataset.addValue(entry.getValue(), "Průměr výkon", entry.getKey());
            }

            JFreeChart chart = ChartFactory.createLineChart(
                    "Průměrné měsíční výkony",
                    "Měsíc",
                    "Průměr výkon",
                    dataset,
                    org.jfree.chart.plot.PlotOrientation.VERTICAL,
                    true,true,false);

            ChartUtilities.saveChartAsPNG(new File("average_per_month.png"), chart, 1920, 1080);

        }else if(args.length == 3) {
            try{
                int dayOfWeek = Integer.parseInt(args[2]);
                if(dayOfWeek < 1 || dayOfWeek > 7) {
                    System.out.println("Neplatný argument DAY (1 - pondělí - 7 - neděle)");
                    System.exit(0);
                }

                LocalDateTime dateTimeFrom = LocalDateTime.parse(args[0] + "01T0000", DATE_FORMATTER);
                LocalDate dateTo = YearMonth.parse(args[1], DateTimeFormatter.ofPattern("yyyyMM")).atEndOfMonth();
                LocalDateTime dateTimeTo = dateTo.atTime(23,59);

                if(dateTimeFrom.isAfter(dateTimeTo)) {
                    System.out.println("FROM nesmí být větší než TO");
                    System.exit(0);
                }

                try(BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
                    br.readLine();
                    String line;
                    float totalSum = 0;
                    float totalCount = 0;
                    float monthSum = 0;
                    int monthCount = 0;
                    int prevMonth = 0;
                    float daySum = 0;
                    int dayCount = 0;
                    LocalDate prevDate = null;

                    while ((line = br.readLine()) != null) {
                        String[] values = line.split(",");
                        String dateStr = values[0];
                        try{
                            LocalDateTime date = LocalDateTime.parse(dateStr, DATE_FORMATTER);
                            if (!date.isBefore(dateTimeFrom) && !date.isAfter(dateTimeTo)) {
                                int currentMonth = date.getMonthValue();
                                if(prevMonth == 0) prevMonth = currentMonth;
                                if(currentMonth != prevMonth) {
                                    if(monthCount > 0) {
                                        logger.info("Průměr za " + prevMonth + ". měsíc: " + (monthSum / monthCount));
                                    }
                                    monthSum = 0;
                                    monthCount = 0;
                                    prevMonth = currentMonth;
                                }

                                if(date.getDayOfWeek().getValue() == dayOfWeek) {
                                    String strValue = values[1];
                                    if(!strValue.isEmpty()) {
                                        float value = Float.parseFloat(strValue);
                                        LocalDate currentDate = date.toLocalDate();

                                        if(prevDate == null) prevDate = currentDate;
                                        if(!currentDate.equals(prevDate)) {
                                            logger.info("Výkon pro "+ getNameOfDay(dayOfWeek)  +" "   + prevDate + ": " + (daySum / dayCount));
                                            chartData.put(prevDate.toString(), daySum / dayCount);
                                            daySum = 0;
                                            dayCount = 0;
                                            prevDate = currentDate;
                                        }

                                        daySum += value;
                                        dayCount++;
                                        totalSum += value;
                                        totalCount++;
                                        monthSum += value;
                                        monthCount++;
                                    }
                                }
                            }
                        }catch (Exception e) {
                        }
                    }

                    if(dayCount > 0) {
                        logger.info("Výkon pro " + getNameOfDay(dayOfWeek) + " " + prevDate + ": " + (daySum / dayCount));
                        chartData.put(prevDate.toString(), daySum / dayCount);
                    }
                    if(monthCount > 0)
                        logger.info("Průměr za " + prevMonth + ". měsíc: " + (monthSum / monthCount));
                    if(totalCount > 0)
                        logger.info("Průměr za celé období (den " + dayOfWeek + "): " + (totalSum / totalCount));

                    DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
                    for(Map.Entry<String, Float> entry : chartData.entrySet()) {
                        dataset.addValue(entry.getValue(), "Průměr výkon", entry.getKey());
                    }
                    JFreeChart chart = ChartFactory.createLineChart(
                            "Průměrné denní výkony pro " + getNameOfDay(dayOfWeek),
                            "Den",
                            "Průměr výkon",
                            dataset,
                            org.jfree.chart.plot.PlotOrientation.VERTICAL,
                            true,true,false);

                    ChartUtilities.saveChartAsPNG(new File("average_per_day_" + dayOfWeek + ".png"), chart, 1920, 1080);

                }catch (Exception e) {
                    System.out.println("Chyba při čtení souboru: " + e.getMessage());
                    System.exit(0);
                }

            }catch (Exception e) {
                System.out.println("Chybný formát argumentu");
                System.exit(0);
            }
        }
    }
}