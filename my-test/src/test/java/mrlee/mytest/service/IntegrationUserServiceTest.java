package mrlee.mytest.service;

import mrlee.mytest.domain.User;
import mrlee.mytest.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
public class IntegrationUserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("실제 시스템 환경에서 유저가 정상적으로 DB에 저장된다.")
    void createUser_Success() {
        // given
        String name = "이순신";
        int age = 36;

        // when
        User user = userService.createUser(name, age);

        // then 1
        assertThat(user).isNotNull();

        User findUser = userRepository.findById(user.getId()).orElseThrow(IllegalArgumentException::new);

        // then 2
        assertThat(findUser.getName()).isEqualTo(name);
        assertThat(findUser.getAge()).isEqualTo(age);
    }

    @Test
    @DisplayName("실제 DB에 이미 같은 이름이 있다면 예외가 발생한다.")
    void createUser_Duplicate_ThrowException() {
        // given
        String name = "이순신";
        int age = 36;

        User user = new User(name, age);
        userRepository.saveAndFlush(user);

        // when & then
        assertThatThrownBy(() -> userService.createUser(name, age))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 존재하는 이름입니다.");
    }
}
