package fboeller

data class Tree<T>(val data: T, val children: List<Tree<T>>)

fun <T> leaf(data: T) = Tree(data, listOf())

fun <T> tree(data: T, children: List<Tree<T>>) = Tree(data, children)