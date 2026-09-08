package com.carexport.scraping.configdriven;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Enables the config-driven scraping catalog only when explicitly requested
 * ({@code scraping.config-driven.enabled=true}). By default nothing is
 * registered and the bespoke connectors keep running.
 */
@Configuration
@ConditionalOnProperty(name = "scraping.config-driven.enabled", havingValue = "true")
@EnableConfigDrivenConnectors
public class ConfigDrivenModule {
}