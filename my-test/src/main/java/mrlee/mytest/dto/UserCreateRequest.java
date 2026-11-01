package mrlee.mytest.dto;

import lombok.Data;
import lombok.Getter;

@Getter
public class UserCreateRequest {
    private String name;
    private int age;

    public UserCreateRequest(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
