package mrlee.mytest.service;

import mrlee.mytest.domain.User;
import mrlee.mytest.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("중복되지 않은 이름이면 유저 생성 및 저장에 성공한다.")
    void createUser_Success() {
        // given
        String name = "이순신";
        int age = 36;

        given(userRepository.findByName(any(String.class)))
                .willReturn(Optional.empty());

        given(userRepository.save(any(User.class)))
                .willReturn(new User(name, age));

        // when
        User user = userService.createUser(name, age);

        // then
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getAge()).isEqualTo(age);
        then(userRepository).should(times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("이미 존재하는 이름이면 IllegalStateException 예외가 발생한다.")
    void createUser_ExistingUser_ThrowsException() {
        // given
        String name = "이순신";
        int age = 36;
        given(userRepository.findByName(any(String.class)))
                .willReturn(Optional.of(new User(name, age)));

        // when & then
        assertThatThrownBy(() -> userService.createUser(name, age))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 존재하는 이름입니다.");

        then(userRepository).should(never()).save(any(User.class));
    }
}