package fboeller

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar

enum class ElementType {
    Class, Field, Method, Interface, Enum
}

sealed class Command
data class ListCmd(val elementTypes: List<Set<ElementType>>) : Command()
data class FocusCmd(val indexPath: List<Int>) : Command()

object CommandParser : Grammar<Command>() {
    val ws by token("\\s+", ignore = true)

    // List Command
    val LIST by token("list")
    val CLASS by token("class")
    val FIELD by token("field")
    val METHOD by token("method")
    val INTERFACE by token("interface")
    val ENUM by token("enum")
    val ALL by token("\\*")
    val elementType by (CLASS use { setOf(ElementType.Class) }) or
            (FIELD use { setOf(ElementType.Field) }) or
            (METHOD use { setOf(ElementType.Method) }) or
            (INTERFACE use { setOf(ElementType.Interface) }) or
            (ENUM use { setOf(ElementType.Enum) }) or
            (ALL use { ElementType.values().toSet() })

    val listCmd by LIST and zeroOrMore(elementType) map { ListCmd(it.t2.ifEmpty { listOf(ElementType.values().toSet()) }) }

    // Focus Command
    val FOCUS by token("focus")
    val NUMBER by token("\\d+")

    val number by NUMBER use { text.toInt() }

    val focusCmd by FOCUS and oneOrMore(number) map { FocusCmd(it.t2) }

    // All Commands
    override val rootParser by listCmd or focusCmd
}