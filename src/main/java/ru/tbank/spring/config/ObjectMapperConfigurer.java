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
import ru.tbank.spring.model.dto.UserDto;


@Configuration
@RequiredArgsConstructor
public class ObjectMapperConfigurer {

    private final UserGenderConverter userGenderConverter;

    @Bean
    public SimpleModule userGenderModule() {
        return new SimpleModule()
                .addDeserializer(UserDto.Gender.class, new JsonDeserializer<>() {
                    @Override
                    @SneakyThrows
                    public UserDto.Gender deserialize(JsonParser parser, DeserializationContext context) {
                        return userGenderConverter.convert(parser.getText());
                    }
                })
                .addSerializer(UserDto.Gender.class, new JsonSerializer<>() {
                    @Override
                    @SneakyThrows
                    public void serialize(UserDto.Gender gender, JsonGenerator generator, SerializerProvider provider) {
                        generator.writeObject(
                                gender.getAliases().stream()
                                        .findFirst()
                                        .orElse(gender.name()));
                    }
                });
    }

}
