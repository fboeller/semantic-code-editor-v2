package fboeller

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import javax.xml.bind.Element

fun subNodesOfType(elementType: ElementType): (Node) -> List<Node> = when (elementType) {
    ElementType.Class -> JavaAccessors::classes
    ElementType.Field -> JavaAccessors::fields
    ElementType.Method -> JavaAccessors::methods
    ElementType.Interface -> JavaAccessors::interfaces
}

fun subNodesOfTypes(elementTypes: Set<ElementType>): (Node) -> List<Node> = {
    node -> elementTypes.flatMap { subNodesOfType(it)(node) }
}

fun subNodeTree(elementTypes: List<Set<ElementType>>, node: Node): Tree<Node> = when {
    elementTypes.isEmpty() -> leaf(node)
    else -> tree(node, subNodesOfTypes(elementTypes[0])(node).map { subNodeTree(elementTypes.drop(1), it) })
}

fun processCommand(compilationUnit: CompilationUnit, command: Command): Tree<Node> = when (command) {
    is ListCmd -> subNodeTree(command.elementTypes, compilationUnit)
}

val code = """
    class A {
      private int b;
      private String c;
      private static class D {
        private int e;
        private int q;
      }
      public static int method1() { return 0; }
      private void method2(String p1) { }
      protected interface E {
        int f();
        boolean g();
        default String h() {
          try {
            return "abc";
          } catch(Exception e) {
            // Ignore
          } finally {
            System.out.println("Hello World!");
          }
        }
      }
    }
    """.trimIndent()

fun oneLineInfo(node: Node): String = when (node) {
    is ClassOrInterfaceDeclaration -> (if (node.isPublic) "public " else "") +
            (if (node.isPrivate) "private " else "") +
            (if (node.isProtected) "protected " else "") +
            (if (node.isInterface) "interface " else "class ") +
            node.nameAsString
    is CompilationUnit -> "CompilationUnit"
    is FieldDeclaration -> node.toString()
    is MethodDeclaration -> node.getDeclarationAsString(true, true, false)
    else -> node.toString()
}

fun main() {
    val command: Command = CommandParser.parseToEnd("list * * *")
    val compilationUnit = StaticJavaParser.parse(code)
    val tree = processCommand(compilationUnit, command)
    print(ppTree(tree, { oneLineInfo(it) }))
}
