package mrlee.ecommerce.domain;

import jakarta.persistence.EntityManager;
import mrlee.ecommerce.repository.OrderRepository;
import mrlee.ecommerce.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Transactional
public class RelationshipTest {

    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private EntityManager em;

    // 테스트 조건
    // SchoolClass 엔티티
    // @OneToMany(fetch = FetchType.LAZY)
    // @JoinColumn(name = "class_id")
    // private List<Student> students = new ArrayList<>();
    @Test
    void aa() {
        Student studentA = new Student("학생A");
        Student studentB = new Student("학생B");

        em.persist(studentA);
        em.persist(studentB);

        List<Student> students = new ArrayList<>(List.of(studentA, studentB));

        SchoolClass schoolClass = SchoolClass.create(students);
        em.persist(schoolClass);

        em.flush();
        em.clear();

        SchoolClass findSchoolClass = em.find(SchoolClass.class, schoolClass.getId());
        List<Student> studentsList = findSchoolClass.getStudents();

        for (Student student : studentsList) {
            System.out.println(student.getName());
        }
    }
}
