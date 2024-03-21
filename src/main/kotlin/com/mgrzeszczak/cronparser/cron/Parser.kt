package com.mgrzeszczak.cronparser.cron

import kotlin.reflect.KClass
import kotlin.reflect.cast

class Parser {

    private val minuteRange = 0..59
    private val hourRange = 0..23
    private val dayOfMonthRange = 1..31
    private val monthRange = 1..12
    private val dayOfWeekRange = 0..6

    fun parse(input: String): CronExpression {
        val tokenizer = Tokenizer(input)
        val minuteExpression =
            ExpressionWithContext(parseGroupExpression(tokenizer, minuteRange), ExpressionContext(minuteRange))
        consume(tokenizer, WhitespaceToken::class)
        val hourExpression =
            ExpressionWithContext(parseGroupExpression(tokenizer, hourRange), ExpressionContext(hourRange))
        consume(tokenizer, WhitespaceToken::class)
        val dayOfMonthExpression =
            ExpressionWithContext(parseGroupExpression(tokenizer, dayOfMonthRange), ExpressionContext(dayOfMonthRange))
        consume(tokenizer, WhitespaceToken::class)
        val monthExpression =
            ExpressionWithContext(parseGroupExpression(tokenizer, monthRange), ExpressionContext(monthRange))
        consume(tokenizer, WhitespaceToken::class)
        val dayOfWeekExpression =
            ExpressionWithContext(parseGroupExpression(tokenizer, dayOfWeekRange), ExpressionContext(dayOfWeekRange))
        consume(tokenizer, WhitespaceToken::class)
        val command = tokenizer.remaining()
        return CronExpression(
            minuteExpression,
            hourExpression,
            dayOfMonthExpression,
            monthExpression,
            dayOfWeekExpression,
            command
        )
    }

    private fun parseGroupExpression(tokenizer: Tokenizer, range: IntRange): GroupExpression {
        val singleExpressions = mutableListOf<SingleExpression>()
        while (true) {
            singleExpressions.add(parseSingleExpression(tokenizer, range))
            when (val next = tokenizer.peek()) {
                is CommaToken -> {
                    tokenizer.next()
                    continue
                }

                is WhitespaceToken -> {
                    break
                }

                else -> {
                    throw ParserException(ParserError.UNEXPECTED_TOKEN, "expected comma or whitespace token, got $next")
                }
            }
        }
        return ListExpression(singleExpressions)
    }

    private fun parseLiteral(token: Token, range: IntRange): LiteralExpression {
        return when (token) {
            is StarToken -> AnyValue
            is NumberToken -> {
                assertValueInRange(token.value, range)
                NumberValue(token.value)
            }

            else -> throw ParserException(ParserError.UNEXPECTED_TOKEN, "expected literal token, got $token")
        }
    }

    private fun parseSingleExpression(tokenizer: Tokenizer, range: IntRange): SingleExpression {
        val token = consumeAnyToken(tokenizer)
        return when (tokenizer.peek()) {
            is StepToken -> parseStepExpression(token, tokenizer, range)
            is RangeToken -> parseRangeExpression(token, tokenizer, range)
            else -> parseLiteral(token, range)
        }
    }

    private fun parseRangeExpression(token: Token, tokenizer: Tokenizer, range: IntRange): RangeExpression {
        return when (token) {
            is NumberToken -> {
                consume(tokenizer, RangeToken::class)
                val numberToken = consume(tokenizer, NumberToken::class)
                assertValueInRange(token.value, range)
                assertValueInRange(numberToken.value, range)
                if (token.value > numberToken.value) {
                    throw ParserException(
                        ParserError.INVALID_RANGE,
                        "invalid range: ${token.value}-${numberToken.value}"
                    )
                }
                RangeExpression(token.value, numberToken.value)
            }

            else -> throw ParserException(ParserError.UNEXPECTED_TOKEN, "expected number token, got $token")
        }
    }

    private fun parseStepExpression(token: Token, tokenizer: Tokenizer, range: IntRange): StepExpression {
        return when (token) {
            is StarToken -> {
                consume(tokenizer, StepToken::class)
                val numberToken = consume(tokenizer, NumberToken::class)
                StepExpression(AnyValue, numberToken.value)
            }

            is NumberToken -> {
                consume(tokenizer, StepToken::class)
                val numberToken = consume(tokenizer, NumberToken::class)
                StepExpression(NumberValue(token.value), numberToken.value)
            }

            else -> throw ParserException(ParserError.UNEXPECTED_TOKEN, "expected number token, got $token")
        }
    }

    private fun consumeAnyToken(tokenizer: Tokenizer): Token {
        return when (val nextToken = tokenizer.peek()) {
            null -> throw ParserException(ParserError.NO_MORE_TOKENS, "no more tokens")
            else -> {
                tokenizer.next()
                nextToken
            }
        }
    }

    private fun <T : Token> consume(tokenizer: Tokenizer, tokenType: KClass<T>): T {
        return when (val nextToken = tokenizer.peek()) {
            null -> throw ParserException(ParserError.NO_MORE_TOKENS, "no more tokens, expected $tokenType")
            else -> {
                if (nextToken::class != tokenType) {
                    throw ParserException(ParserError.UNEXPECTED_TOKEN, "expected $tokenType, got $nextToken")
                }
                tokenizer.next()
                tokenType.cast(nextToken)
            }
        }
    }

    private fun assertValueInRange(value: Int, range: IntRange) {
        if (value !in range) {
            throw ParserException(ParserError.INVALID_VALUE, "expected value in range $range, got $value")
        }
    }

}

enum class ParserError {
    NO_MORE_TOKENS,
    UNEXPECTED_TOKEN,
    INVALID_VALUE,
    INVALID_RANGE
}

class ParserException(val error: ParserError, message: String) : RuntimeException(message)
