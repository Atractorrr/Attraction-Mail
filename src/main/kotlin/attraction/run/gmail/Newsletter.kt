package attraction.run.gmail

import org.slf4j.LoggerFactory

data class Newsletter(private val from: String) {

    private val log = LoggerFactory.getLogger(this.javaClass)!!

    val nickname: String
    val email: String

    init {
        val lastIndexOf = from.lastIndexOf("<")
        val fromNickname = from.substring(0, lastIndexOf - 1).trim()
        val fromEmail = from.substring(lastIndexOf + 1, from.length - 1)

        nickname = if (fromNickname[0] == '"') {
            fromNickname.substring(1, fromNickname.length - 1)
        } else {
            fromNickname
        }
        email = fromEmail.trim()
        log.info("nickname=$nickname email=$email")
    }
}
