package com.carexport.scraping.configdriven;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;

/**
 * Turns every {@code scraping-sources.yml} entry into its own
 * {@link ConfigDrivenConnector} bean, so the {@code List<VehicleSourceConnector>}
 * injected by the orchestrator and the per-source health service pick them up
 * exactly like hand-written connectors.
 *
 * Only invoked when the {@link ConfigDrivenModule} passes its
 * {@code scraping.config-driven.enabled=true} condition.
 */
public class ConfigDrivenConnectorRegistrar implements ImportBeanDefinitionRegistrar {

    private final ScrapingSourcesLoader sourcesLoader = new ScrapingSourcesLoader();

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                        BeanDefinitionRegistry registry,
                                        BeanNameGenerator importBeanNameGenerator) {
        List<SourceConfig> sources = sourcesLoader.load();
        for (SourceConfig source : sources) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder
                    .genericBeanDefinition(ConfigDrivenConnector.class)
                    .addConstructorArgValue(source.name())
                    .addConstructorArgValue(source);
            registry.registerBeanDefinition("configDriven-" + source.name(), builder.getBeanDefinition());
        }
    }
}