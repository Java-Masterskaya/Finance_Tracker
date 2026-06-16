package ru.yandex.finance_tracker.validation;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.springmvc.OpenApiValidationInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class ValidationConfig implements WebMvcConfigurer {
    @Bean
    public OpenApiInteractionValidator openApiValidator() throws IOException {
        Resource resource = new ClassPathResource("finance_openapi.yaml");

        String spec = new String(
                resource.getInputStream().readAllBytes()
        );

        return OpenApiInteractionValidator
                .createForInlineApiSpecification(spec)
                .build();
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        try {
            registry.addInterceptor(new OpenApiValidationInterceptor(openApiValidator()))
                    .addPathPatterns("/api/v1/**")
                    .order(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
