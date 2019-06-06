package fboeller

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar

enum class ElementType {
    Class, Field, Method, Interface, Enum, Parameter, Name
}

val scopeTypes: Set<ElementType> = setOf(
        ElementType.Class,
        ElementType.Field,
        ElementType.Method,
        ElementType.Interface,
        ElementType.Enum
)

enum class PathSymbol {
    UP, ROOT
}

sealed class Command
data class ListCmd(val elementTypes: List<Set<ElementType>>) : Command()
data class FocusCmd(val path: Path) : Command()
data class ReadCmd(val indexPath: List<Int>) : Command()

sealed class Path
data class IndexPath(val indexPath: List<Int>) : Path()
data class DirectivePath(val pathSymbol: PathSymbol) : Path()

object CommandParser : Grammar<Command>() {
    val ws by token("\\s+", ignore = true)

    // List Command
    val LIST by token("list")
    val CLASS by token("class")
    val FIELD by token("field")
    val METHOD by token("method")
    val INTERFACE by token("interface")
    val ENUM by token("enum")
    val PARAMETER by token("parameter")
    val NAME by token("name")
    val ALL by token("\\*")
    val elementType by (CLASS use { setOf(ElementType.Class) }) or
            (FIELD use { setOf(ElementType.Field) }) or
            (METHOD use { setOf(ElementType.Method) }) or
            (INTERFACE use { setOf(ElementType.Interface) }) or
            (ENUM use { setOf(ElementType.Enum) }) or
            (PARAMETER use { setOf(ElementType.Parameter) }) or
            (NAME use { setOf(ElementType.Name) }) or
            (ALL use { scopeTypes })

    val listCmd by -LIST and zeroOrMore(elementType) map { ListCmd(it.ifEmpty { listOf(scopeTypes) }) }

    // Focus Command
    val FOCUS by token("focus")
    val NUMBER by token("\\d+")
    val UP by token("\\.\\.")
    val ROOT by token("/")

    val number by NUMBER use { text.toInt() }
    val up by UP use { PathSymbol.UP }
    val root by ROOT use { PathSymbol.ROOT }
    val pathSymbol by up or root map { DirectivePath(it) }
    val indexPath by oneOrMore(number) map { IndexPath(it) }

    val focusCmd by -FOCUS and (indexPath or pathSymbol) map { FocusCmd(it) }

    // Read Command
    val READ by token("read")

    val readCmd by -READ and zeroOrMore(number) map { ReadCmd(it) }

    // All Commands
    override val rootParser by listCmd or focusCmd or readCmd
}