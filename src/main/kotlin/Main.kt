import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    val timer = CoroutineTimer(1000, true) {
        println("executing periodic task")
    }

    runBlocking {
        timer.start()
        timer.start()
        timer.start()
        delay(20000)
        timer.stop()
        delay(1000)
        println("Done run blocking")
    }

    println("done main")
}