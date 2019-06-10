package fboeller

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.parser.ParseException
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.nodeTypes.NodeWithName
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName
import com.github.javaparser.ast.nodeTypes.NodeWithType
import com.github.javaparser.ast.type.Type
import org.jline.reader.EndOfFileException
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import org.jline.terminal.TerminalBuilder
import java.nio.file.Paths

fun subNodesOfType(elementType: ElementType): (Node) -> List<Node> = when (elementType) {
    ElementType.Class -> JavaAccessors::classes
    ElementType.Field -> JavaAccessors::fields
    ElementType.Method -> JavaAccessors::methods
    ElementType.Interface -> JavaAccessors::interfaces
    ElementType.Enum -> JavaAccessors::enums
    ElementType.Parameter -> JavaAccessors::parameters
    ElementType.Name -> JavaAccessors::names
    ElementType.Type -> JavaAccessors::types
}

fun hasName(name: String): (Node) -> Boolean = { node -> when (node) {
    is NodeWithSimpleName<*> -> node.nameAsString.startsWith(name)
    is NodeWithName<*> -> node.nameAsString.startsWith(name)
    else -> false
}}

fun hasType(type: String): (Node) -> Boolean = { node -> when (node) {
    is NodeWithType<*, *> -> node.typeAsString.startsWith(type)
    else -> false
}}

fun subNodesOfTypes(elementTypes: Set<ElementType>): (Node) -> List<Node> =
        { node -> elementTypes.flatMap { subNodesOfType(it)(node) } }

fun subNodeTree(levelFilter: List<LevelFilter>): (Node) -> TreeNode<Node> = { node -> when {
    levelFilter.isEmpty() -> leaf(node)
    else -> tree(node, levelFilter[0].producer(node)
            .filter(levelFilter[0].filter)
            .map(subNodeTree(levelFilter.drop(1))))
}}

fun list(command: ListCmd, appState: AppState): Tree<Node> = when {
    appState.focus.isEmpty() -> root(
            appState.project
                    .groupBy(oneLineInfo, subNodeTree(command.levelFilters))
                    .map { tree(it.value[0].data, it.value.flatMap { it.children }) }
                    .filter { it.children.isNotEmpty() }
    )
    else -> subNodeTree(command.levelFilters)(appState.focus.last())
}

fun processCommand(command: Command): (AppState) -> AppState = when (command) {
    is ListCmd -> { appState ->
        val result = list(command, appState)
        appState.copy(
                result = result,
                output = ppTree(result, oneLineInfo)
        )
    }
    is FocusCmd -> { appState ->
        when (command.path) {
            is IndexPath -> {
                val newLastFocus = appState.result.retrieve(command.path.indexPath)?.data
                appState.copy(
                        output = if (newLastFocus == null) "No such element" else "",
                        focus = appState.focus + listOfNotNull(newLastFocus)
                )
            }
            is DirectivePath -> appState.copy(
                    output = "",
                    focus = when (command.path.pathSymbol) {
                        PathSymbol.ROOT -> listOf()
                        PathSymbol.UP -> appState.focus.dropLast(1)
                    }
            )
        }
    }
    is ReadCmd -> { appState ->
        appState.copy(
                output = when {
                    command.indexPath.isEmpty() -> appState.focus.lastOrNull()?.toString() ?: "No content in root"
                    else -> appState.result.retrieve(command.indexPath)?.data?.toString() ?: "No such element"
                }
        )
    }
}

val oneLineInfo: (Node) -> String = { node -> when (node) {
    is ClassOrInterfaceDeclaration -> (if (node.isInterface) "interface " else "class ") + node.nameAsString
    is EnumDeclaration -> "enum " + node.nameAsString
    is CompilationUnit -> "package " + node.packageDeclaration.map { it.nameAsString }.orElse("default package")
    is FieldDeclaration -> "field " + node.variables.joinToString(", ") { it.nameAsString + ": " + it.typeAsString }
    is MethodDeclaration -> "method " + node.nameAsString + "(" +
            node.parameters.joinToString(", ") { it.nameAsString + ": " + it.typeAsString } +
            "): " + node.typeAsString
    is Parameter -> "parameter " + node.nameAsString + ": " + node.typeAsString
    is SimpleName -> "name " + node.asString()
    is Type -> "type " + node.asString()
    else -> node.toString()
}}

fun prompt(appState: AppState) = when {
    appState.focus.isEmpty() -> ""
    else -> oneLineInfo(appState.focus.last())
}

fun repl(project: List<CompilationUnit>) {
    val terminal = TerminalBuilder.terminal()
    val reader = LineReaderBuilder.builder()
            .terminal(terminal)
            .build()
    val writer = terminal.writer()
    var appState = AppState(project, true, root(listOf()), listOf(), "")
    while (appState.running) {
        try {
            var line = reader.readLine(prompt(appState) + "> ")
            if (line == "quit" || line == "exit") {
                appState = appState.copy(running = false)
            } else if (line.trim().isNotEmpty()) {
                appState = try {
                    processCommand(CommandParser.parseToEnd(line))(appState)
                } catch (e: ParseException) {
                    appState.copy(output = e.message ?: "Unknown command")
                }
                writer.print(appState.output + (if (appState.output.isEmpty()) "" else "\n"))
            }
        } catch (e: UserInterruptException) {
            // Ignore
        } catch (e: EndOfFileException) {
            return
        }
    }
}

fun main() {
    val project = ProjectParser.readDirectory(Paths.get("/home/fboeller/src/java-design-patterns/flyweight/src/main/java/com/iluwatar"))
    repl(project)
}
