package attraction.run.gmail

data class Newsletter(private val from: String) {
    private companion object {
        @JvmStatic
        private val NICKNAME_EMAIL_REGEX = "\"([^\"]+)\"\\s*<([^>]+)>".toRegex()
    }

    val nickname: String
    val email: String

    init {
        val match = NICKNAME_EMAIL_REGEX.find(from)

        if (match != null) {
            nickname = match.groupValues[1]
            email = match.groupValues[2]
        } else throw IllegalArgumentException("메일의 보낸사람 정보가 올바르지 않습니다.")
    }
}
