package ru.tbank.spring.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import ru.tbank.spring.validation.RussianPhoneNumber;

import java.util.Set;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Valid
    @NotNull(message = "ФИО пользователя должно быть заполнено!")
    private Fio fio;

    @RussianPhoneNumber
    private String phone;

    @Email(message = "Неверный формат email!")
    private String email;

    @NotNull(message = "Пол пользователя должен быть заполнен!")
    private Gender gender;

    @Getter
    @RequiredArgsConstructor
    public enum Gender {
        MALE(Set.of("м", "мужской", "муж")),
        FEMALE(Set.of("ж", "женский", "жен")),
        UNDEFINED(Set.of("Не определено"));

        private final Set<String> aliases;
    }

    @Data
    public static class Fio {

        @NotBlank(message = "Имя пользователя должно быть указано!")
        private String firstName;

        @NotBlank(message = "Фамилия пользователя должна быть указано!")
        private String secondName;

        // Note: отчества может и не быть :(
        private String middleName;

    }

}
