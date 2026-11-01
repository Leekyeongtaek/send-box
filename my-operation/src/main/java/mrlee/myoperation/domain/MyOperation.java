package mrlee.myoperation.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Table(name = "my-operation")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MyOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int price;

    public MyOperation(String name, int price) {
        this.name = name;
        this.price = price;
    }
}
