package fboeller

import com.github.javaparser.ast.Node

data class AppState(
        val project: Project,
        val running: Boolean,
        val result: Root<Node>,
        val focus: List<Node>,
        val output: String
)