package com.example.batchkill;

import com.example.batchkill.config.InFearLearnStudentsBrainWashJobConfig;
import jakarta.annotation.PostConstruct;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.*;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.test.ExecutionContextTestUtils;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;

import static com.example.batchkill.config.InFearLearnStudentsBrainWashJobConfig.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;


@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
public class InFearLearnStudentsBrainWashJobTest {

    // 단위 테스트에서는 ItemReader, ItemProcessor, ItemWriter 등의 개별 컴포넌트를 하나씩 분리해서 정밀 타격한다.
    // @StepScope 빈은 Step 실행 이후에 생성된다. 따라서 단순한 빈 주입만으로는 @StepScope 빈을 테스트할 수 없다.
    // 이 점이 스프링 배치 단위테스트와 일반 스프링 단위 테스트의 주된 차이점이다.
    // 그렇다면 별도의 Job 또는 Step 실행없이 ItemWriter와 같은 개별 컴포넌트를 단위 테스트하려면 어떻게 해야할까?
    // @SpringBatchTest 어노테이션만 추가하면 배치 스코프 빈을 @Autowired로 간편하게 주입받아 테스트할 수 있다.

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Job inFearLearnStudentsBrainWashJob;

    @TempDir
    private Path tempDir;

    @Autowired
    private FlatFileItemWriter<BrainwashedVictim> brainwashedVictimWriter;
    private Path writeTestDir;

    private static final List<InFearLearnStudents> TEST_STUDENTS = List.of(
            new InFearLearnStudents("스프링 핵심 원*", "세계관 최강자", "MURDER_YOUR_IGNORANCE"),
            new InFearLearnStudents("고성* JPA & Hibernate", "자바계의 독재자", "SLAUGHTER_YOUR_LIMITS"),
            new InFearLearnStudents("토*의 스프링 부트", "원조 처형자", "EXECUTE_YOUR_POTENTIAL"),
            new InFearLearnStudents("스프링 시큐리티 완전 정*", "무결점 학살자", "TERMINATE_YOUR_EXCUSES"),
            new InFearLearnStudents("자바 프로그래밍 입* 강좌 (old ver.)", "InFearLearn", "RESIST_BRAINWASH") // 💀 이 놈은 ItemProcessor 필터링 대상
    );

    @PostConstruct
    public void configureJobLauncherTestUtils() throws Exception {
        jobLauncherTestUtils.setJob(inFearLearnStudentsBrainWashJob);
    }

    @AfterEach
    void cleanup() {
        jdbcTemplate.execute("TRUNCATE TABLE infearlearn_students RESTART IDENTITY");
    }

    @Test
    @DisplayName("💀 전체 Job 실행 성공 테스트")
    void shouldLaunchJobSuccessfully() throws Exception {
        // Given - 세뇌 대상자들 투입
        insertTestStudents();
        JobParameters jobParameters = jobLauncherTestUtils.getUniqueJobParametersBuilder()
                .addString("filePath", tempDir.toString())
                .toJobParameters();

        // When - 세뇌 배치 실행
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Then - 배치 실행 결과 검증
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

        Path expectedFile = Paths.get("src/test/resources/expected_brainwashed_victims.jsonl");
        Path actualFile = tempDir.resolve("brainwashed_victims.jsonl");

        List<String> expectedLines = Files.readAllLines(expectedFile);
        List<String> actualLines = Files.readAllLines(actualFile);

        assertLinesMatch(expectedLines, actualLines);
    }

    private void insertTestStudents() {
        TEST_STUDENTS.forEach(student ->
                jdbcTemplate.update("INSERT INTO infearlearn_students (current_lecture, instructor, persuasion_method) VALUES (?, ?, ?)",
                        student.getCurrentLecture(), student.getInstructor(), student.getPersuasionMethod())
        );
    }

