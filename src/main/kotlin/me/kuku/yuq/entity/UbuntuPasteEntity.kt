package me.kuku.yuq.entity

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.kuku.yuq.utils.SpringUtils
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import javax.persistence.*

@Entity
@Table(name = "ubuntu_paste")
class UbuntuPasteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @Column(columnDefinition = "text", unique = true)
    var value: String = ""
    var url: String = ""
}

interface UbuntuPasteRepository: JpaRepository<UbuntuPasteEntity, Int> {
    fun findByValue(value: String): UbuntuPasteEntity?
}


@Service
class UbuntuPasteService(
    private val ubuntuPasteRepository: UbuntuPasteRepository
) {


}

object UbuntuPasteUtils {

    fun url(text: String): String {
        val repository = SpringUtils.getBean<UbuntuPasteRepository>()
        val entity = repository.findByValue(text) ?: kotlin.run {
            runBlocking {
                withContext(Dispatchers.IO) {
                    val url = text.toUrl()
                    val newEntity = UbuntuPasteEntity().also {
                        it.url = url
                        it.value = text
                    }
                    if (url.startsWith("http")) {
                        repository.save(newEntity)
                    }
                    newEntity
                }
            }
        }
        return entity.url
    }

}