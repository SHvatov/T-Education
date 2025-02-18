package ru.tbank.spring.repository;

import jakarta.transaction.Transactional;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.tbank.spring.model.entity.User;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;


@DataJpaTest
@Testcontainers
public class UserRepositoryTest {

    private static final EasyRandom RANDOM = new EasyRandom();

    @Container
    private final static PostgreSQLContainer<?> database =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:15"))
                    .withDatabaseName("users_db")
                    .withUsername("admin")
                    .withPassword("admin12345");

    @Autowired
    private UserRepository userRepository;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", database::getJdbcUrl);
        registry.add("spring.datasource.username", database::getUsername);
        registry.add("spring.datasource.password", database::getPassword);
    }

    @AfterAll
    public static void shutdown() {
        database.close();
    }

    @Test
    @Rollback
    @Transactional
    @DisplayName("Проверка базовых CRUD-методов")
    void test1() {
        IntStream.range(0, 10)
                .mapToObj(_ -> RANDOM.nextObject(User.class).setSystemId(null))
                .forEach(userRepository::save);

        var users = userRepository.findAll();
        assertEquals(10, users.size());
    }

}
