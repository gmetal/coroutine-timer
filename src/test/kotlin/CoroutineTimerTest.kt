import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.delayController
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.coroutines.CoroutineContext
import kotlin.test.assertEquals

@ExperimentalStdlibApi
@ExperimentalKotest
@ExperimentalCoroutinesApi
class CoroutineTimerTest : BehaviorSpec({
    // Sets per-test case timeout for all test cases in the spec
    timeout = 3500
    // Enable the TestCoroutineDispatcher
    testCoroutineDispatcher = true

    lateinit var mockTimerAction: () -> Unit
    lateinit var testCoroutineTimer: CoroutineTimer

    fun setupTest(repeat: Boolean, coroutineContext: CoroutineContext) {
        mockTimerAction = mockk()
        every { mockTimerAction.invoke() } just Runs

        testCoroutineTimer = CoroutineTimer(
            2000,
            repeat,
            timerAction = mockTimerAction,
            dispatcher = coroutineContext[CoroutineDispatcher.Key]!!
        )
    }

    Given("a CoroutineTimer") {
        And("the timer executes once") {
            And("the timer is not running ") {
                When("the coroutine timer is initialised") {
                    setupTest(repeat = false, coroutineContext)
                    Then("it is not running") {
                        testCoroutineTimer.isRunning() shouldBe false
                    }
                    Then("the timer action has not been executed") {
                        verify { mockTimerAction wasNot called }
                    }
                }
                When("the stop method is called") {
                    setupTest(repeat = false, coroutineContext)
                    testCoroutineTimer.stop()

                    Then("the timer is not running") {
                        testCoroutineTimer.isRunning() shouldBe false
                    }
                    Then("the timer action is not executed") {
                        verify { mockTimerAction wasNot called }
                    }
                }
            }
            And("the timer is running") {
                When("the timer is executed") {
                    setupTest(repeat = false, testContext.coroutineContext)
                    testCoroutineTimer.start()
                    val timerWasRunning = testCoroutineTimer.isRunning()

                    delayController.advanceTimeBy(2100)

                    Then("the timer was running") {
                        timerWasRunning shouldBe true
                    }
                    Then("the timer stops running") {
                        testCoroutineTimer.isRunning() shouldBe false
                    }
                    Then("the timerAction is executed once") {
                        verify(atMost = 1, atLeast = 1) { mockTimerAction.invoke() }
                    }
                }
                When("the timer is stopped") {
                    setupTest(repeat = false, testContext.coroutineContext)
                    testCoroutineTimer.start()

                    delayController.advanceTimeBy(1000)

                    val timerWasRunning = testCoroutineTimer.isRunning()

                    testCoroutineTimer.stop()
                    delayController.advanceTimeBy(1500)

                    Then("verify the time was running") {
                        timerWasRunning shouldBe true
                    }
                    Then("the timer stops running") {
                        assertEquals(false, testCoroutineTimer.isRunning())
                    }
                    Then("the timer actions is not executed") {
                        verify { mockTimerAction wasNot called }
                    }
                }
            }
        }
        And("the timer executes indefinitely") {
            When("a single time period has elapsed") {
                setupTest(repeat = true, testContext.coroutineContext)
                testCoroutineTimer.start()
                delayController.advanceTimeBy(2100)
                val timerWasRunning = testCoroutineTimer.isRunning()
                delayController.advanceTimeBy(2100)

                testCoroutineTimer.stop()

                Then("the timer was running") {
                    timerWasRunning shouldBe true
                }
                Then("the timer action has executed twice") {
                    verify(atLeast = 2, atMost = 2) { mockTimerAction.invoke() }
                }
                Then("the timer is not running") {
                    testCoroutineTimer.isRunning() shouldBe false
                }
            }
        }
    }
})
