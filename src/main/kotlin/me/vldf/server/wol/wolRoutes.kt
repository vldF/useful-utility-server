package me.vldf.server.wol

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File
import java.util.*

fun Route.wolRoutes() {
    var lastStatusCheckTime: Long = 0
    val macsNeedToEnable = mutableListOf<String>()
    val macsHistory = mutableMapOf<String, Long>()
    val config = Properties()
    config.load(File("./config.properties").inputStream())
    val secretKey = config["secretKey"]

    get("/api/wol") {
        lastStatusCheckTime = System.currentTimeMillis()
        val secretKeyParam = this.context.parameters["key"]

        // I don't use JSON here due simplification of the parsing process on the server
        when {
            secretKeyParam != secretKey -> {
                call.respond(HttpStatusCode.Forbidden, "wrong the key param")
            }
            macsNeedToEnable.isNotEmpty() -> {
                val mac = macsNeedToEnable.removeAt(0)
                macsHistory[mac] = lastStatusCheckTime
                call.respondText(mac, ContentType.Text.Plain)
            }
            else -> {
                call.respondText("nothing", ContentType.Text.Plain)
            }
        }
    }

    post("/api/wol") {
        val secretKeyParam = this.context.parameters["key"]
        val mac = this.context.parameters["mac"]

        when {
            secretKeyParam != secretKey -> {
                call.respond(HttpStatusCode.Forbidden, "wrong the key param")
            }
            mac == null -> {
                call.respond(buildJsonObject {
                    put("status", "error")
                    put("description", "MAC address not provided")
                })
            }
            else -> {
                macsNeedToEnable.add(mac)
                call.respond(buildJsonObject {
                    put("status", "ok")
                    put("isServerOnline", isServerOnline(lastStatusCheckTime))
                })
            }
        }
    }

    get("api/wol/checkmac") {
        val secretKeyParam = this.context.parameters["key"]
        val mac = this.context.parameters["mac"]
        when {
            secretKeyParam != secretKey -> {
                call.respond(HttpStatusCode.Forbidden, "wrong the key param")
            }
            mac == null -> {
                call.respond(buildJsonObject {
                    put("status", "error")
                    put("description", "MAC address not provided")
                })
            }
            else -> {
                call.respond(buildJsonObject {
                    put("status", "ok")
                    val response = checkTimeLimit(macsHistory.getOrDefault(mac, 0))
                    put("response", response)
                })
            }
        }
    }
}

private fun isServerOnline(lastTime: Long) = System.currentTimeMillis() - lastTime <= 10 * 1000

private fun checkTimeLimit(time: Long) = System.currentTimeMillis() - time <= 60 * 1000
