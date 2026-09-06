package com.carexport.scraping;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SearchCriteria {
    private String brand;
    private int minYear;

    public static SearchCriteria defaultExportCriteria() {
        SearchCriteria c = new SearchCriteria();
        c.setMinYear(LocalDate.now().getYear() - 3);
        return c;
    }
}