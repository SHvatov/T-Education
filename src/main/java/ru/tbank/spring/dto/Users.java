package ru.tbank.spring.dto;

import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@Builder
@XmlRootElement
@NoArgsConstructor
@AllArgsConstructor
public class Users {

    private List<User> users = new ArrayList<>();

}
