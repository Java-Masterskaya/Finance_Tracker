package ru.yandex.finance_tracker.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegistrationRequest {

    /**
     * Электронная почта пользователя.
     */
    @Email(message = "The 'email' field must contain a valid email address, such as user@example.com")
    @NotBlank(message = "The user's email must be specified")
    @Size(min = 6, max = 60, message = "User email length must be between {min} and {max} characters")
    String email;

    /**
     * Пароль пользователя.
     */
    @NotNull(message = "User password must be specified")
    @Size(min = 10, max = 60, message = "User password length must be between {min} and {max} characters")
    String password;

    /**
     * Имя пользователя.
     */
    @NotBlank(message = "User firstName must be specified")
    @Size(min = 2, max = 30, message = "User firstName length must be between {min} and {max} characters")
    String firstName;
}
