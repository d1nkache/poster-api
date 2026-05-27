package com.example

import com.example.application.config.installHttp
import com.example.application.config.installResources
import com.example.application.config.installRouting
import com.example.application.config.installSecurity
import com.example.application.config.installSerialization
import com.example.application.config.installStatusPages
import com.example.application.config.installWebsockets
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.*

class ServerTest {

    @Test
    fun `test root endpoint`() = testApplication {
        application {
            installHttp()
            installSerialization()
            installSecurity()
            installStatusPages()
            installWebsockets()
            installResources()
            installRouting()
        }

        assertEquals(HttpStatusCode.OK, client.get("/").status)
    }

}
