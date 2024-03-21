package com.mgrzeszczak.cronparser

import com.mgrzeszczak.cronparser.cron.Parser

const val nameColumnSize = 14

private val parser = Parser()

fun main(args: Array<String>) {
    val arg = args.firstOrNull() ?: throw IllegalStateException("missing argument")
    println(parser.parse(arg).prettyString(nameColumnSize))
}
