package mrlee.ecommerce.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "e_member")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;
    private String name;

    public void changeName(String name) {
        this.name = name;
    }
}
