package com.carexport.scraping;

import com.fasterxml.jackson.databind.JsonNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Extracts a listing's main image URL from a vehicle detail page.
 *
 * Order of preference:
 * <ol>
 *   <li>JSON-LD schema.org {@code image} — which may be a string, an array
 *       of strings, or an {@code ImageObject} with a {@code contentUrl} /
 *       {@code url} / {@code thumbnailUrl} field,</li>
 *   <li>the Open Graph {@code og:image} (or Twitter {@code twitter:image})
 *       meta tag.</li>
 * </ol>
 *
 * Returns {@code null} when the page exposes no usable image, in which case
 * the frontend simply renders the card without a photo.
 */
final class ListingImageExtractor {

    private ListingImageExtractor() {}

    static String extract(JsonNode vehicleNode, Document doc) {
        if (vehicleNode != null) {
            String fromJsonLd = resolve(vehicleNode.path("image"));
            if (fromJsonLd != null) {
                return fromJsonLd;
            }
        }
        Element og = doc.selectFirst("meta[property=og:image], meta[name=twitter:image]");
        if (og != null) {
            String content = og.attr("content");
            if (!content.isBlank()) {
                return content.trim();
            }
        }
        return null;
    }

    private static String resolve(JsonNode image) {
        if (image.isMissingNode() || image.isNull()) {
            return null;
        }
        if (image.isTextual()) {
            return nonBlank(image.asText());
        }
        if (image.isArray()) {
            for (JsonNode node : image) {
                String resolved = resolve(node);
                if (resolved != null) {
                    return resolved;
                }
            }
            return null;
        }
        if (image.isObject()) {
            return firstText(image, "contentUrl", "url", "thumbnailUrl");
        }
        return null;
    }

    private static String firstText(JsonNode object, String... fields) {
        for (String field : fields) {
            String value = object.path(field).asText("");
            if (!value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private static String nonBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}