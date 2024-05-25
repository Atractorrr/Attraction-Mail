package attraction.run

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing
@SpringBootApplication
class MailServiceApplication

fun main(args: Array<String>) {
    runApplication<MailServiceApplication>(*args)
}