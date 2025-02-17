package ru.tbank.spring.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.tbank.spring.model.dto.UserDto;


@Component
public class UserGenderConverter implements Converter<String, UserDto.Gender> {

    @Override
    public UserDto.Gender convert(String source) {
        if (!StringUtils.hasText(source)) {
            return UserDto.Gender.UNDEFINED;
        }

        var loweredSource = source.toLowerCase();
        for (var gender : UserDto.Gender.values()) {
            if (gender.getAliases().contains(loweredSource)) {
                return gender;
            }
        }
        return UserDto.Gender.UNDEFINED;
    }

}
