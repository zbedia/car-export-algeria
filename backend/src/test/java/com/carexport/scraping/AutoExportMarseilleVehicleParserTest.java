package com.carexport.scraping;

import com.carexport.model.FuelType;
import com.carexport.model.VehicleListing;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class AutoExportMarseilleVehicleParserTest {

    private static final char REPLACEMENT = '\uFFFD';

    private final AutoExportMarseilleVehicleParser parser = new AutoExportMarseilleVehicleParser("AutoExportMarseille");

    private VehicleListing parse(String html) {
        Document doc = Jsoup.parse(html);
        return parser.parse(doc, "https://autoexportmarseille.com/annonce/some-car/");
    }

    @Test
    void fullPageIsParsedIntoAListing() {
        VehicleListing v = parse(page(
                "8 990 \u20AC",
                "<h1>2023 PEUGEOT 208 - 25000kms</h1>",
                attribute("Marque", "Peugeot"),
                attribute("Mod\u00E8le", "208"),
                attribute("Ann\u00E9e", "2023"),
                attribute("Carburant", "Essence"),
                attribute("Mise en circulation", "27/12/2023")));

        assertThat(v.getSource()).isEqualTo("AutoExportMarseille");
        assertThat(v.getBrand()).isEqualTo("Peugeot");
        assertThat(v.getModel()).isEqualTo("208");
        assertThat(v.getYear()).isEqualTo(2023);
        assertThat(v.getFuelType()).isEqualTo(FuelType.ESSENCE);
        assertThat(v.getPrice()).isEqualByComparingTo(new BigDecimal("8990"));
        assertThat(v.getCurrency()).isEqualTo("EUR");
        assertThat(v.getFirstRegistrationDate()).isEqualTo(LocalDate.of(2023, 12, 27));
        assertThat(v.getGarageCity()).isEqualTo("Marseille");
    }

    @Test
    void brokenCharsetLabelsAreStillMatched() {
        // The site declares UTF-8 but serves windows-1252 bytes: accented
        // labels arrive as U+FFFD. ASCII-normalized keys must still win.
        VehicleListing v = parse(page(
                "21 500 \u20AC",
                "<h1>NEUF DACIA DUSTER</h1>",
                attribute("Marque", "Dacia"),
                attribute("Mod" + REPLACEMENT + "le", "Duster"),
                attribute("Ann" + REPLACEMENT + "e", "2026"),
                attribute("Carburant", "Essence")));

        assertThat(v.getBrand()).isEqualTo("Dacia");
        assertThat(v.getModel()).isEqualTo("Duster");
        assertThat(v.getYear()).isEqualTo(2026);
    }

    @Test
    void mileageFallsBackToTheHeadline() {
        VehicleListing v = parse(page(
                "12 490 \u20AC",
                "<h1>RENAULT CLIO 90CV - 45000kms</h1>",
                attribute("Marque", "Renault"),
                attribute("Mod" + REPLACEMENT + "le", "Clio"),
                attribute("Ann" + REPLACEMENT + "e", "2025"),
                attribute("Carburant", "Essence")));

        assertThat(v.getMileageKm()).isEqualTo(45_000);
    }

    @Test
    void missingRegistrationDefaultsToFirstOfJanuary() {
        VehicleListing v = parse(page(
                "21 500 \u20AC",
                "<h1>NEUF DACIA DUSTER</h1>",
                attribute("Marque", "Dacia"),
                attribute("Mod" + REPLACEMENT + "le", "Duster"),
                attribute("Ann" + REPLACEMENT + "e", "2026"),
                attribute("Carburant", "Essence")));

        assertThat(v.getFirstRegistrationDate()).isEqualTo(LocalDate.of(2026, 1, 1));
    }

    @Test
    void normallyEncodedElectricValueMapsToElectricFuelType() {
        VehicleListing v = parse(page(
                "25 900 \u20AC",
                "<h1>NEUF GAC GS3 EMZOOM</h1>",
                attribute("Marque", "Gac"),
                attribute("Mod" + REPLACEMENT + "le", "GS3"),
                attribute("Ann" + REPLACEMENT + "e", "2026"),
                attribute("Carburant", "\u00C9lectrique")));

        assertThat(v.getFuelType()).isEqualTo(FuelType.ELECTRIQUE);
    }

    @Test
    void missingDisplacementStaysNull() {
        VehicleListing v = parse(page(
                "8 990 \u20AC",
                "<h1>NEUF PEUGEOT 208</h1>",
                attribute("Marque", "Peugeot"),
                attribute("Mod" + REPLACEMENT + "le", "208"),
                attribute("Ann" + REPLACEMENT + "e", "2026"),
                attribute("Carburant", "Essence")));

        assertThat(v.getEngineDisplacementCm3()).isNull();
    }

    private static String page(String price, String headline, String... attributes) {
        return "<html><body>"
                + "<div class=\"vehica-car-attributes\">" + String.join("", attributes) + "</div>"
                + "<div class=\"vehica-car-price\">" + price + "</div>"
                + headline
                + "</body></html>";
    }

    private static String attribute(String label, String value) {
        return "<div class=\"vehica-car-attributes__name\">" + label + "</div>"
                + "<div class=\"vehica-car-attributes__values\">" + value + "</div>";
    }
}