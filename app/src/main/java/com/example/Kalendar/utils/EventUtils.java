package com.example.Kalendar.utils;

import com.example.Kalendar.models.EventEntity;

import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class EventUtils {

    public static boolean occursOnDate(EventEntity event, LocalDate targetDate, LocalDate startDate) {
        if (event.repeatRule == null || event.repeatRule.isEmpty()) return false;

        // 1. Исключённые даты
        Set<LocalDate> exdates = parseExcludedDates(event.excludedDates);
        if (exdates.contains(targetDate)) return false;

        // 2. Парсим RRULE
        Map<String, String> parts = new HashMap<>();
        for (String part : event.repeatRule.split(";")) {
            String[] kv = part.split("=");
            if (kv.length == 2) parts.put(kv[0], kv[1]);
        }

        String freq = parts.get("FREQ");
        int interval = Integer.parseInt(Objects.requireNonNull(parts.getOrDefault("INTERVAL", "1")));

        Integer count = null;
        if (parts.containsKey("COUNT")) {
            try {
                count = Integer.parseInt(Objects.requireNonNull(parts.get("COUNT")));
            } catch (Exception ignored) {}
        }

        LocalDate until = null;
        if (parts.containsKey("UNTIL") && Objects.requireNonNull(parts.get("UNTIL")).length() == 8) {
            String s = parts.get("UNTIL");
            until = LocalDate.of(
                    Integer.parseInt(Objects.requireNonNull(s).substring(0, 4)),
                    Integer.parseInt(s.substring(4, 6)),
                    Integer.parseInt(s.substring(6, 8))
            );
        }

        // 3. Проверка границ
        if (targetDate.isBefore(startDate)) return false;
        if (until != null && targetDate.isAfter(until)) return false;

        // 4. Расчёт индекса
        long diff;
        switch (Objects.requireNonNull(freq)) {
            case "DAILY":
                diff = ChronoUnit.DAYS.between(startDate, targetDate);
                break;
            case "WEEKLY":
                diff = ChronoUnit.WEEKS.between(startDate, targetDate);
                break;
            case "MONTHLY":
                if (startDate.getDayOfMonth() != targetDate.getDayOfMonth()) return false;
                diff = ChronoUnit.MONTHS.between(startDate, targetDate);
                break;
            case "YEARLY":
                if (!(startDate.getDayOfMonth() == targetDate.getDayOfMonth() &&
                        startDate.getMonth() == targetDate.getMonth())) return false;
                diff = ChronoUnit.YEARS.between(startDate, targetDate);
                break;
            default:
                return false;
        }


        if (diff < 0 || diff % interval != 0) return false;
        return count == null || (diff / interval) < count;
    }





    public static Set<LocalDate> parseExcludedDates(String raw) {
        Set<LocalDate> set = new HashSet<>();
        if (raw == null || raw.trim().isEmpty()) return set;

        for (String s : raw.split(",")) {
            s = s.trim();
            if (s.isEmpty()) continue;
            try {
                set.add(LocalDate.parse(s));
            } catch (Exception ignored) {}
        }
        return set;
    }


    public static String parseDisplayFromRule(String rule) {
        if (rule == null || rule.isEmpty()) return "не повторяется";

        Map<String, String> parts = new HashMap<>();
        for (String part : rule.split(";")) {
            String[] kv = part.split("=");
            if (kv.length == 2) {
                parts.put(kv[0], kv[1]);
            }
        }

        String freq = parts.getOrDefault("FREQ", "DAILY");
        String interval = parts.getOrDefault("INTERVAL", "1");
        String count = parts.get("COUNT");
        String until = parts.get("UNTIL");

        String freqText = switch (Objects.requireNonNull(freq)) {
            case "DAILY" -> "Каждый день";
            case "WEEKLY" -> "Каждую неделю";
            case "MONTHLY" -> "Каждый месяц";
            case "YEARLY" -> "Каждый год";
            default -> "Повтор: не определён";
        };

        int i = Integer.parseInt(Objects.requireNonNull(interval));
        if (i > 1) {
            freqText = switch (freq) {
                case "DAILY" -> "Каждые " + i + " дней";
                case "WEEKLY" -> "Каждую " + i + "-ю неделю";
                case "MONTHLY" -> "Каждый " + i + "-й месяц";
                case "YEARLY" -> "Каждый " + i + "-й год";
                default -> freqText;
            };
        }

        if (count != null) {
            freqText += ", " + count + " раз";
        } else if (until != null && until.length() == 8) {
            String dateStr = until.substring(6, 8) + "." + until.substring(4, 6) + "." + until.substring(0, 4);
            freqText += ", до " + dateStr;
        }

        return freqText;
    }

}

