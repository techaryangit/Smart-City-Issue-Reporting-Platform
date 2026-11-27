package com.smartcity.issuereporting.config;

import com.smartcity.issuereporting.service.FileStorageService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Serves uploaded complaint photos from the local upload directory at /uploads/**. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final FileStorageService storage;

    public WebConfig(FileStorageService storage) { this.storage = storage; }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(storage.getRoot().toUri().toString());
    }
}
