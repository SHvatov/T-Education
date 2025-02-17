package ru.tbank.spring.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tbank.spring.mapper.UserMapper;
import ru.tbank.spring.model.dto.UserDto;
import ru.tbank.spring.repository.UserRepository;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    @Transactional
    public Long save(UserDto userDto) {
        return repository.save(mapper.toEntity(userDto))
                .getSystemId();
    }

    public UserDto get(Long id) {
        var user = repository.findById(id);
        return user.map(mapper::toDto)
                .orElseThrow(() -> new RuntimeException("User not found!"));
    }

    public List<UserDto> getAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

}
