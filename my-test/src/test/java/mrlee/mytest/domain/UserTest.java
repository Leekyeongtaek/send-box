package mrlee.mytest.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    @DisplayName("정상적인 이름과 나이를 입력하면 유저 객체가 생성된다.")
    void createUser_Success() {
        // given
        String name = "이순신";
        int age = 36;

        // when
        User user = new User(name, age);

        // then
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getAge()).isEqualTo(age);
    }

    @Test
    @DisplayName("나이가 0 미만이면 IllegalArgumentException 예외가 발생한다.")
    void createUser_NegativeAge_ThrowsException() {
        // given
        String name = "이순신";
        int age = -1;

        // when & then
        assertThatThrownBy(() -> new User(name, age))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("나이는 0살 이상이어야 합니다.");
    }
}