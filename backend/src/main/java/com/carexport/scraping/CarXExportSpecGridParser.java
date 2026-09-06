package com.carexport.scraping;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Extracts the label/value spec grid from a CarXport detail page.
 *
 * The site has shipped several different markups over time, so a cascade of
 * strategies is tried, from classic tables to the current Tailwind
 * paragraphs. The first strategy that yields entries wins; the Tailwind pass
 * is the exception and always supplements the map (putIfAbsent) because the
 * current CarXport grid uses it exclusively.
 */
final class CarXExportSpecGridParser {

    Map<String, String> parse(Document doc) {
        Map<String, String> specs = new LinkedHashMap<>();

        Elements tableRows = doc.select(CarXExportSelectors.SPEC_TABLE_ROWS);
        for (Element row : tableRows) {
            Elements th = row.select(CarXExportSelectors.SPEC_TABLE_LABEL_CELLS);
            Elements td = row.select(CarXExportSelectors.SPEC_TABLE_VALUE_CELLS);
            if (!th.isEmpty() && !td.isEmpty()) {
                specs.put(th.text().trim(), td.text().trim());
            }
        }
        if (!specs.isEmpty()) return specs;

        Elements dts = doc.select(CarXExportSelectors.SPEC_DL_DTS);
        for (Element dt : dts) {
            Element dd = dt.nextElementSibling();
            if (dd != null && "dd".equalsIgnoreCase(dd.tagName())) {
                specs.put(dt.text().trim(), dd.text().trim());
            }
        }
        if (!specs.isEmpty()) return specs;

        Elements items = doc.select(CarXExportSelectors.SPEC_ITEMS);
        for (Element item : items) {
            Element label = item.selectFirst(CarXExportSelectors.SPEC_ITEM_LABEL);
            Element value = item.selectFirst(CarXExportSelectors.SPEC_ITEM_VALUE);
            if (label != null && value != null && label != value) {
                specs.put(label.text().trim(), value.text().trim());
            }
        }

        // Current CarXport markup: <p class="... text-fog">label</p> immediately
        // followed by <p class="... text-snow ...">value</p>. "text-fog" is the
        // stable class; the label font size varies (text-xs / text-[10px]).
        for (Element label : doc.select(CarXExportSelectors.SPEC_TAILWIND_LABEL)) {
            Element value = label.nextElementSibling();
            if (value != null && value.hasText()) {
                specs.putIfAbsent(label.text().trim(), value.text().trim());
            }
        }

        return specs;
    }
}