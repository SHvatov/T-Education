package ru.tbank.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.tbank.spring.model.entity.User;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
