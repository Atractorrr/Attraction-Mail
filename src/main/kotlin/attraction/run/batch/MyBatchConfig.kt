package attraction.run.batch

import attraction.run.article.Article
import attraction.run.gmail.GmailReader
import attraction.run.user.User
import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class MyBatchConfig(
        private val entityManagerFactory: EntityManagerFactory,
): DefaultBatchConfiguration() {
    companion object { const val chuckSize = 100 }

    @Bean
    fun myJob(jobRepository: JobRepository, step: Step): Job {
        return JobBuilder("mailJob", jobRepository)
                .start(step)
                .build()
    }

    @Bean
    fun myStep(jobRepository: JobRepository, transactionManager: PlatformTransactionManager, entityManagerFactory: EntityManagerFactory): Step {
        return StepBuilder("mailStep", jobRepository)
                .chunk<User, List<Article>>(chuckSize, transactionManager)
                // chunk 단위만큼 데이터가 쌓이면 writer에 전달하고, writer는 저장
                // 마지막 chunk에서는 사이즈 만큼 안채워져도 실행됨
                .reader(reader(null))
                .processor(mailReadProcessor(null))
                .writer(writer(null))
                .build()
    }

    @Bean
    @StepScope // Bean의 생성 시점이 스프링 애플리케이션이 실행되는 시점이 아닌 @JobScope, @StepScope가 명시된 메서드가 실행될 때까지 지연
    fun reader(@Value("#{jobParameters[requestDate]}") requestDate: String?): JpaPagingItemReader<User> {
        println("==> reader: $requestDate")
        return JpaPagingItemReaderBuilder<User>()
                .name("reader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chuckSize)
                .queryString("SELECT u FROM User u")
                .build()
    }

    @Bean
    @StepScope
    fun mailReadProcessor(@Value("#{jobParameters[requestDate]}") requestDate: String?): ItemProcessor <User, List<Article>> {
        println("==> processor: $requestDate")
        return ItemProcessor<User, List<Article>> (GmailReader::getMemberInboxArticle)
    }

    @Bean
    @StepScope
    fun writer(@Value("#{jobParameters[requestDate]}") requestDate: String?): ItemWriter<List<Article>> {
        println("==> writer: $requestDate")
        return ItemWriter<List<Article>> { items ->
            for (item in items) {
                println("items: $item")
            }
        }
    }

}