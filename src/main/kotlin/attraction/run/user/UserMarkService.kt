package attraction.run.user

import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class UserMarkService(
        private val entityManager: EntityManager
) {
    @Transactional
    fun markTokenForReissue(user: User) {
        entityManager.createQuery("update User u set u.shouldReissueToken = true where u.id = :id")
                .setParameter("id", user.id)
                .executeUpdate()
        entityManager.flush()
        entityManager.clear()
    }
}