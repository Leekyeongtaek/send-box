package mrlee.ordersbatch.job.autoconfirm;

import mrlee.ordersbatch.domain.order.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AutoConfirmTaskletUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private AutoConfirmConfiguration autoConfirmConfiguration;

    @Test
    @DisplayName("Tasklet 단위 테스트: 지정된 기간으로 updateAutoConfirm을 호출한다")
    void autoConfirmTaskletUnitTest() throws Exception {
        // given
        String requestDate = "2026-10-20";
        given(orderRepository.updateAutoConfirm(any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(5);

        Tasklet autoConfirmTasklet = autoConfirmConfiguration.autoConfirmTasklet(requestDate);

        // when
        RepeatStatus status = autoConfirmTasklet.execute(null, null);

        // then: Tasklet의 execute 메서드를 직접 실행
        assertThat(status).isEqualTo(RepeatStatus.FINISHED);

        LocalDate localDate = LocalDate.parse(requestDate);
        LocalDateTime startAt = localDate.minusDays(7).atStartOfDay();
        LocalDateTime endAt = localDate.atTime(LocalTime.MAX);

        verify(orderRepository).updateAutoConfirm(startAt, endAt);
    }
}
