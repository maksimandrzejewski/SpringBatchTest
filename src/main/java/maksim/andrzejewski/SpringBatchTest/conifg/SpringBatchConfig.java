package maksim.andrzejewski.SpringBatchTest.conifg;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import maksim.andrzejewski.SpringBatchTest.listener.SpringBatchJobListener;
import maksim.andrzejewski.SpringBatchTest.mapper.UserPrivilegeRowMapper;
import maksim.andrzejewski.SpringBatchTest.model.Privilege;
import maksim.andrzejewski.SpringBatchTest.model.User;
import maksim.andrzejewski.SpringBatchTest.model.dto.UserPrivilegeDto;
import maksim.andrzejewski.SpringBatchTest.reader.JpaCustomReader;
import maksim.andrzejewski.SpringBatchTest.repository.UserRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.H2PagingQueryProvider;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.RecordFieldExtractor;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Configuration
@RequiredArgsConstructor
//@EnableBatchProcessing
public class SpringBatchConfig {

    private static final Random RANDOM = new Random();
    private static final Integer USERS_NUMBER = 500;
    private static final Integer JDBC_PAGE_SIZE = 1000;
    private static final Integer JDBC_CHUNK_SIZE = 100;
    private static final Integer JPA_CHUNK_SIZE = 100;

    private final UserRepository userRepository;
    private final DataSource dataSource;
    private final UserPrivilegeRowMapper userPrivilegeRowMapper;

    @Bean
    public Job testJob(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager,
                           SpringBatchJobListener listener) {
        return new JobBuilder("test_job", jobRepository)
                .listener(listener)
                .start(insertDatStepOne(jobRepository, transactionManager))
                .next(createCsvStepTwoUsingJdbc(jobRepository, transactionManager))
                .next(createCsvStepThreeUsingJPA(jobRepository, transactionManager))
                .build();
    }

    @Bean
    public Step insertDatStepOne(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("insert_data_step_1", jobRepository)
                .tasklet(dataPrepareTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Step createCsvStepTwoUsingJdbc(JobRepository jobRepository,
                                          PlatformTransactionManager transactionManager) {
        return new StepBuilder("get_data_by_jdbc_db_and_create_csv_step_2", jobRepository)
                .<UserPrivilegeDto, UserPrivilegeDto>chunk(JDBC_CHUNK_SIZE, transactionManager)
                .reader(jdbcReader(null))
                .writer(jdbcWriter())
                .build();
    }

    @Bean
    public Step createCsvStepThreeUsingJPA(JobRepository jobRepository,
                                           PlatformTransactionManager transactionManager) {
        return new StepBuilder("get_data_by_jpa_and_create_csv_step_3", jobRepository)
                .<UserPrivilegeDto, UserPrivilegeDto>chunk(JPA_CHUNK_SIZE, transactionManager)
                .reader(jpaReader())
                .writer(jpaWriter())
                .build();
    }

    @Bean
    public ItemReader<UserPrivilegeDto> jpaReader() {
        return new JpaCustomReader(userRepository);
    }

    @SneakyThrows
    @Bean
    @StepScope
    public JdbcPagingItemReader<UserPrivilegeDto> jdbcReader(@Value("#{jobParameters}") Map<String, Object> jobParameters) {
        final JdbcPagingItemReaderBuilder<UserPrivilegeDto> readerBuilder = new JdbcPagingItemReaderBuilder<>();
        readerBuilder.name("reader_name");
        readerBuilder.dataSource(dataSource);
        readerBuilder.rowMapper(userPrivilegeRowMapper);
        readerBuilder.pageSize(JDBC_PAGE_SIZE);
        final Map<String, Order> orderById = new HashMap<>();
        final String sortColumnName = (String) jobParameters.get("sortColumnName");
        System.out.println(sortColumnName);
        orderById.put(sortColumnName, Order.ASCENDING);
        final H2PagingQueryProvider h2PagingQueryProvider = new H2PagingQueryProvider();
        h2PagingQueryProvider.setSelectClause("SELECT user_id, u.USERNAME, p.PRIVILEGE_NAME");
        h2PagingQueryProvider.setFromClause("from users u left join privileges p on p.user_id_fk = u.user_id");
        h2PagingQueryProvider.setSortKeys(orderById);
        h2PagingQueryProvider.init(dataSource);
        readerBuilder.queryProvider(h2PagingQueryProvider);
        return readerBuilder.build();
    }

    @Bean
    public FlatFileItemWriter<UserPrivilegeDto> jdbcWriter() {
        final FlatFileItemWriter<UserPrivilegeDto> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource("output/test_job_jdbc.csv"));
        writer.setLineAggregator(getDelimitedLineAggregator());
        writer.setName("user_privilege.csv");
        writer.setHeaderCallback(
                writer1 -> writer1.write("userId;userName;privilegeName")
        );
        return writer;
    }
    @Bean
    public FlatFileItemWriter<UserPrivilegeDto> jpaWriter() {
        final FlatFileItemWriter<UserPrivilegeDto> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource("output/test_job_jpa.csv"));
        writer.setLineAggregator(getDelimitedLineAggregator());
        writer.setName("user_privilege.csv");
        writer.setHeaderCallback(
                writer1 -> writer1.write("userId;userName;privilegeName")
        );
        return writer;
    }


    private DelimitedLineAggregator<UserPrivilegeDto> getDelimitedLineAggregator() {
        final RecordFieldExtractor<UserPrivilegeDto> recordFieldExtractor = new RecordFieldExtractor<>(UserPrivilegeDto.class);
        recordFieldExtractor.setNames("userId", "userName", "privilegeName");

        final DelimitedLineAggregator<UserPrivilegeDto> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(";");
        aggregator.setFieldExtractor(recordFieldExtractor);
        return aggregator;

    }

    private Tasklet dataPrepareTasklet() {
        return (contribution, chunkContext) -> {
            for (int i = 0; i < USERS_NUMBER; i++) {
                final User user = User.builder()
                        .username("max_" + i)
                        .privilegeSet(createPrivileges())
                        .build();
                userRepository.save(user);
            }
            return RepeatStatus.FINISHED;
        };
    }

    private static Set<Privilege> createPrivileges() {
        final int randomInt = RANDOM.nextInt(10);
        return IntStream.range(0, randomInt)
                .mapToObj(value ->
                        Privilege.builder()
                                .privilegeName("Privilege_" + value)
                                .build()
                )
                .collect(Collectors.toSet());
    }

}