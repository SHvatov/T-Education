package ru.tbank.spring.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.tbank.spring.dto.User;
import ru.tbank.spring.dto.Users;

import java.util.ArrayList;
import java.util.List;


/**
 * Примеры конвертеров для HTTP запросов:
 *
 * @see org.springframework.http.converter.HttpMessageConverter
 * @see org.springframework.http.converter.json.JsonbHttpMessageConverter
 * @see org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final List<User> users = new ArrayList<>();

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
    public void createUserViaJson(@RequestBody @Valid User user) {
        users.add(user);
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
    public void createUserViaXml(@RequestBody @Valid User user) {
        users.add(user);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/json", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<User> getUsersJson() {
        return users;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public Users getUsersXml() {
        return Users.builder()
                .users(users)
                .build();
    }

}
