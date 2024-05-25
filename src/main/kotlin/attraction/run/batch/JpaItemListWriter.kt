package attraction.run.batch

import org.springframework.batch.item.Chunk
import org.springframework.batch.item.database.JpaItemWriter

class JpaItemListWriter<T>(
        private val jpaItemWriter: JpaItemWriter<T>
) : JpaItemWriter<List<T>>() {

    override fun write(items: Chunk<out List<T>>) {
        val totalEntity = Chunk<T>()

        for (item in items) {
            totalEntity.addAll(item)
        }
        jpaItemWriter.write(totalEntity)
    }
}

