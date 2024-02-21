package maksim.andrzejewski.SpringBatchTest;

import maksim.andrzejewski.SpringBatchTest.repository.UserRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.time.Instant;
import java.util.Date;

@SpringBootApplication
public class SpringBatchTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBatchTestApplication.class, args);
	}




	@Bean
	public ApplicationRunner configure(JobRepository jobRepository, Job testJobJdbc, Job testJobJpa) {
		return env ->
		{
			TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
			jobLauncher.setJobRepository(jobRepository);
			jobLauncher.afterPropertiesSet();
			jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
			final JobParameters jobParameters = new JobParametersBuilder()
					.addString("sortColumnName", "user_id")
					.addDate("dateOfExecution", Date.from(Instant.now()))
					.toJobParameters();
			jobLauncher.run(testJobJdbc, jobParameters);
			jobLauncher.run(testJobJpa, jobParameters);
		};
	}
}
