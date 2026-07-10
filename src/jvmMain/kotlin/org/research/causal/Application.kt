package org.research.causal

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.http.HttpMethod
import com.expediagroup.graphql.server.ktor.GraphQL
import com.expediagroup.graphql.server.ktor.graphQLGetRoute
import com.expediagroup.graphql.server.ktor.graphQLPostRoute
import com.expediagroup.graphql.server.ktor.graphQLSDLRoute
import com.expediagroup.graphql.server.ktor.graphiQLRoute

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowHeader("Content-Type")
    }
    
    install(GraphQL) {
        schema {
            packages = listOf("org.research.causal")
            queries = listOf(
                EconometricsQuery()
            )
        }
    }
    
    routing {
        graphQLGetRoute()
        graphQLPostRoute()
        graphiQLRoute()
        graphQLSDLRoute()
    }
}
