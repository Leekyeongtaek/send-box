package mrlee.mytest.service;

import lombok.RequiredArgsConstructor;
import mrlee.mytest.domain.User;
import mrlee.mytest.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class UserService {

    private final UserRepository userRepository;

    public User createUser(String name, int age) {
        // 비즈니스 로직: 중복 이름 검사
        if (userRepository.findByName(name).isPresent()) {
            throw new IllegalStateException("이미 존재하는 이름입니다.");
        }

        User user = new User(name, age);
        return userRepository.save(user);
    }
}
