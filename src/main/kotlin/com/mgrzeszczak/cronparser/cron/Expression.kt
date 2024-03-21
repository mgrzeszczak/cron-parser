package com.mgrzeszczak.cronparser.cron

data class CronExpression(
    val minute: ExpressionWithContext,
    val hour: ExpressionWithContext,
    val dayOfMonth: ExpressionWithContext,
    val month: ExpressionWithContext,
    val dayOfWeek: ExpressionWithContext,
    val command: String
) {

    override fun toString() = listOf(minute.expression, hour.expression, dayOfMonth.expression, month.expression, dayOfWeek.expression, command)
        .joinToString(" ")

    fun prettyString(nameColumnSize: Int): String {
        val expressions = listOf(minute, hour, dayOfMonth, month, dayOfWeek)
            .zip(listOf("minute", "hour", "day of month", "month", "day of week"))
            .map { "${it.second.padEnd(nameColumnSize, ' ')}${it.first.eval().toList().joinToString(" ")}" }
            .joinToString("\n")
        return "$expressions\n${"command".padEnd(nameColumnSize, ' ')}$command"
    }

}

data class ExpressionContext(val range: IntRange)

sealed class Expression {

    abstract fun eval(context: ExpressionContext): Sequence<Int>

}

data class ExpressionWithContext(
    val expression: Expression,
    val context: ExpressionContext
) {
    fun eval(): Sequence<Int> {
        return expression.eval(context)
    }
}

sealed class GroupExpression: Expression()

data class ListExpression(val expressions: List<Expression>) : GroupExpression() {

    override fun eval(context: ExpressionContext): Sequence<Int> {
        return expressions.asSequence()
            .flatMap { it.eval(context) }
            .distinct()
            .sorted()
    }

    override fun toString() = expressions.joinToString(",")

}

sealed class SingleExpression : GroupExpression()

data class RangeExpression(val from: Int, val to: Int) : SingleExpression() {

    override fun eval(context: ExpressionContext): Sequence<Int> {
        return (from..to).asSequence()
    }

    override fun toString() = "$from-$to"

}

data class StepExpression(val start: LiteralExpression, val step: Int) : SingleExpression() {

    override fun eval(context: ExpressionContext): Sequence<Int> {
        return when (start) {
            is NumberValue -> ((start.value)..(context.range.last))
            else -> context.range
        }.step(step).asSequence()
    }

    override fun toString() = "$start/$step"

}

sealed class LiteralExpression : SingleExpression()

data class NumberValue(val value: Int) : LiteralExpression() {

    override fun eval(context: ExpressionContext): Sequence<Int> {
        return sequenceOf(value)
    }

    override fun toString() = value.toString()

}

data object AnyValue : LiteralExpression() {

    override fun eval(context: ExpressionContext): Sequence<Int> {
        return context.range.asSequence()
    }

    override fun toString() = "*"

}
