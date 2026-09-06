package com.carexport.scraping;

import com.carexport.model.VehicleListing;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CarXExportVehicleParserTest {

    private final CarXExportVehicleParser parser = new CarXExportVehicleParser("CarXExport");

    private VehicleListing parse(String html) {
        Document doc = Jsoup.parse(html);
        return parser.parse(doc, "https://carxexport.com/fr/offers/anything");
    }

    @Test
    void labelMoteurWithLitreValueIsConvertedToCm3() {
        VehicleListing v = parse(specGrid(
                row("Moteur", "1,2 L"),
                row("Kilométrage", "44 500 km")));

        assertThat(v.getEngineDisplacementCm3()).isEqualTo(1200);
        assertThat(v.getMileageKm()).isEqualTo(44_500);
    }

    @Test
    void litreVariantsAndCcAreAccepted() {
        assertThat(parse(specGrid(row("Moteur", "2.0L"))).getEngineDisplacementCm3()).isEqualTo(2000);
        assertThat(parse(specGrid(row("Moteur", "1,8 l"))).getEngineDisplacementCm3()).isEqualTo(1800);
        assertThat(parse(specGrid(row("Moteur", "1981 cm3"))).getEngineDisplacementCm3()).isEqualTo(1981);
        assertThat(parse(specGrid(row("Moteur", "2000 cc"))).getEngineDisplacementCm3()).isEqualTo(2000);
        assertThat(parse(specGrid(row("Moteur", "2,0"))).getEngineDisplacementCm3()).isEqualTo(2000);
    }

    @Test
    void missingDisplacementYieldsZero() {
        assertThat(parse(specGrid(row("Kilométrage", "44 500 km"))).getEngineDisplacementCm3()).isZero();
        assertThat(parse(specGrid(row("Moteur", "—"))).getEngineDisplacementCm3()).isZero();
    }

    private String specGrid(String... rows) {
        StringBuilder html = new StringBuilder("<html><body>");
        for (String row : rows) {
            String[] parts = row.split("\\|", 2);
            html.append("<p class=\"text-fog\">").append(parts[0]).append("</p>");
            html.append("<p>").append(parts[1]).append("</p>");
        }
        return html.append("</body></html>").toString();
    }

    private static String row(String label, String value) {
        return label + "|" + value;
    }
}