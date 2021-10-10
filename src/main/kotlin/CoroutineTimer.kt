import kotlinx.coroutines.*

class CoroutineTimer(
    private val period: Long = 1000L,
    private val repeat: Boolean = false,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val timerAction: () -> Unit
) {
    private var job: Job? = null

    /**
     * Starts the execution of the timer
     */
    @Synchronized
    fun start() {
        if (isRunning()) {
            return
        }
        job = CoroutineScope(dispatcher).launch {
            withTimeout(period) {
                delay(period + 1)
            }
        }.also {
            it.invokeOnCompletion { exc ->
                when (exc) {
                    is TimeoutCancellationException -> {
                        println("timeout reached")
                        timerAction()
                        if (repeat) {
                            start()
                        }
                    }
                    is CancellationException -> println("cancelled")
                    else -> println("Other")
                }
            }
        }
    }

    /**
     * Stops the execution of the timer, if it is running
     */
    fun stop() {
        job?.cancel(CancellationException())
    }

    /**
     * Returns whether the timer is currently running or not
     */
    fun isRunning(): Boolean = job?.isActive == true
}