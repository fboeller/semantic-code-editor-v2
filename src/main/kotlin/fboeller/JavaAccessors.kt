package fboeller

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName

object JavaAccessors {

    fun classes(node: Node): List<ClassOrInterfaceDeclaration> = when (node) {
        is ClassOrInterfaceDeclaration ->
            node.members
                    .filterIsInstance<ClassOrInterfaceDeclaration>()
                    .filter { !it.isInterface }
        is EnumDeclaration ->
            node.members
                    .filterIsInstance<ClassOrInterfaceDeclaration>()
                    .filter { !it.isInterface }
        is CompilationUnit ->
            node.types
                    .filterIsInstance<ClassOrInterfaceDeclaration>()
                    .filter { !it.isInterface }
        else -> listOf()
    }

    fun interfaces(node: Node): List<ClassOrInterfaceDeclaration> = when (node) {
        is ClassOrInterfaceDeclaration ->
            node.members
                    .filterIsInstance<ClassOrInterfaceDeclaration>()
                    .filter { it.isInterface }
        is EnumDeclaration ->
            node.members
                    .filterIsInstance<ClassOrInterfaceDeclaration>()
                    .filter { it.isInterface }
        is CompilationUnit ->
            node.types
                    .filterIsInstance<ClassOrInterfaceDeclaration>()
                    .filter { it.isInterface }
        else -> listOf()
    }

    fun enums(node: Node): List<EnumDeclaration> = when (node) {
        is ClassOrInterfaceDeclaration ->
            node.members.filterIsInstance<EnumDeclaration>()
        is EnumDeclaration ->
            node.members.filterIsInstance<EnumDeclaration>()
        is CompilationUnit ->
            node.types.filterIsInstance<EnumDeclaration>()
        else -> listOf()
    }

    fun fields(node: Node): List<FieldDeclaration> = when (node) {
        is ClassOrInterfaceDeclaration -> node.fields
        is EnumDeclaration -> node.fields
        is CompilationUnit -> listOf()
        else -> listOf()
    }

    fun methods(node: Node): List<MethodDeclaration> = when (node) {
        is ClassOrInterfaceDeclaration -> node.methods
        is EnumDeclaration -> node.methods
        is CompilationUnit -> listOf()
        else -> listOf()
    }

    fun parameters(node: Node): List<Parameter> = when (node) {
        is MethodDeclaration -> node.parameters
        else -> listOf()
    }

    fun names(node: Node): List<SimpleName> = when (node) {
        is NodeWithSimpleName<*> -> listOf(node.name)
        else -> listOf()
    }
}