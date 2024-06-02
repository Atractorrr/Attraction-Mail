package attraction.run.batch

import attraction.run.article.Article
import attraction.run.gmail.GmailReader
import attraction.run.html.HTMLHandler
import attraction.run.s3.S3Service
import attraction.run.token.CannotAccessGmailException
import attraction.run.token.GoogleRefreshToken
import jakarta.persistence.EntityManagerFactory
import lombok.RequiredArgsConstructor
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.StepScope
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
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

const val CHUNK_SIZE = 100

@Configuration
@EnableBatchProcessing(dataSourceRef = "defaultDataSource", transactionManagerRef = "serverTransactionManager")
@RequiredArgsConstructor
class BatchConfig(
        @Qualifier("serverEntityManagerFactory")
        private val entityManagerFactory: EntityManagerFactory,
        private val s3Service: S3Service,
        private val gmailReader: GmailReader
) {
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
                .chunk<GoogleRefreshToken, List<Article>>(CHUNK_SIZE, transactionManager)
                .reader(userPagingReader(null))
                .processor(mailCompositeProcessor(null))
                .writer(compositeWriter(null))
                .faultTolerant()
                .skipPolicy(RefreshTokenSkipPolicy())
                .noRollback(CannotAccessGmailException::class.java)
                .build()
    }

    @Bean
    @StepScope // Bean의 생성 시점이 스프링 애플리케이션이 실행되는 시점이 아닌 @JobScope, @StepScope가 명시된 메서드가 실행될 때까지 지연
    fun userPagingReader(@Value("#{jobParameters[requestDate]}") requestDate: String?): JpaPagingItemReader<GoogleRefreshToken> {
        log.info("==> processor: {}", requestDate)
        return JpaPagingItemReaderBuilder<GoogleRefreshToken>()
                .name("reader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString("""
                    SELECT g FROM GoogleRefreshToken g 
                    WHERE g.shouldReissueToken = false
                    ORDER BY g.email
                """.trimIndent())
                .build()
    }

    @Bean
    @StepScope
    fun mailCompositeProcessor(@Value("#{jobParameters[requestDate]}") requestDate: String?): CompositeItemProcessor<GoogleRefreshToken, List<Article>> {
        log.info("==> processor: {}", requestDate)
        val delegates = listOf(mailBoxProcessor(), mailContentProcessor())

        val processor = CompositeItemProcessor<GoogleRefreshToken, List<Article>>()
        processor.setDelegates(delegates)

        return processor
    }

    @Bean
    fun mailBoxProcessor(): ItemProcessor<GoogleRefreshToken, List<Article>> {
        return ItemProcessor<GoogleRefreshToken, List<Article>>(gmailReader::getMemberInboxArticle)
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