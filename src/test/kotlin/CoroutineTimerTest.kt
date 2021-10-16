import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.lang.Thread.sleep
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class CoroutineTimerTest {

    @MockK
    lateinit var mockTimerAction: () -> Unit
    lateinit var testCoroutineDispatcher: TestCoroutineDispatcher

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { mockTimerAction.invoke() } just Runs
        testCoroutineDispatcher = TestCoroutineDispatcher()
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
    fun without_repeat_calling_start_will_execute_the_timer_action_once() = runBlockingTest(testCoroutineDispatcher) {
        val testCoroutineTimer = CoroutineTimer(2000, false, testCoroutineDispatcher, mockTimerAction)

        testCoroutineTimer.start()

        assertEquals(true, testCoroutineTimer.isRunning())
        advanceTimeBy(2100)

        assertEquals(false, testCoroutineTimer.isRunning())
        verify { mockTimerAction.invoke() }
    }

    @Timeout(3500, unit = TimeUnit.MILLISECONDS)
    @Test
    fun without_repeat_calling_stop_will_stop_the_timer_without_executing_the_timer_action() = runBlockingTest(testCoroutineDispatcher) {
        val testCoroutineTimer = CoroutineTimer(2000, false, testCoroutineDispatcher, mockTimerAction)

        testCoroutineTimer.start()
        advanceTimeBy(1000)
        assertEquals(true, testCoroutineTimer.isRunning())
        testCoroutineTimer.stop()
        advanceTimeBy(1500)

        assertEquals(false, testCoroutineTimer.isRunning())
        verify { mockTimerAction wasNot called }
    }

    @Test
    fun with_repeat_wait_to_execute_twice_and_then_stop() = runBlockingTest(testCoroutineDispatcher) {
        val testCoroutineTimer = CoroutineTimer(2000, true, testCoroutineDispatcher, mockTimerAction)

        testCoroutineTimer.start()
        advanceTimeBy(2100)
        assertEquals(true, testCoroutineTimer.isRunning())
        verify(atLeast = 1, atMost = 1) { mockTimerAction.invoke() }
        advanceTimeBy(2100)
        testCoroutineTimer.stop()
        verify(atLeast = 2, atMost = 2) { mockTimerAction.invoke() }

        assertEquals(false, testCoroutineTimer.isRunning())
    }
}