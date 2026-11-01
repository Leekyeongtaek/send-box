package mrlee.ecommerce.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "school_class")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SchoolClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "class_id")
    private Long id;
    private String name;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private List<Student> students = new ArrayList<>();

    public static SchoolClass create(List<Student> students) {
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.name = "그리핀도르";
        schoolClass.students.addAll(students);

        return schoolClass;
    }
}
