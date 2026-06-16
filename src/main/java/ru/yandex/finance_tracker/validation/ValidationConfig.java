package ru.yandex.finance_tracker.validation;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.springmvc.OpenApiValidationInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ValidationConfig implements WebMvcConfigurer {
    @Bean
    public OpenApiInteractionValidator openApiValidator() {
        return OpenApiInteractionValidator
                .createForSpecificationUrl("/finance_openapi.yaml")
                .build();
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new OpenApiValidationInterceptor(openApiValidator()))
                .addPathPatterns("/api/v1/**")
                .order(0);
    }
}
