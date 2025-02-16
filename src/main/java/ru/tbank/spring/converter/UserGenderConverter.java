package ru.tbank.spring.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.tbank.spring.dto.User;


@Component
public class UserGenderConverter implements Converter<String, User.Gender> {

    @Override
    public User.Gender convert(String source) {
        if (!StringUtils.hasText(source)) {
            return User.Gender.UNDEFINED;
        }

        for (var gender : User.Gender.values()) {
            if (gender.getAliases().contains(source)) {
                return gender;
            }
        }
        return User.Gender.UNDEFINED;
    }

}
