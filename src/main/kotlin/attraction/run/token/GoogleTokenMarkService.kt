package attraction.run.token

import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GoogleTokenMarkService(
        private val entityManager: EntityManager
) {
    @Transactional
    fun markTokenForReissue(email: String) {
        entityManager.createQuery("update GoogleRefreshToken g set g.shouldReissueToken = true where g.email = :email")
                .setParameter("email", email)
                .executeUpdate()
        entityManager.flush()
        entityManager.clear()
    }
}