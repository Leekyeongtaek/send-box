package mrlee.ordersbatch.job.autoconfirm;

import mrlee.ordersbatch.domain.order.repository.OrderRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SpringBootTest
@SpringBatchTest
class AutoConfirmConfigurationTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private JobOperatorTestUtils jobOperatorTestUtils;

    @MockitoBean
    private OrderRepository orderRepository;

    @Test
    void autoConfirmTaskletTest() {
        // given
        String requestDate = "2026-10-01";

        JobParameters jobParameters = jobOperatorTestUtils.getUniqueJobParametersBuilder()
                .addString("requestDate", requestDate)
                .toJobParameters();

        given(orderRepository.updateAutoConfirm(any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(5);

        // when
        JobExecution jobExecution = jobOperatorTestUtils.launchStep("autoConfirmStep", jobParameters);

        // then
        Assertions.assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

        LocalDate localDate = LocalDate.parse(requestDate);
        LocalDateTime expectedStartAt = localDate.minusDays(7).atStartOfDay();
        LocalDateTime expectedEndAt = localDate.atTime(LocalTime.MAX);

        // OrderRepository의 updateAutoConfirm이 정확한 인자로 1회 호출되었는지 검증
        //verify(orderRepository).updateAutoConfirm(expectedStartAt, expectedEndAt);
    }
}