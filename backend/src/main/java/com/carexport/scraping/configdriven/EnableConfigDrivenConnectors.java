package com.carexport.scraping.configdriven;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Opts into the config-driven scraping catalog: every source declared in
 * {@code scraping-sources.yml} is registered as a connector bean at startup
 * (when {@code scraping.config-driven.enabled=true}).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(ConfigDrivenConnectorRegistrar.class)
public @interface EnableConfigDrivenConnectors {
}