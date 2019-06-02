package fboeller

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node

fun subNodes(elementType: ElementType, node: Node): List<Node> = when (elementType) {
    ElementType.Class -> JavaAccessors.classes(node)
    ElementType.Field -> JavaAccessors.fields(node)
    ElementType.Method -> JavaAccessors.methods(node)
}

fun subNodes(elementTypes: List<ElementType>, node: Node): Tree<Node> = when {
    elementTypes.isEmpty() -> Leaf(node)
    else -> Node(subNodes(elementTypes[0], node).map { subNodes(elementTypes.drop(1), it) })
}

fun processCommand(compilationUnit: CompilationUnit, command: Command) = when (command) {
    is ListCmd -> subNodes(command.elementTypes, compilationUnit)
}

val code = """
    class A {
      private int b;
      private String c;
      private static class D {
        private int e;
      }
      public static int method1() { return 0; }
      private void method2(String p1) { }
    }
    """.trimIndent()

fun main() {
    val result: Command = CommandParser.parseToEnd("list class method")
    val compilationUnit = StaticJavaParser.parse(code)
    println(processCommand(compilationUnit, result))
}
