package attraction.run.token

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import lombok.AccessLevel
import lombok.Getter
import lombok.NoArgsConstructor

@Entity
@Getter
@Table(name = "google_refresh_token")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class GoogleRefreshToken(
        @Id
        @Column(name = "email", length = 100)
        val email: String,

        @Column(name = "refresh_token")
        var token: String,

        @Column(name = "should_reissue_token", nullable = false, columnDefinition = "TINYINT(1) default 0")
        var shouldReissueToken: Boolean
)
