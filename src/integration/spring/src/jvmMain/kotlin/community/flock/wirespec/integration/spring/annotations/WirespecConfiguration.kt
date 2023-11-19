package community.flock.wirespec.integration.spring.annotations

import com.fasterxml.jackson.databind.ObjectMapper
import community.flock.wirespec.Wirespec
import community.flock.wirespec.integration.spring.annotations.Util.getStaticField
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.util.pattern.PathPatternParser
import java.io.BufferedReader
import java.lang.reflect.Type

class JacksonContentMapper(val objectMapper: ObjectMapper) : Wirespec.ContentMapper<BufferedReader> {
    override fun <T> read(
        content: Wirespec.Content<BufferedReader>,
        valueType: Type,
    ) = content.let {
        val type = objectMapper.constructType(valueType)
        val obj: T = objectMapper.readValue(it.body, type)
        Wirespec.Content(it.type, obj)
    }

    override fun <T> write(
        content: Wirespec.Content<T>,
    ) = content.let {
        val bytes: ByteArray = objectMapper.writeValueAsBytes(content.body)
        Wirespec.Content(it.type, bytes.inputStream().bufferedReader())
    }
}


@Configuration
@Import(WirespecResponseBodyAdvice::class, WirespecWebMvcConfigurer::class)
open class WirespecConfiguration {


    @Bean
    open fun contentMapper(objectMapper: ObjectMapper) = JacksonContentMapper(objectMapper)

    @Bean
    open fun registerWirespecController(
        contentMapper: JacksonContentMapper,
        applicationContext: ApplicationContext,
        requestMappingHandlerMapping: RequestMappingHandlerMapping
    ): String {
        val options = RequestMappingInfo.BuilderConfiguration()
            .apply {
                patternParser = PathPatternParser()
            }

        applicationContext.getBeansWithAnnotation(WirespecController::class.java)
            .forEach { controller ->
                controller.value.javaClass.interfaces.toList().forEach { endpoint ->
                    val path = endpoint.getStaticField("PATH")?.get(endpoint) as String
                    val method = endpoint.getStaticField("METHOD")?.get(endpoint) as String
                    val requestMappingWirespec = RequestMappingInfo
                        .paths(path)
                        .methods(RequestMethod.valueOf(method))
                        .options(options)
                        .build();
                    val ignoredMethods = listOf("REQUEST_MAPPER", "RESPONSE_MAPPER")
                    val func = endpoint.methods.first { !ignoredMethods.contains(it.name) }
                    requestMappingHandlerMapping.registerMapping(
                        requestMappingWirespec,
                        controller.value,
                        func
                    )
                }
            }

        return "Hello"
    }


}
