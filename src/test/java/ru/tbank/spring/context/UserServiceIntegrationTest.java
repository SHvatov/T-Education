package ru.tbank.spring.context;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.Advice;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.tbank.spring.mapper.UserMapper;
import ru.tbank.spring.mapper.UserMapperImpl;
import ru.tbank.spring.model.dto.UserDto;
import ru.tbank.spring.model.entity.User;
import ru.tbank.spring.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;


@Testcontainers
@SpringBootTest(classes = UserServiceIntegrationTest.TestApp.class)
@ContextConfiguration(initializers = UserServiceIntegrationTest.DbPropertiesInitializer.class)
public class UserServiceIntegrationTest {

    @Container
    private final static PostgreSQLContainer<?> database =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:15"))
                    .withDatabaseName("users_db")
                    .withUsername("admin")
                    .withPassword("qwerty123");

    @Autowired
    private UserService userService;

    @AfterAll
    public static void shutdown() {
        database.close();
    }

    @Test
    @Rollback
    @Transactional
    @DisplayName("Проверка базовых CRUD-методов")
    void test1() {
        var userDto = UserDto.builder()
                .email("<EMAIL>")
                .phone("<PHONE>")
                .gender(UserDto.Gender.MALE)
                .fio(UserDto.Fio.builder()
                        .firstName("Иван")
                        .secondName("Иванов")
                        .middleName("Иванович")
                        .build())
                .build();

        var id = userService.save(userDto);
        var userEntity = userService.get(id);

        assertEquals(userDto.getEmail(), userEntity.getEmail());
        assertEquals(userDto.getPhone(), userEntity.getPhone());
        assertEquals(1, userService.getAll().size());
    }

    @Slf4j
    @TestConfiguration
    @SpringBootApplication
    @ComponentScan(basePackages = { "ru.tbank.spring" })
    public static class TestApp {

        @Bean
        @Primary
        public UserMapper userMapper() {
            var delegate = new UserMapperImpl();
            return new UserMapper() {
                @Override
                public UserDto toDto(User user) {
                    log.info("Конвертирование сущности в DTO: {}", user);
                    return delegate.toDto(user);
                }

                @Override
                public User toEntity(UserDto user) {
                    log.info("Конвертирование DTO в сущность: {}", user);
                    return delegate.toEntity(user);
                }
            };
        }

        @Slf4j
        private static class LoggingAdvice {

            @Advice.OnMethodEnter
            static void onEnter() {
                System.out.println("Вошли в метод");
            }

            @Advice.OnMethodExit
            static void onExit() {
                System.out.println("Вышли из метода");
            }

        }

    }

    public static class DbPropertiesInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + database.getJdbcUrl(),
                    "spring.datasource.username=" + database.getUsername(),
                    "spring.datasource.password=" + database.getPassword()
            ).applyTo(applicationContext.getEnvironment());
        }

    }

}
