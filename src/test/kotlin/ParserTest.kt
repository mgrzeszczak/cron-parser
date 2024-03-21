import com.mgrzeszczak.cronparser.cron.Parser
import com.mgrzeszczak.cronparser.cron.ParserError
import com.mgrzeszczak.cronparser.cron.ParserException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class ParserTest {

    private val parser = Parser()

    @ParameterizedTest
    @MethodSource("provideValidData")
    fun `should parse cron expression successfully`(testCase: TestCase) {
        val result = parser.parse(testCase.input)
        assertEquals(testCase.expectedMinuteValues, result.minute.eval().toList())
        assertEquals(testCase.expectedHourValues, result.hour.eval().toList())
        assertEquals(testCase.expectedDayOfMonthValues, result.dayOfMonth.eval().toList())
        assertEquals(testCase.expectedMonthValues, result.month.eval().toList())
        assertEquals(testCase.expectedDayOfWeekValues, result.dayOfWeek.eval().toList())
        assertEquals(testCase.expectedCommand, result.command)
    }

    @ParameterizedTest
    @MethodSource("provideInvalidData")
    fun `should fail to parse cron expression`(testCase: FailingTestCase) {
        val exception = assertThrows<ParserException> {
            parser.parse(testCase.input)
        }
        assertEquals(testCase.expectedError, exception.error)
    }

    companion object {
        @JvmStatic
        fun provideValidData(): List<Arguments> {
            val cases = listOf(
                TestCase(
                    "* * * * * test",
                    (0..59).toList(),
                    (0..23).toList(),
                    (1..31).toList(),
                    (1..12).toList(),
                    (0..6).toList(),
                    "test"
                ),
                TestCase(
                    "*/15 1,2,3 3-5 * * /usr/bin/find",
                    listOf(0, 15, 30, 45),
                    listOf(1, 2, 3),
                    listOf(3, 4, 5),
                    (1..12).toList(),
                    (0..6).toList(),
                    "/usr/bin/find"
                ),
                TestCase(
                    "*/15,*/17 1,2,3 3-5 */2 0-5 ps aux | grep slack",
                    listOf(0, 15, 17, 30, 34, 45, 51),
                    listOf(1, 2, 3),
                    listOf(3, 4, 5),
                    listOf(1, 3, 5, 7, 9, 11),
                    listOf(0, 1, 2, 3, 4, 5),
                    "ps aux | grep slack"
                ),
                TestCase(
                    "*/15,2/17 1,2,3 3-5 */2 0-5 /program arg1 arg2 arg3",
                    listOf(0, 2, 15, 19, 30, 36, 45, 53),
                    listOf(1, 2, 3),
                    listOf(3, 4, 5),
                    listOf(1, 3, 5, 7, 9, 11),
                    listOf(0, 1, 2, 3, 4, 5),
                    "/program arg1 arg2 arg3"
                ),
                TestCase(
                    "0 0 1 SEP-DEC MON-WED /program arg1 arg2 arg3",
                    listOf(0),
                    listOf(0),
                    listOf(1),
                    listOf(9, 10, 11, 12),
                    listOf(1, 2, 3),
                    "/program arg1 arg2 arg3"
                ),
                TestCase(
                    "0 0 1 JAN/3 TUE/2 /program arg1 arg2 arg3",
                    listOf(0),
                    listOf(0),
                    listOf(1),
                    listOf(1, 4, 7, 10),
                    listOf(2, 4, 6),
                    "/program arg1 arg2 arg3"
                )
            )
            return cases.map { Arguments.of(it) }
        }

        @JvmStatic
        fun provideInvalidData(): List<Arguments> {
            val cases = listOf(
                FailingTestCase(
                    "* test",
                    ParserError.NO_MORE_TOKENS
                ),
                FailingTestCase(
                    "1-100 * * * * test",
                    ParserError.INVALID_VALUE
                ),
                FailingTestCase(
                    "10-1 * * * * test",
                    ParserError.INVALID_RANGE
                ),
                FailingTestCase(
                    "1-10, * * * * test",
                    ParserError.UNEXPECTED_TOKEN
                ),
                FailingTestCase(
                    "*/, * * * * test",
                    ParserError.UNEXPECTED_TOKEN
                ),
                FailingTestCase(
                    "2/*, * * * * test",
                    ParserError.UNEXPECTED_TOKEN
                )
            )
            return cases.map { Arguments.of(it) }
        }
    }

    data class TestCase(
        val input: String,
        val expectedMinuteValues: List<Int>,
        val expectedHourValues: List<Int>,
        val expectedDayOfMonthValues: List<Int>,
        val expectedMonthValues: List<Int>,
        val expectedDayOfWeekValues: List<Int>,
        val expectedCommand: String
    )

    data class FailingTestCase(
        val input: String,
        val expectedError: ParserError
    )

}
