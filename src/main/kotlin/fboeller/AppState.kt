package fboeller

import com.github.javaparser.ast.Node

data class AppState(
        val project: Project,
        val running: Boolean,
        val result: List<Tree<Node>>,
        val focus: List<Node>
)