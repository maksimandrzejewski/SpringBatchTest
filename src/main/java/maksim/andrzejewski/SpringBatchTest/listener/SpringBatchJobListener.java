package maksim.andrzejewski.SpringBatchTest.listener;

import lombok.RequiredArgsConstructor;
import maksim.andrzejewski.SpringBatchTest.repository.UserRepository;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SpringBatchJobListener implements JobExecutionListener {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            System.out.println("!!! JOB FINISHED! Time to verify the results");

            System.out.println("user count " + userRepository.count());

//			userRepository.findAll().stream()
//					.forEach(user -> System.out.println(user.toString()));
        }
    }
}