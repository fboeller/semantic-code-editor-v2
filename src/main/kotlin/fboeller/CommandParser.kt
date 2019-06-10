package fboeller

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.javaparser.ast.Node

enum class ElementType {
    Class, Field, Method, Interface, Enum, Parameter, Name, Type
}

val scopeTypes: Set<ElementType> = setOf(
        ElementType.Class,
        ElementType.Field,
        ElementType.Method,
        ElementType.Interface,
        ElementType.Enum
)

val allSubNodes = subNodesOfTypes(scopeTypes)

enum class PathSymbol {
    UP, ROOT
}

sealed class Command
data class ListCmd(val levelFilters: List<LevelFilter>) : Command()
data class FocusCmd(val path: Path) : Command()
data class ReadCmd(val indexPath: List<Int>) : Command()

sealed class Path
data class IndexPath(val indexPath: List<Int>) : Path()
data class DirectivePath(val pathSymbol: PathSymbol) : Path()

data class LevelFilter(val producer: (Node) -> List<Node>,
                       val filter: (Node) -> Boolean)

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
    val TYPE by token("type")
    val ALL by token("\\*")
    val elementType by (CLASS use { setOf(ElementType.Class) }) or
            (FIELD use { setOf(ElementType.Field) }) or
            (METHOD use { setOf(ElementType.Method) }) or
            (INTERFACE use { setOf(ElementType.Interface) }) or
            (ENUM use { setOf(ElementType.Enum) }) or
            (PARAMETER use { setOf(ElementType.Parameter) }) or
            (NAME use { setOf(ElementType.Name) }) or
            (TYPE use { setOf(ElementType.Type) }) or
            (ALL use { scopeTypes })

    val AND by token("&&")
    val LPAR by token("\\(")
    val RPAR by token("\\)")
    val WITH by token("with")

    val STRING_LITERAL by token("\"[^\\\\\"]*(\\\\[\"nrtbf\\\\][^\\\\\"]*)*\"")
    val stringLiteral by STRING_LITERAL use { text.substring(1, text.length - 1) }

    val nodeProducer by elementType map { subNodesOfTypes(it) }

    // TODO: Disallow name filter for type and name element types
    val singleFilter by (stringLiteral or (-NAME and stringLiteral) map { hasName(it) })

    val multipleFilters by leftAssociative(singleFilter, AND) { l, _, r -> { node -> l(node) && r(node) } }

    val levelFilter: Parser<LevelFilter> =
            (nodeProducer and -WITH and multipleFilters map { (producer, filter) -> LevelFilter(producer, filter) }) or
                    (nodeProducer map { LevelFilter(it, { true }) }) or
                    (multipleFilters map { LevelFilter(allSubNodes, it) })

    val levelFilterExpr: Parser<LevelFilter> by levelFilter or
            (-LPAR and parser(this::levelFilterExpr) and -RPAR)

    val listCmd by -LIST and zeroOrMore(levelFilterExpr) map
            { ListCmd(it.ifEmpty { listOf(LevelFilter(allSubNodes) { true }) }) }

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