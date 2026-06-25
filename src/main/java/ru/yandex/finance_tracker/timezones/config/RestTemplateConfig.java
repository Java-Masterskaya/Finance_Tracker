package ru.yandex.finance_tracker.timezones.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import ru.yandex.finance_tracker.timezones.mapper.GeoApiProperties;

@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {
    private final GeoApiProperties geoApiProperties;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) geoApiProperties.getConnectTimeout().toMillis());
        factory.setReadTimeout((int) geoApiProperties.getReadTimeout().toMillis());
        return new RestTemplate(factory);
    }
}
