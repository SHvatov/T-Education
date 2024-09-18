package ru.tbank.edu.ab.sem3;

import lombok.Builder;
import lombok.Data;
import lombok.With;


@With
@Data
@Builder
public class Person {

    private final String name;
    private final String surname;
    private final int age;

}
