package attraction.run.user

import jakarta.persistence.*

@Entity
class User(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        @Column
        var email: String,
        @Column
        var name: String,
        @Column
        var refreshToken: String
)