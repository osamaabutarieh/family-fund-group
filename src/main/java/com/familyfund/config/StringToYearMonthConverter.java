package com.familyfund.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Component
public class StringToYearMonthConverter implements Converter<String, YearMonth> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Override
    public YearMonth convert(String source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        return YearMonth.parse(source, FORMATTER);
    }
} 