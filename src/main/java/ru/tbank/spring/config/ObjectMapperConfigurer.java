package ru.tbank.spring.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.tbank.spring.converter.UserGenderConverter;
import ru.tbank.spring.dto.User;


/**
 * Note: работает только для JSON!
 */
@Configuration
@RequiredArgsConstructor
public class ObjectMapperConfigurer {

    private final UserGenderConverter userGenderConverter;

    @Bean
    public SimpleModule userGenderModule() {
        var module = new SimpleModule();
        module.addDeserializer(User.Gender.class, new JsonDeserializer<>() {
            @Override
            @SneakyThrows
            public User.Gender deserialize(JsonParser parser, DeserializationContext context) {
                return userGenderConverter.convert(parser.getText());
            }
        });
        module.addSerializer(User.Gender.class, new JsonSerializer<User.Gender>() {
            @Override
            @SneakyThrows
            public void serialize(User.Gender gender, JsonGenerator generator, SerializerProvider provider) {
                generator.writeObject(
                        gender.getAliases().stream()
                                .findFirst()
                                .orElse(gender.name()));
            }
        });
        return module;
    }

}
