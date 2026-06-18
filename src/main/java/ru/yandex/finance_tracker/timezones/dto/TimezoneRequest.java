package ru.yandex.finance_tracker.timezones.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TimezoneRequest {

    @NotNull(message = "Timezone must be specified")
    @Pattern(regexp = "^[a-zA-Z0-9_/+-]+$", message = "Invalid timezone format")
    String timezone;
}
