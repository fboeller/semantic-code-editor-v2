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

fun subNodeTree(elementTypes: List<Set<ElementType>>, node: Node): Tree<Node> = when {
    elementTypes.isEmpty() -> leaf(node)
    else -> tree(node, subNodesOfTypes(elementTypes[0])(node).map { subNodeTree(elementTypes.drop(1), it) })
}

fun processCommand(command: Command): (AppState) -> AppState = when (command) {
    is ListCmd -> { appState ->
        val result = appState.project.compilationUnits
                .map { subNodeTree(command.elementTypes, it) }
                .filter { it.children.isNotEmpty() }
        appState.copy(
                result = result,
                output = ppTreeList(result) { oneLineInfo(it) })
    }
    is FocusCmd -> { appState -> appState.copy(output = "") }
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

fun repl(project: Project) {
    val terminal = TerminalBuilder.terminal();
    val reader = LineReaderBuilder.builder()
            .terminal(terminal)
            .build()
    val writer = terminal.writer()
    var appState = AppState(project, true, listOf(), listOf(), "")
    while (appState.running) {
        try {
            var line = reader.readLine("> ")
            if (line == "quit" || line == "exit") {
                appState = appState.copy(running = false)
            } else if (line.trim().isNotEmpty()) {
                val command = CommandParser.parseToEnd(line)
                appState = processCommand(command)(appState)
                writer.print(appState.output)
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
