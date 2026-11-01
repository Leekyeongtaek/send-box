package mrlee.mytest.controller;

import lombok.RequiredArgsConstructor;
import mrlee.mytest.domain.User;
import mrlee.mytest.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<User> create(@RequestBody User request) {
        User user = userService.createUser(request.getName(), request.getAge());
        return ResponseEntity.ok(user);
    }
}
