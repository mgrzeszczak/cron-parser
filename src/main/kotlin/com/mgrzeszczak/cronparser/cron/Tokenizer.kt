package com.mgrzeszczak.cronparser.cron

class Tokenizer(private val input: String) {

    private val tokenRegex =
        Regex("\\d+|\\*|/|-|\\s+|,|MON|TUE|WED|THU|FRI|SAT|SUN|JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC")
    private val numberRegex = Regex("\\d+")
    private val starRegex = Regex("\\*")
    private val stepRegex = Regex("/")
    private val rangeRegex = Regex("-")
    private val commaRegex = Regex(",")
    private val whitespaceRegex = Regex("\\s+")
    private val weekdayRegex = Regex("MON|TUE|WED|THU|FRI|SAT|SUN")
    private val monthRegex = Regex("JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC")

    private val daysOfWeek = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
    private val months = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")

    private var state = input

    fun peek(): Token? {
        val match = tokenRegex.find(state) ?: return null
        val value = match.value
        return when {
            numberRegex.matches(value) -> NumberToken(value.toInt())
            starRegex.matches(value) -> StarToken
            stepRegex.matches(value) -> StepToken
            commaRegex.matches(value) -> CommaToken
            rangeRegex.matches(value) -> RangeToken
            weekdayRegex.matches(value) -> NumberToken(daysOfWeek.indexOf(value))
            monthRegex.matches(value) -> NumberToken(months.indexOf(value) + 1)
            whitespaceRegex.matches(value) -> WhitespaceToken
            else -> UnknownToken(value)
        }
    }

    fun next() {
        val match = tokenRegex.find(state) ?: return
        state = state.substring(match.range.last + 1)
    }

    fun remaining(): String {
        return state
    }

}

sealed class Token
data object StarToken : Token()
data class NumberToken(val value: Int) : Token()
data object RangeToken : Token()
data object StepToken : Token()
data object WhitespaceToken : Token()
data object CommaToken : Token()
data class UnknownToken(val value: String) : Token()
