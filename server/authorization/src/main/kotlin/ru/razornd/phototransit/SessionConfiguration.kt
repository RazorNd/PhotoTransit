package ru.razornd.phototransit

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.BeanClassLoaderAware
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.support.GenericConversionService
import org.springframework.core.io.Resource
import org.springframework.core.serializer.Deserializer
import org.springframework.core.serializer.Serializer
import org.springframework.core.serializer.support.DeserializingConverter
import org.springframework.core.serializer.support.SerializingConverter
import org.springframework.security.jackson2.SecurityJackson2Modules
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module
import org.springframework.session.FindByIndexNameSessionRepository
import org.springframework.session.config.SessionRepositoryCustomizer
import org.springframework.session.jdbc.JdbcIndexedSessionRepository
import org.springframework.session.security.SpringSessionBackedSessionRegistry
import java.io.InputStream
import java.io.OutputStream

@Configuration(proxyBeanMethods = false)
class SessionConfiguration : BeanClassLoaderAware {

    private lateinit var classLoader: ClassLoader

    @Bean
    fun httpSessionRegistry(
        repository: FindByIndexNameSessionRepository<*>
    ) = SpringSessionBackedSessionRegistry(repository)


    @Bean
    fun jsonConverterJdbcSessionRepositoryCustomizer(
        @Value("classpath:/session/queries/create-session.sql") createSessionQuery: Resource,
        @Value("classpath:/session/queries/get-session.sql") getSessionQuery: Resource,
        @Value("classpath:/session/queries/update-session.sql") updateSessionQuery: Resource,
        @Value("classpath:/session/queries/delete-session.sql") deleteSessionQuery: Resource,

        @Value("classpath:/session/queries/create-session-attribute.sql") createSessionAttributeQuery: Resource,
        @Value("classpath:/session/queries/update-session-attribute.sql") updateSessionAttributeQuery: Resource,

        @Value("classpath:/session/queries/sessions-by-principal.sql") sessionsByPrincipalQuery: Resource,
        @Value("classpath:/session/queries/delete-sessions-by-expiry-time.sql") deleteSessionsByExpiryTimeQuery: Resource,
    ) = SessionRepositoryCustomizer<JdbcIndexedSessionRepository> { repository ->
        val objectMapper = JacksonSerializer(ObjectMapper().apply {
            registerModules(SecurityJackson2Modules.getModules(classLoader))
            registerModules(OAuth2AuthorizationServerJackson2Module())
        })
        repository.setConversionService(GenericConversionService().apply {
            addConverter(SerializingConverter(objectMapper))
            addConverter(DeserializingConverter(objectMapper))
        })

        repository.setCreateSessionQuery(createSessionQuery.getContentAsString(Charsets.UTF_8))
        repository.setGetSessionQuery(getSessionQuery.getContentAsString(Charsets.UTF_8))
        repository.setUpdateSessionQuery(updateSessionQuery.getContentAsString(Charsets.UTF_8))
        repository.setDeleteSessionQuery(deleteSessionQuery.getContentAsString(Charsets.UTF_8))

        repository.setCreateSessionAttributeQuery(createSessionAttributeQuery.getContentAsString(Charsets.UTF_8))
        repository.setUpdateSessionAttributeQuery(updateSessionAttributeQuery.getContentAsString(Charsets.UTF_8))

        repository.setListSessionsByPrincipalNameQuery(sessionsByPrincipalQuery.getContentAsString(Charsets.UTF_8))

        repository.setDeleteSessionsByExpiryTimeQuery(deleteSessionsByExpiryTimeQuery.getContentAsString(Charsets.UTF_8))
    }

    private class JacksonSerializer(private val objectMapper: ObjectMapper) : Serializer<Any>, Deserializer<Any> {
        override fun serialize(o: Any, outputStream: OutputStream) = objectMapper.writeValue(outputStream, o)

        override fun deserialize(inputStream: InputStream): Any = objectMapper.readValue(inputStream, Any::class.java)
    }

    override fun setBeanClassLoader(classLoader: ClassLoader) {
        this.classLoader = classLoader
    }

}
