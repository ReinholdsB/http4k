package org.http4k.testing.lambda

import org.http4k.client.HttpClientContract
import org.http4k.client.JavaHttpClient
import org.http4k.cloudnative.env.Environment
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.then
import org.http4k.filter.AwsAuth
import org.http4k.filter.ClientFilters
import org.http4k.filter.DebuggingFilters
import org.http4k.server.Http4kServer
import org.http4k.server.ServerConfig
import org.http4k.testing.lambda.client.Config
import org.http4k.testing.lambda.client.FunctionName
import org.http4k.testing.lambda.client.LambdaHttpClient
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach


private val lazyClient by lazy {
        val config = Environment.ENV overrides Environment.fromResource("/local.properties")
        val region = Config.region(config)
        val client = DebuggingFilters.PrintRequestAndResponse()
            .then(ClientFilters.AwsAuth(Config.scope(config), Config.credentials(config)))
            .then(JavaHttpClient())

        LambdaHttpClient(FunctionName("test-function"), region).then(client)
}

private val client = { request: Request -> lazyClient(request) }

class LambdaHttpClientTest : HttpClientContract({ NoOpServerConfig }, client, client) {

    @BeforeEach
    fun ensureLocalPropertiesExist(){
        assumeTrue(LambdaHttpClientTest::class.java.getResourceAsStream("/local.properties") != null,
            "local.properties must exist for this test to run")
    }

    override fun `handles response with custom status message`() = assumeTrue(false, "Unsupported client feature")
    override fun `connection refused are converted into 503`() = assumeTrue(false, "Unsupported client feature")
    override fun `unknown host are converted into 503`() = assumeTrue(false, "Unsupported client feature")
    override fun `send binary data`() = assumeTrue(false, "Unsupported client feature")
}

private object NoOpHttp4kServer : Http4kServer {
    override fun start(): Http4kServer = this
    override fun stop(): Http4kServer = this
    override fun port(): Int = 0
}

private object NoOpServerConfig : ServerConfig {
    override fun toServer(httpHandler: HttpHandler): Http4kServer = NoOpHttp4kServer
}