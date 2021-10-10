import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.lang.Thread.sleep
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

class CoroutineTimerTest {

    @MockK
    lateinit var mockTimerAction: () -> Unit

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { mockTimerAction.invoke() } just Runs
    }

    @Test
    fun new_coroutine_timers_are_not_running() {
        val testCoroutineTimer = CoroutineTimer(2000, false, timerAction = mockTimerAction)

        assertEquals(false, testCoroutineTimer.isRunning())
        verify { mockTimerAction wasNot called }
    }

    @Test
    fun on_new_coroutine_timers_the_stop_method_does_nothing() {
        val testCoroutineTimer = CoroutineTimer(2000, false, timerAction = mockTimerAction)

        testCoroutineTimer.stop()

        assertEquals(false, testCoroutineTimer.isRunning())
        verify { mockTimerAction wasNot called }
    }

    @Timeout(3500, unit = TimeUnit.MILLISECONDS)
    @Test
    fun without_repeat_calling_start_will_execute_the_timer_action_once() {
        val testCoroutineTimer = CoroutineTimer(2000, false, timerAction = mockTimerAction)

        testCoroutineTimer.start()

        assertEquals(true, testCoroutineTimer.isRunning())
        while (testCoroutineTimer.isRunning()) {
            sleep(1000)
        }
        assertEquals(false, testCoroutineTimer.isRunning())
        verify { mockTimerAction.invoke() }
    }

    @Timeout(3500, unit = TimeUnit.MILLISECONDS)
    @Test
    fun without_repeat_calling_stop_will_stop_the_timer_without_executing_the_timer_action() {
        val testCoroutineTimer = CoroutineTimer(2000, false, timerAction = mockTimerAction)

        testCoroutineTimer.start()
        sleep(1000)
        assertEquals(true, testCoroutineTimer.isRunning())
        testCoroutineTimer.stop()

        while (testCoroutineTimer.isRunning()) {
            sleep(1000)
        }
        assertEquals(false, testCoroutineTimer.isRunning())
        verify { mockTimerAction wasNot called }
    }

    @Test
    fun with_repeat_wait_to_execute_twice_and_then_stop() {
        val testCoroutineTimer = CoroutineTimer(2000, true, timerAction = mockTimerAction)

        testCoroutineTimer.start()
        sleep(2100)
        assertEquals(true, testCoroutineTimer.isRunning())
        verify(atLeast = 1, atMost = 1) { mockTimerAction.invoke() }
        sleep(2100)
        testCoroutineTimer.stop()
        verify(atLeast = 2, atMost = 2) { mockTimerAction.invoke() }

        assertEquals(false, testCoroutineTimer.isRunning())
    }
}