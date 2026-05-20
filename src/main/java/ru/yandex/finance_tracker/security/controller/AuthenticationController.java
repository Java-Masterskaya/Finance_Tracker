package ru.yandex.finance_tracker.security.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.finance_tracker.security.dto.JwtResponse;
import ru.yandex.finance_tracker.security.dto.LoginRequest;
import ru.yandex.finance_tracker.security.dto.RegistrationRequest;
import ru.yandex.finance_tracker.security.service.AuthenticationService;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/registration")
    public ResponseEntity<JwtResponse> registration(@Valid @RequestBody RegistrationRequest request) {
        return new ResponseEntity<>(authenticationService.registration(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        return new ResponseEntity<>(authenticationService.login(request), HttpStatus.OK);
    }
}
