package attraction.run.batch

import org.springframework.batch.item.Chunk
import org.springframework.batch.item.database.JpaItemWriter

class JpaItemListWriter<T>(
        private val jpaItemWriter: JpaItemWriter<T>
) : JpaItemWriter<List<T>>() {

    override fun write(items: Chunk<out List<T>>) {
        val totalEntity = flatChunkItems(items)
        logger.info("write item = $totalEntity")

        runCatching {
            jpaItemWriter.write(totalEntity)
        }.onFailure { e ->
            logger.error("jpa insert error = ${e.localizedMessage}")
        }
    }

    private fun flatChunkItems(items: Chunk<out List<T>>): Chunk<T> {
        val totalEntity = Chunk<T>()
        for (item in items) {
            totalEntity.addAll(item)
        }
        return totalEntity
    }
}

