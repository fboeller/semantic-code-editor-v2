package fboeller

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration

object JavaAccessors {

    fun classes(node: Node): List<ClassOrInterfaceDeclaration> = when (node) {
        is ClassOrInterfaceDeclaration ->
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
        is CompilationUnit ->
            node.types
                    .filterIsInstance<ClassOrInterfaceDeclaration>()
                    .filter { it.isInterface }
        else -> listOf()
    }

    fun fields(node: Node): List<FieldDeclaration> = when (node) {
        is ClassOrInterfaceDeclaration -> node.fields
        is CompilationUnit -> listOf()
        else -> listOf()
    }

    fun methods(node: Node): List<MethodDeclaration> = when (node) {
        is ClassOrInterfaceDeclaration -> node.methods
        is CompilationUnit -> listOf()
        else -> listOf()
    }
}