package com.carexport.scraping;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Extracts the label/value spec grid from an Auto Export Marseille detail page.
 *
 * The Vehica theme renders the grid as two always-matching lists in document
 * order: {@code .vehica-car-attributes__name} cells followed by their
 * {@code .vehica-car-attributes__values} siblings, nested in a
 * {@code .vehica-car-attributes} container. The two lists are zipped back
 * into a name-to-value map.
 *
 * Because the site mangles accented characters (declares UTF-8, serves
 * windows-1252), labels are normalized to ASCII before being used as keys:
 * « Année » arrives as « Ann�e » so under what spelling it does — the key is
 * stripped of any non-ASCII byte and lowercased (« annee »).
 */
final class AutoExportMarseilleSpecGridParser {

    Map<String, String> parse(Document doc) {
        Map<String, String> specs = new LinkedHashMap<>();

        Elements names = doc.select(
                AutoExportMarseilleSelectors.SPEC_CONTAINER + " " + AutoExportMarseilleSelectors.SPEC_ATTRIBUTE_NAME);
        Elements values = doc.select(
                AutoExportMarseilleSelectors.SPEC_CONTAINER + " " + AutoExportMarseilleSelectors.SPEC_ATTRIBUTE_VALUE);

        int pairs = Math.min(names.size(), values.size());
        for (int i = 0; i < pairs; i++) {
            String label = normalizeLabel(names.get(i).text());
            String value = values.get(i).text().trim();
            if (!label.isEmpty() && !value.isEmpty()) {
                specs.putIfAbsent(label, value);
            }
        }
        return specs;
    }

    private String normalizeLabel(String rawLabel) {
        if (rawLabel == null) {
            return "";
        }
        // Drop a trailing « : » the theme appends to each label, then remove
        // every non-ASCII byte (accented chars arrive broken) and lowercase.
        String label = rawLabel.replaceAll("[\\s:：]+$", "")
                .replaceAll("[^\\x00-\\x7F]", "")
                .trim()
                .toLowerCase();
        return label.replaceAll("\\s+", " ");
    }
}