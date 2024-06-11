package attraction.run.config

import attraction.run.article.Article
import attraction.run.token.GoogleRefreshToken
import com.zaxxer.hikari.HikariDataSource
import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.batch.BatchDataSource
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaDialect
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import javax.sql.DataSource

@EnableTransactionManagement(proxyTargetClass = true)
@EnableJpaRepositories(
        basePackageClasses = [GoogleRefreshToken::class, Article::class],
        entityManagerFactoryRef = "serverEntityManagerFactory",
        transactionManagerRef = "serverTransactionManager"
)
@Configuration
class MultiDataSourceConfig : DefaultBatchConfiguration() {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.default")
    fun defaultDataSource(): DataSource {
        return DataSourceBuilder.create().apply {
            type(HikariDataSource::class.java)
        }.build()
    }

    @Bean
    @BatchDataSource
    @ConfigurationProperties("spring.datasource.server")
    fun serverDataSource(): DataSource {
        return DataSourceBuilder.create().apply {
            type(HikariDataSource::class.java)
        }.build()
    }

    @Bean("serverEntityManagerFactory")
    fun serverEntityManagerFactory(
            @Qualifier("serverDataSource") dataSource: DataSource,
    ): LocalContainerEntityManagerFactoryBean {
        return LocalContainerEntityManagerFactoryBean().apply {
            persistenceUnitName = "serverEntityManager"
            this.dataSource = dataSource
            jpaVendorAdapter = HibernateJpaVendorAdapter()
            setPackagesToScan("attraction.run.token", "attraction.run.article")
            jpaDialect = HibernateJpaDialect()
        }
    }

    @Bean("serverTransactionManager")
    fun jpaTransactionManager(
            @Qualifier("serverEntityManagerFactory") entityManagerFactory: EntityManagerFactory): JpaTransactionManager {
        return JpaTransactionManager().apply {
            this.entityManagerFactory = entityManagerFactory
            setJpaDialect(HibernateJpaDialect())
        }
    }

    override fun getTablePrefix(): String {
        return "ATTRACTION_"
    }

    override fun getCharset(): Charset {
        return StandardCharsets.UTF_8
    }
}