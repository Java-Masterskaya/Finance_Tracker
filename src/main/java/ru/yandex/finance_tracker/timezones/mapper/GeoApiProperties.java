package ru.yandex.finance_tracker.timezones.mapper;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "geo.api")
public class GeoApiProperties {
    private String url;
    private Duration connectTimeout;
    private Duration readTimeout;
}
