package ru.tbank.spring.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.tbank.spring.model.dto.UserDto;
import ru.tbank.spring.model.dto.UsersDto;
import ru.tbank.spring.service.UserService;

import java.util.List;


/**
 * Примеры конвертеров для HTTP запросов:
 *
 * @see org.springframework.http.converter.HttpMessageConverter
 * @see org.springframework.http.converter.json.JsonbHttpMessageConverter
 * @see org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    // POST http://localhost:8080/users
    // Content-Type: application/json
    //
    // {
    //   "fio": {
    //     "firstName": "Иван",
    //     "secondName": "Иванов",
    //     "middleName": "Иванович"
    //   },
    //   "gender": "Мужской",
    //   "phone": "79111111111",
    //   "email": "test@tbank.ru"
    // }
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Long createUserViaJson(@RequestBody @Valid UserDto user) {
        return service.save(user);
    }

    // POST http://localhost:8080/users
    // Content-Type: application/xml
    //
    // <user>
    //     <fio>
    //         <firstName>Sergey</firstName>
    //         <secondName>Sergey</secondName>
    //         <middleName>Sergey</middleName>
    //     </fio>
    //     <gender>Муж</gender>
    //     <phone>79111111111</phone>
    //     <email>test@tbank.ru</email>
    // </user>
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.APPLICATION_XML_VALUE)
    public Long createUserViaXml(@RequestBody @Valid UserDto user) {
        return service.save(user);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserDto getUser(
            @PathVariable
            @NotNull(message = "ИД пользователя не может быть пустым")
            @Positive(message = "ИД пользователя не может быть отрицательным")
            Long id) {

        return service.get(id);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/json", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UserDto> getUsersJson() {
        return service.getAll();
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public UsersDto getUsersXml() {
        return UsersDto.builder()
                .users(service.getAll())
                .build();
    }

}
