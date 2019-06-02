package fboeller

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import fboeller.CommandParser.getValue
import fboeller.CommandParser.provideDelegate

enum class ElementType {
    Class, Field, Method, Interface
}

sealed class Command
data class ListCmd(val elementTypes: List<ElementType>) : Command()

object CommandParser : Grammar<Command>() {
    val ws by token("\\s+", ignore = true)
    val LIST by token("list")
    val CLASS by token("class")
    val FIELD by token("field")
    val METHOD by token("method")
    val INTERFACE by token("interface")
    val elementType by (CLASS use { ElementType.Class }) or
            (FIELD use { ElementType.Field }) or
            (METHOD use { ElementType.Method }) or
            (INTERFACE use { ElementType.Interface })

    val commandParser by LIST and zeroOrMore(elementType) map { ListCmd(it.t2) }

    override val rootParser by commandParser
}