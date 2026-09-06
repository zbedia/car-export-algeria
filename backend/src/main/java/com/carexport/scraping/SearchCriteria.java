package com.carexport.scraping;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SearchCriteria {

    private static final int DEFAULT_EXPORT_AGE_LIMIT_YEARS = 3;

    private String brand;
    private int minYear;

    public static SearchCriteria defaultExportCriteria() {
        SearchCriteria c = new SearchCriteria();
        c.setMinYear(LocalDate.now().getYear() - DEFAULT_EXPORT_AGE_LIMIT_YEARS);
        return c;
    }
}