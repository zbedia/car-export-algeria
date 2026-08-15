package com.carexport.scraping;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

/**
 * Owns a single shared Playwright Browser instance, used by connectors
 * that need to scrape JS-rendered pages (Jsoup only sees the initial
 * server-rendered HTML, not content injected by client-side JavaScript).
 *
 * The browser is started lazily, on first use, rather than at application
 * startup: this is a portfolio/demo project, and failing to launch a
 * browser (e.g. Playwright's browser binaries not installed yet via
 * `mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI
 * -D exec.args="install"`) should not prevent the whole application
 * from starting — only the Playwright-based connectors would fail,
 * and that failure is already contained by ScrapingOrchestrator.
 */
@Component
public class PlaywrightBrowserManager {

    private Playwright playwright;
    private Browser browser;

    public synchronized Browser getBrowser() {
        if (browser == null) {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true)
            );
        }
        return browser;
    }

    @PreDestroy
    public void close() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}