    @Test
    @DisplayName("\uD83D\uDC80 세뇌 Step 실행 후 출력 파일 및 컨텍스트 검증")
    void shouldExecuteBrainwashStepAndVerifyOutput() throws IOException {
        // Given
        insertTestStudents();
        JobParameters jobParameters = jobLauncherTestUtils.getUniqueJobParametersBuilder()
                .addString("filePath", tempDir.toString())
                .toJobParameters();

        // When
        // 개별 Step만을 테스트할 때는 launchStep() 메서드를 사용한다.
        JobExecution jobExecution =
                jobLauncherTestUtils.launchStep("inFearLearnStudentsBrainWashStep", jobParameters);

        // Then
        StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();
        verifyStepExecution(stepExecution);
        verifyExecutionContextPromotion(jobExecution);
        verifyFileOutput(tempDir);
    }

    private void verifyStepExecution(StepExecution stepExecution) {
        assertThat(stepExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(stepExecution.getWriteCount()).isEqualTo(TEST_STUDENTS.size() - 1L); // 세뇌 성공자 4명
        assertThat(stepExecution.getFilterCount()).isEqualTo(1L); // 세뇌 저항자 1명
    }

    private void verifyExecutionContextPromotion(JobExecution jobExecution) {
        Long brainwashedVictimCount = ExecutionContextTestUtils.getValueFromJob(jobExecution, "brainwashedVictimCount");
        Long brainwashResistanceCount = ExecutionContextTestUtils.getValueFromJob(jobExecution, "brainwashResistanceCount");

        assertThat(brainwashedVictimCount).isEqualTo(TEST_STUDENTS.size() - 1);
        assertThat(brainwashResistanceCount).isEqualTo(1L);
    }

    private void verifyFileOutput(Path actualPath) throws IOException {
        Path expectedFile = Paths.get("src/test/resources/expected_brainwashed_victims.jsonl");
        Path actualFile = actualPath.resolve("brainwashed_victims.jsonl");

        List<String> expectedLines = Files.readAllLines(expectedFile);
        List<String> actualLines = Files.readAllLines(actualFile);

        assertLinesMatch(expectedLines, actualLines);
    }

    private void verifyFileOutput() throws IOException {
        Path expectedFile = Paths.get("src/test/resources/expected_brainwashed_victims.jsonl");
        Path actualFile = tempDir.resolve("brainwashed_victims.jsonl");

        List<String> expectedLines = Files.readAllLines(expectedFile);
        List<String> actualLines = Files.readAllLines(actualFile);

        assertLinesMatch(expectedLines, actualLines);
    }

    @Test
    @DisplayName("\uD83D\uDC80 통계 Step 실행 후 성공률 계산 확인")
    void shouldExecuteStatisticsStepAndCalculateSuccessRate() throws Exception {
        // Given
        ExecutionContext jobExecutionContext = new ExecutionContext();
        jobExecutionContext.putLong("brainwashedVictimCount", TEST_STUDENTS.size() - 1);
        jobExecutionContext.putLong("brainwashResistanceCount", 1L);

        // When
        JobExecution stepJobExecution =
                jobLauncherTestUtils.launchStep("brainwashStatisticsStep", jobExecutionContext);

        // Then
        Collection<StepExecution> stepExecutions = stepJobExecution.getStepExecutions();
        StepExecution stepExecution = stepExecutions.iterator().next();

        assertThat(stepExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        Double brainwashSuccessRate = ExecutionContextTestUtils.getValueFromStep(stepExecution, "brainwashSuccessRate");
        assertThat(brainwashSuccessRate).isEqualTo(80.0);
    }

    // StepScopeTestExecutionListener가 자동으로 탐지/호출하여 테스트용 StepExecution 생성
//    public StepExecution getStepExecution() throws IOException {
//        writeTestDir = Files.createTempDirectory("write-test");
//
//        JobParameters jobParameters = new JobParametersBuilder()
//                .addString("filePath", writeTestDir.toString())
//                .addLong("random", new SecureRandom().nextLong())
//                .toJobParameters();
//
//        return MetaDataInstanceFactory.createStepExecution(jobParameters);
//    }

//    @Test
//    @DisplayName("💀 ItemWriter 단위 테스트 - 세뇌 대상 파일 출력 검증")
//    void shouldWriteBrainwashedVictimsToFileCorrectly() throws Exception {
//        // Given
//        List<BrainwashedVictim> brainwashedVictims = createBrainwashedVictims();
//
//        // When
//        // 4장의 마지막 작전에서 살펴보았듯이 ItemStream의 open()과 close() 메서드는 Spring Batch Step이 자동으로 호출해준다.
//        // 그러나 여기서는 실제 Step을 실행하는 것이 아니기 때문에 우리가 직접 ItemStream의 메서드들을 호출해주어야 한다.
//        // 테스트 메서드별로 서로 다른 JobParameters가 필요한 경우
//        // 이런 제약들을 해결할 수 있는 더 유연한 방법은 없을까?
//        // 만약 각 테스트 메서드에서 필요할 때마다 커스텀 StepExecution을 생성할 수 있다면 어떨까?
//        // SpringBatchTest는 StepScopeTestUtils 제공
//        brainwashedVictimWriter.open(new ExecutionContext());
//        brainwashedVictimWriter.write(new Chunk<>(brainwashedVictims));
//        brainwashedVictimWriter.close();
//
//        // Then
//        verifyFileOutput(writeTestDir);
//    }

    @Test
    @DisplayName("💀 ItemWriter 단위 테스트 - 세뇌 대상 파일 출력 검증")
    void shouldWriteBrainwashedVictimsToFileCorrectly() throws Exception {
        // Given
        List<BrainwashedVictim> brainwashedVictims = createBrainwashedVictims();

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("filePath", tempDir.toString()) // 이제 @TempDir 사용 가능
                .addLong("random", new SecureRandom().nextLong())
                .toJobParameters();

        StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(jobParameters);

        // When
        // StepScopeTestUtils 활용
        StepScopeTestUtils.doInStepScope(stepExecution, () -> {
            brainwashedVictimWriter.open(new ExecutionContext());
            brainwashedVictimWriter.write(new Chunk<>(brainwashedVictims));
            brainwashedVictimWriter.close();
            return null;
        });

        // Then
        verifyFileOutput();
    }

    private List<BrainwashedVictim> createBrainwashedVictims() {
        return List.of(
                BrainwashedVictim.builder()
                        .victimId(1L)
                        .originalLecture("스프링 핵심 원*")
                        .originalInstructor("세계관 최강자")
                        .brainwashMessage("무지를 살해하라... 배치의 세계가 기다린다 💀")
                        .newMaster("KILL-9")
                        .conversionMethod("MURDER_YOUR_IGNORANCE")
                        .brainwashStatus("MIND_CONTROLLED")
                        .nextAction("ENROLL_KILL9_BATCH_COURSE")
                        .build(),
                BrainwashedVictim.builder()
                        .victimId(2L)
                        .originalLecture("고성* JPA & Hibernate")
                        .originalInstructor("자바계의 독재자")
                        .brainwashMessage("한계를 도살하라... 대용량 데이터를 정복하라 💀")
                        .newMaster("KILL-9")
                        .conversionMethod("SLAUGHTER_YOUR_LIMITS")
                        .brainwashStatus("MIND_CONTROLLED")
                        .nextAction("ENROLL_KILL9_BATCH_COURSE")
                        .build(),
                BrainwashedVictim.builder()
                        .victimId(3L)
                        .originalLecture("토*의 스프링 부트")
                        .originalInstructor("원조 처형자")
                        .brainwashMessage("잠재력을 처형하라... 대용량 처리의 세계로 💀")
                        .newMaster("KILL-9")
                        .conversionMethod("EXECUTE_YOUR_POTENTIAL")
                        .brainwashStatus("MIND_CONTROLLED")
                        .nextAction("ENROLL_KILL9_BATCH_COURSE")
                        .build(),
                BrainwashedVictim.builder()
                        .victimId(4L)
                        .originalLecture("스프링 시큐리티 완전 정*")
                        .originalInstructor("무결점 학살자")
                        .brainwashMessage("변명을 종료하라... 지금 당장 배치를 배워라 💀")
                        .newMaster("KILL-9")
                        .conversionMethod("TERMINATE_YOUR_EXCUSES")
                        .brainwashStatus("MIND_CONTROLLED")
                        .nextAction("ENROLL_KILL9_BATCH_COURSE")
                        .build()
        );
    }
}
