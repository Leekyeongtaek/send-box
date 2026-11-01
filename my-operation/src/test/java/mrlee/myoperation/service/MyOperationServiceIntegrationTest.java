package mrlee.myoperation.service;

import mrlee.myoperation.domain.MyOperation;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class MyOperationServiceIntegrationTest {

    @Autowired
    private MyOperationService myOperationService;

    @Test
    void myOperationCreate() {
        // given
        MyOperation myOperation = new MyOperation("이경택", 36);

        // when
        MyOperation result = myOperationService.myOperation("이경택", 36);

        // then
        assertThat(result.getName()).isEqualTo("이경택");
        assertThat(result.getPrice()).isEqualTo(36);
    }
}