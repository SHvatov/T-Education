package ru.tbank.spring.mapper;

import org.mapstruct.Mapper;
import org.springframework.util.StringUtils;
import ru.tbank.spring.model.dto.UserDto;
import ru.tbank.spring.model.entity.User;

import java.util.Arrays;


@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);

    default UserDto.Fio toFio(String fio) {
        if (!StringUtils.hasText(fio)) {
            return null;
        }

        var fioParts = new String[3];
        Arrays.fill(fioParts, "");

        var splitFio = fio.split(" ");
        for (int i = 0; i < splitFio.length; i++) {
            fioParts[i] = splitFio[i];
        }

        return new UserDto.Fio()
                .setFirstName(fioParts[0])
                .setSecondName(fioParts[1])
                .setMiddleName(fioParts[2]);
    }

    User toEntity(UserDto user);

    default String toFio(UserDto.Fio fio) {
        return String.format("%s %s %s", fio.getFirstName(), fio.getSecondName(), fio.getMiddleName()).trim();
    }

}
