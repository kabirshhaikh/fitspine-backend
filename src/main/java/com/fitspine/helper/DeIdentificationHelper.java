package com.fitspine.helper;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

@Component
public class DeIdentificationHelper {
    // Patterns to detect PHI-like content
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "\\b\\+?\\d{1,2}?[-.\\s]?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}\\b"
    );

    private static final Pattern NAME_PATTERN = Pattern.compile(
            "\\b(Dr\\.?\\s+[A-Z][a-z]+|[A-Z][a-z]+\\s+[A-Z][a-z]+)\\b"
    );

    private static final Pattern LOCATION_PATTERN = Pattern.compile(
            "\\b(?:Hospital|Clinic|Boston|New\\s?York|MGH|Brigham|Street|Ave|Road|Center)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern DATE_PATTERN = Pattern.compile(
            "\\b(?:\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}|January|February|March|April|May|June|July|August|September|October|November|December)\\b",
            Pattern.CASE_INSENSITIVE
    );

    //Used in FitbitAiDailyAggregationServiceImpl:
    public String sanitizeTheDate(LocalDate logDate) {
        if (logDate == null) {
            return "Unknown Day";
        }
        String dayContext = null;
        LocalDate today = LocalDate.now();
        if (logDate.equals(today)) {
            dayContext = "Today";
        } else if (logDate.equals(today.minusDays(1))) {
            dayContext = "Yesterday";
        } else if (logDate.equals(today.plusDays(1))) {
            dayContext = "Tomorrow";
        } else {
            dayContext = "Undefined Day";
        }

        return dayContext;
    }

    //Used in FitbitAiDailyAggregationServiceImpl:
    public String sanitizeNotes(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        String sanitizedNote = text;

        sanitizedNote = EMAIL_PATTERN.matcher(sanitizedNote).replaceAll("[REPLACED_EMAIL]");
        sanitizedNote = PHONE_PATTERN.matcher(sanitizedNote).replaceAll("[REPLACED_PHONE_NUMBER]");
        sanitizedNote = NAME_PATTERN.matcher(sanitizedNote).replaceAll("[REPLACED_NAME]");
        sanitizedNote = LOCATION_PATTERN.matcher(sanitizedNote).replaceAll("[REPLACED_LOCATION]");
        sanitizedNote = DATE_PATTERN.matcher(sanitizedNote).replaceAll("[REPLACED_DATE]");

        //Replace extra space:
        sanitizedNote = sanitizedNote.replaceAll("\\s{2,}", " ").trim();

        //Remove URL's:
        sanitizedNote = sanitizedNote.replaceAll("https?://\\S+", "[REDACTED_URL]");

        return sanitizedNote;
    }

    //Used in FitbitContextAggregationServiceImpl:
    public String sanitizeTheDateForContextBuilding(LocalDate startDate, String metric) {
        if (startDate == null) {
            return "Undefined start date";
        }

        LocalDate today = LocalDate.now();
        long daysAgo = today.toEpochDay() - startDate.toEpochDay();

        if (daysAgo < 0) {
            return metric.equals("start") ? "Start date is in the future" : "End date is in the future";
        } else if (daysAgo == 0) {
            return metric.equals("start") ? "Start date is today" : "End date is today";
        } else if (daysAgo == 1) {
            return metric.equals("start") ? "Start date was yesterday" : "End date was yesterday";
        } else if (daysAgo <= 10) {
            return metric.equals("start") ? "Start date was " + daysAgo + " days ago" : "End date was " + daysAgo + " days ago";
        } else {
            return metric.equals("start") ? "Start date was more than 10 days ago" : "End date was more than 10 days ago";
        }
    }
}
