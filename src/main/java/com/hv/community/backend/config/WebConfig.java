package com.hv.community.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOrigins("http://localhost:5173")
        .allowedMethods("GET", "POST", "PUT", "DELETE")
        .allowedHeaders("*")
        .allowCredentials(true);

    registry.addMapping("/auth/*")
        .allowedOrigins("http://localhost:3080")
        .allowCredentials(false);

    registry.addMapping("/member/*")
        .allowedOrigins("http://localhost:3080")
        .allowCredentials(false);

    registry.addMapping("/community/*")
        .allowedOrigins("http://localhost:3080")
        .allowCredentials(false);
  }
}