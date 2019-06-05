package fboeller

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.EnumDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.google.common.io.LineReader
import org.jline.reader.EndOfFileException
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import org.jline.terminal.TerminalBuilder
import java.nio.file.Paths
import java.util.*
import java.util.function.Function
import javax.xml.bind.Element

fun subNodesOfType(elementType: ElementType): (Node) -> List<Node> = when (elementType) {
    ElementType.Class -> JavaAccessors::classes
    ElementType.Field -> JavaAccessors::fields
    ElementType.Method -> JavaAccessors::methods
    ElementType.Interface -> JavaAccessors::interfaces
    ElementType.Enum -> JavaAccessors::enums
}

fun subNodesOfTypes(elementTypes: Set<ElementType>): (Node) -> List<Node> = { node ->
    elementTypes.flatMap { subNodesOfType(it)(node) }
}

fun subNodeTree(elementTypes: List<Set<ElementType>>, node: Node): TreeNode<Node> = when {
    elementTypes.isEmpty() -> leaf(node)
    else -> tree(node, subNodesOfTypes(elementTypes[0])(node).map { subNodeTree(elementTypes.drop(1), it) })
}

fun list(command: ListCmd, appState: AppState): Tree<Node> = when {
    appState.focus.isEmpty() -> root(
            appState.project
                    .groupBy({ oneLineInfo(it) }, { subNodeTree(command.elementTypes, it) })
                    .map { tree(it.value[0].data, it.value.flatMap { it.children }) }
                    .filter { it.children.isNotEmpty() }
    )
    else -> subNodeTree(command.elementTypes, appState.focus.last())
}

fun processCommand(command: Command): (AppState) -> AppState = when (command) {
    is ListCmd -> { appState ->
        val result = list(command, appState)
        appState.copy(
                result = result,
                output = ppTree(result) { oneLineInfo(it) }
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

fun oneLineInfo(node: Node): String = when (node) {
    is ClassOrInterfaceDeclaration -> (if (node.isPublic) "public " else "") +
            (if (node.isPrivate) "private " else "") +
            (if (node.isProtected) "protected " else "") +
            (if (node.isInterface) "interface " else "class ") +
            node.nameAsString
    is EnumDeclaration -> (if (node.isPublic) "public " else "") +
            (if (node.isPrivate) "private " else "") +
            (if (node.isProtected) "protected " else "") +
            "enum " +
            node.nameAsString
    is CompilationUnit -> node.packageDeclaration.map { it.nameAsString }.orElse("default package")
    is FieldDeclaration -> node.toString()
    is MethodDeclaration -> node.getDeclarationAsString(true, true, false)
    else -> node.toString()
}

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
                val command = CommandParser.parseToEnd(line)
                appState = processCommand(command)(appState)
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
