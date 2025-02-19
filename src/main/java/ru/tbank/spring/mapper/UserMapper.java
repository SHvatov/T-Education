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
        System.arraycopy(splitFio, 0, fioParts, 0, splitFio.length);

        return UserDto.Fio.builder()
                .firstName(fioParts[0])
                .secondName(fioParts[1])
                .middleName(fioParts[2])
                .build();
    }

    User toEntity(UserDto user);

    default String toFio(UserDto.Fio fio) {
        return String.format("%s %s %s", fio.getFirstName(), fio.getSecondName(), fio.getMiddleName()).trim();
    }

}
