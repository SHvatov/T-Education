package ru.tbank.spring.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Optional;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = User.TABLE, schema = User.SCHEMA)
public class User {

    public static final String TABLE = "users";
    public static final String SCHEMA = "users";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long systemId;
    private String fio;
    private String phone;
    private String email;
    private LocalDateTime rowInsertTime;
    private LocalDateTime rowUpdateTime;

    @PrePersist
    public void audit() {
        rowInsertTime = Optional.ofNullable(rowInsertTime)
                .orElseGet(LocalDateTime::now);
        rowUpdateTime = LocalDateTime.now();
    }

}
