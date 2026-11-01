package mrlee.ecommerce.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "e_delivery")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Long id;
    private String address;
}
