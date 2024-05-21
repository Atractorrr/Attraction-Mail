package attraction.run.gmail

import jakarta.persistence.*

@Entity
class Member(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        @Column
        var email: String,
        @Column
        var name: String,
        @Column
        var refreshToken: String
) {

}