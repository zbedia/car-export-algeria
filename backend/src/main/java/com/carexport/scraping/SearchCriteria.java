package com.carexport.scraping;

import java.time.LocalDate;

public class SearchCriteria {
    private String brand;
    private int minYear;

    public static SearchCriteria defaultExportCriteria() {
        SearchCriteria c = new SearchCriteria();
        c.setMinYear(LocalDate.now().getYear() - 3);
        return c;
    }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public int getMinYear() { return minYear; }
    public void setMinYear(int minYear) { this.minYear = minYear; }
}
