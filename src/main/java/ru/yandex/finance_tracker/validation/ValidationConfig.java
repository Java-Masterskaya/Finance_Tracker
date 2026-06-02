package ru.yandex.finance_tracker.validation;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.springmvc.OpenApiValidationInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class ValidationConfig implements WebMvcConfigurer {

    // Временно отключаем валидатор пока эндпоинты не реализованы полностью
    //Валидация OpenApi через Interceptor
//    @Bean
//    public OpenApiInteractionValidator openApiValidator() {
//        return OpenApiInteractionValidator
//                .createForSpecificationUrl("classpath:finance_openapi.yaml")
//                .build();
//    }
//
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new OpenApiValidationInterceptor(openApiValidator()))
//                .addPathPatterns("/api/v1/**")
//                .order(0);
//    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        //здесь разрешаем запросы только с "http://localhost:1111"
        config.setAllowedOrigins(List.of("http://localhost:1111"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("userId", "Content-Type", "Accept"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);

        return new CorsFilter(source);
    }
}
