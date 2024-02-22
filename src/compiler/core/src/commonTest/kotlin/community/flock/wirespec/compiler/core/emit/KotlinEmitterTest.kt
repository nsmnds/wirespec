package community.flock.wirespec.compiler.core.emit

import community.flock.wirespec.compiler.core.fixture.ClassModelFixture
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.test.assertEquals

class KotlinEmitterTest {

    private val emitter = KotlinEmitter()

    @Test
    fun testEmitterType() {
        val expected = """
            |data class Todo(
            |  val name: String,
            |  val description: String? = null,
            |  val notes: List<String>,
            |  val done: Boolean
            |)
        """.trimMargin()

        val res = emitter.emit(ClassModelFixture.type)
        res shouldBe expected
    }

    @Test
    fun testEmitterRefined() {
        val expected = """
            |data class UUID(override val value: String): Wirespec.Refined
            |fun UUID.validate() = Regex(${"\"\""}^[0-9a-fA-F]{8}\b-[0-9a-fA-F]{4}\b-[0-9a-fA-F]{4}\b-[0-9a-fA-F]{4}\b-[0-9a-fA-F]{12}${'$'}${"\"\""}).matches(value)
        """.trimMargin()

        val res = emitter.emit(ClassModelFixture.refined)
        res shouldBe expected
    }

    @Test
    fun testEmitterEnum() {
        val expected = """
            |enum class TodoStatus (val label: String): Wirespec.Enum {
            |  OPEN("OPEN"),
            |  IN_PROGRESS("IN_PROGRESS"),
            |  CLOSE("CLOSE");
            |  override fun toString(): String {
            |    return label
            |  }
            |}
        """.trimMargin()

        val res = emitter.emit(ClassModelFixture.enum)
        res shouldBe expected
    }

    @Test
    fun testEmitterEndpoint() {
        val expected = """
            |interface AddPetEndpoint : Wirespec.Endpoint {
            |  sealed interface Request<T> : Wirespec.Request<T>
            |  data class RequestApplicationXml(
            |    override val path: String,
            |    override val method: Wirespec.Method,
            |    override val query: Map<String, List<Any?>>,
            |    override val headers: Map<String, List<Any?>>,
            |    override val content: Wirespec.Content<Pet>? = null
            |  ) : Request<Pet> {
            |    constructor(body: Pet) : this(
            |      path = "/pet",
            |      method = Wirespec.Method.POST,
            |      query = mapOf<String, List<Any?>>(),
            |      headers = mapOf<String, List<Any?>>(),
            |      content = Wirespec.Content("application/xml", body)
            |    )
            |  }
            |  data class RequestApplicationJson(
            |    override val path: String,
            |    override val method: Wirespec.Method,
            |    override val query: Map<String, List<Any?>>,
            |    override val headers: Map<String, List<Any?>>,
            |    override val content: Wirespec.Content<Pet>? = null
            |  ) : Request<Pet> {
            |    constructor(body: Pet) : this(
            |      path = "/pet",
            |      method = Wirespec.Method.POST,
            |      query = mapOf<String, List<Any?>>(),
            |      headers = mapOf<String, List<Any?>>(),
            |      content = Wirespec.Content("application/json", body)
            |    )
            |  }
            |  data class RequestApplicationXWwwFormUrlencoded(
            |    override val path: String,
            |    override val method: Wirespec.Method,
            |    override val query: Map<String, List<Any?>>,
            |    override val headers: Map<String, List<Any?>>,
            |    override val content: Wirespec.Content<Pet>? = null
            |  ) : Request<Pet> {
            |    constructor(body: Pet) : this(
            |      path = "/pet",
            |      method = Wirespec.Method.POST,
            |      query = mapOf<String, List<Any?>>(),
            |      headers = mapOf<String, List<Any?>>(),
            |      content = Wirespec.Content("application/x-www-form-urlencoded", body)
            |    )
            |  }
            |
            |  sealed interface Response<T> : Wirespec.Response<T>
            |  sealed interface Response2XX<T> : Response<T>
            |  sealed interface Response4XX<T> : Response<T>
            |  sealed interface Response200<T> : Response2XX<T>
            |  sealed interface Response405<T> : Response4XX<T>
            |  data class Response200ApplicationXml(override val status: Int, override val headers: Map<String, List<Any?>>, override val content: Wirespec.Content<Pet>? = null) : Response200<Pet>
            |  data class Response200ApplicationJson(override val status: Int, override val headers: Map<String, List<Any?>>, override val content: Wirespec.Content<Pet>) : Response200<Pet>
            |  data class Response405Unit(override val status: Int, override val headers: Map<String, List<Any?>>, override val content: Wirespec.Content<Unit>? = null) : Response405<Unit>
            |  companion object {
            |    const val PATH = "/pet"
            |    const val METHOD = "POST"
            |    fun <B> REQUEST_MAPPER(contentMapper: Wirespec.ContentMapper<B>) = { request: Wirespec.Request<B> ->
            |      when {
            |        request.content?.type == "application/json" -> contentMapper
            |          .read<Pet>(request.content!!, Wirespec.getType(Pet::class.java, false))
            |          .let { RequestApplicationJson(request.path, request.method, request.query, request.headers, it) }
            |        request.content?.type == "application/xml" -> contentMapper
            |          .read<Pet>(request.content!!, Wirespec.getType(Pet::class.java, false))
            |          .let { RequestApplicationXml(request.path, request.method, request.query, request.headers, it) }
            |        request.content?.type == "application/x-www-form-urlencoded" -> contentMapper
            |          .read<Pet>(request.content!!, Wirespec.getType(Pet::class.java, false))
            |          .let { RequestApplicationXWwwFormUrlencoded(request.path, request.method, request.query, request.headers, it) }
            |        else -> error("Cannot map request")
            |      }
            |    }
            |    fun <B> RESPONSE_MAPPER(contentMapper: Wirespec.ContentMapper<B>) = { response: Wirespec.Response<B> ->
            |      when {
            |        response.status == 200 && response.content?.type == "application/xml" -> contentMapper
            |          .read<Pet>(response.content!!, Wirespec.getType(Pet::class.java, false))
            |          .let { Response200ApplicationXml(response.status, response.headers, it) }
            |        response.status == 200 && response.content?.type == "application/json" -> contentMapper
            |          .read<Pet>(response.content!!, Wirespec.getType(Pet::class.java, false))
            |          .let { Response200ApplicationJson(response.status, response.headers, it) }
            |        response.status == 405 && response.content == null -> Response405Unit(response.status, response.headers, null)
            |        else -> error("Cannot map response with status ${'$'}{response.status}")
            |      }
            |    }
            |  }
            |  suspend fun addPet(request: Request<*>): Response<*>
            |}
        """.trimMargin()

        val res = emitter.emit(ClassModelFixture.endpoint)
        res shouldBe expected
    }
}