package attraction.run.batch

import attraction.run.gmail.MailNotFoundException
import attraction.run.user.CannotAccessGmailException
import org.springframework.batch.core.step.skip.SkipPolicy

class RefreshTokenSkipPolicy: SkipPolicy {
    override fun shouldSkip(t: Throwable, skipCount: Long): Boolean {
        return (t is MailNotFoundException || t is CannotAccessGmailException)
    }
}