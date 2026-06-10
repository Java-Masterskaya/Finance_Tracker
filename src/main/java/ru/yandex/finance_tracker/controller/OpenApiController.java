package ru.yandex.finance_tracker.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpenApiController {
    @GetMapping(value = "/v3/api-docs-static", produces = "application/yaml")
    public ResponseEntity<Resource> getOpenApiYaml() {
        Resource resource = new ClassPathResource("finance_openapi.yaml");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/yaml"))
                .body(resource);
    }
}