package fboeller

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node

data class AppState(
        val project: List<CompilationUnit>,
        val running: Boolean,
        val result: Tree<Node>,
        val focus: List<Node>,
        val output: String
)