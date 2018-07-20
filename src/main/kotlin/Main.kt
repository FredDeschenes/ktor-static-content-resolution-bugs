import io.ktor.content.resources
import io.ktor.content.static
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main(args: Array<String>) {
    embeddedServer(Netty, 8080) {
        routing {
            static {
                resources("static")
            }
        }
    }.start(wait = true)
}
