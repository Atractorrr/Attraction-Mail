package attraction.run.gmail

import org.slf4j.LoggerFactory

data class Newsletter(private val from: String) {

    private val log = LoggerFactory.getLogger(this.javaClass)!!

    val nickname: String
    val email: String

    init {
        val split = from.trim().split(" ")

        val fromNickname = split[0]
        if (fromNickname.startsWith("\"") && fromNickname.endsWith("\"")) {
            nickname = fromNickname.substring(1, fromNickname.length - 1)
        } else nickname = fromNickname

        val fromEmail = split[1]
        email = fromEmail.substring(1, fromEmail.length - 1)
    }
}
