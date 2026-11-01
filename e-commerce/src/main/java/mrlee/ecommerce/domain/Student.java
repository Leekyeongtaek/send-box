package mrlee.ecommerce.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Long id;
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    private SchoolClass schoolClass;

    public Student(String name) {
        this.name = name;
    }
}
