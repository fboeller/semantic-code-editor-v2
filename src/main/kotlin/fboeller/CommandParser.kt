package fboeller

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar

enum class ElementType {
    Class, Field
}

sealed class Command
data class ListCmd(val elementTypes: List<ElementType>) : Command()

object CommandParser : Grammar<Command>() {
    val ws by token("\\s+", ignore = true)
    val LIST by token("list")
    val CLASS by token("class")
    val FIELD by token("field")
    val elementType by (CLASS use { ElementType.Class }) or (FIELD use { ElementType.Field })

    val commandParser by LIST and zeroOrMore(elementType) map { ListCmd(it.t2) }

    override val rootParser by commandParser
}