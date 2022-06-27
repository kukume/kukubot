package me.kuku.yuq.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import me.kuku.ktor.service.JacksonConfiguration
import org.springframework.stereotype.Component

@Component
class JacksonConfig: JacksonConfiguration {

    override fun configuration(objectMapper: ObjectMapper) {
        objectMapper
            .enable(SerializationFeature.WRITE_ENUMS_USING_INDEX)
            .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
            .registerModule(JavaTimeModule())
    }
}