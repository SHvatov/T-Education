package ru.tbank.spring.mock;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.spring.mapper.UserMapper;
import ru.tbank.spring.mapper.UserMapperImpl;
import ru.tbank.spring.model.dto.UserDto;
import ru.tbank.spring.model.entity.User;
import ru.tbank.spring.repository.UserRepository;
import ru.tbank.spring.service.UserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    private static final EasyRandom RANDOM = new EasyRandom();
    private static final long USER_ID = 30L;

    @Mock
    private UserRepository userRepository;

    @Spy
    private UserMapper userMapper = new UserMapperImpl();

    @InjectMocks
    private UserService userService;

    @Test
    void save__whenValidUserDto__thenSaveUserAndReturnId() {
        Mockito.when(userRepository.save(Mockito.any()))
                .thenAnswer(invocation -> invocation.getArgument(0, User.class).setSystemId(USER_ID));

        var id = userService.save(RANDOM.nextObject(UserDto.class));
        assertEquals(USER_ID, id);
    }

    @Test
    void get__whenUserNotExists__thenThrowsException() {
        Mockito.when(userRepository.findById(Mockito.eq(USER_ID)))
                .thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> userService.get(USER_ID));
    }

    @Test
    void get__whenUserExists__thenReturnsUserDto() {
        Mockito.when(userRepository.findById(Mockito.eq(USER_ID)))
                .thenReturn(Optional.of(RANDOM.nextObject(User.class)));

        var userDto = userService.get(USER_ID);

        var captor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userMapper, Mockito.times(1)).toDto(captor.capture());

        assertEquals(userDto.getPhone(), captor.getValue().getPhone());
        assertEquals(userDto.getEmail(), captor.getValue().getEmail());
    }

}
