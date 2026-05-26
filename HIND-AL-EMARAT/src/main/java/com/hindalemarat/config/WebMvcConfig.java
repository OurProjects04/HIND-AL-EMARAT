package com.hindalemarat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.dir:uploads/products}")
    private String uploadDirProp;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded images from /uploads/products/ directory
        String uploadDir = Paths.get(uploadDirProp).toAbsolutePath().toString();
        registry
            .addResourceHandler("/uploads/products/**")
            .addResourceLocations("file:" + uploadDir + "/");
    }
}
