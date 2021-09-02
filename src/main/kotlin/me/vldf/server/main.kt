package me.vldf.server

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.serialization.json.Json
import me.vldf.server.wol.wolRoutes
import java.util.*

fun main() {
    embeddedServer(Netty, 8081) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
            })
        }
        routing {
            wolRoutes()
        }
    }.start(wait = true)
}

