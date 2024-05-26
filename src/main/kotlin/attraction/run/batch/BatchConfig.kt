package attraction.run.batch

import attraction.run.article.Article
import attraction.run.gmail.GmailReader
import attraction.run.html.HTMLHandler
import attraction.run.s3.S3Service
import attraction.run.user.User
import jakarta.persistence.EntityManagerFactory
import lombok.RequiredArgsConstructor
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.JpaItemWriter
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.batch.item.support.CompositeItemProcessor
import org.springframework.batch.item.support.CompositeItemWriter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

const val CHUNK_SIZE = 100

@Configuration
@RequiredArgsConstructor
class BatchConfig(
        private val entityManagerFactory: EntityManagerFactory,
        private val s3Service: S3Service
) : DefaultBatchConfiguration() {
    private val log = LoggerFactory.getLogger(this.javaClass)!!

    @Bean
    fun job(jobRepository: JobRepository, step: Step): Job {
        return JobBuilder("mailJob", jobRepository)
                .start(step)
                .build()
    }

    @Bean
    fun step(jobRepository: JobRepository, transactionManager: PlatformTransactionManager, entityManagerFactory: EntityManagerFactory): Step {
        return StepBuilder("mailStep", jobRepository)
                .chunk<User, List<Article>>(CHUNK_SIZE, transactionManager)
                // chunk 단위만큼 데이터가 쌓이면 writer에 전달하고, writer는 저장
                // 마지막 chunk에서는 사이즈 만큼 안채워져도 실행됨
                .reader(userPagingReader(null))
                .processor(mailCompositeProcessor(null))
                .writer(compositeWriter(null))
                .build()
    }

    @Bean
    @StepScope // Bean의 생성 시점이 스프링 애플리케이션이 실행되는 시점이 아닌 @JobScope, @StepScope가 명시된 메서드가 실행될 때까지 지연
    fun userPagingReader(@Value("#{jobParameters[requestDate]}") requestDate: String?): JpaPagingItemReader<User> {
        log.info("==> processor: {}", requestDate)
        return JpaPagingItemReaderBuilder<User>()
                .name("reader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString("SELECT u FROM User u")
                .build()
    }

    @Bean
    @StepScope
    fun mailCompositeProcessor(@Value("#{jobParameters[requestDate]}") requestDate: String?): CompositeItemProcessor<User, List<Article>> {
        log.info("==> processor: {}", requestDate)
        val delegates = listOf(mailBoxProcessor(), mailContentProcessor())

        val processor = CompositeItemProcessor<User, List<Article>>()
        processor.setDelegates(delegates)

        return processor
    }

    @Bean
    fun mailBoxProcessor(): ItemProcessor<User, List<Article>> {
        return ItemProcessor<User, List<Article>>(GmailReader::getMemberInboxArticle)
    }

    @Bean
    fun mailContentProcessor(): ItemProcessor<List<Article>, List<Article>> {
        return ItemProcessor<List<Article>, List<Article>>(HTMLHandler::extractArticleFromHtmlContent)
    }

    @Bean
    @StepScope
    fun compositeWriter(@Value("#{jobParameters[requestDate]}") requestDate: String?): CompositeItemWriter<List<Article>> {
        log.info("==> writer: {}", requestDate)
        val delegates = listOf(s3FileWriter(), jpaItemListWriter())

        val writer = CompositeItemWriter<List<Article>>()
        writer.setDelegates(delegates)

        return writer
    }

    @Bean
    fun s3FileWriter(): ItemWriter<List<Article>> {
        return ItemWriter<List<Article>> { items ->
            s3Service.initFilePath()
            items.forEach(s3Service::upload)
        }
    }

    @Bean
    fun jpaItemListWriter(): JpaItemListWriter<Article> {
        val jpaItemWriter = JpaItemWriter<Article>()
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory)

        return JpaItemListWriter(jpaItemWriter).apply {
            setEntityManagerFactory(entityManagerFactory)
        }
    }

}