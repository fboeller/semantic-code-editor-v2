package fboeller

data class Tree<T>(val data: T, val children: List<Tree<T>>)

fun <T> leaf(data: T) = Tree(data, listOf())

fun <T> tree(data: T, children: List<Tree<T>>) = Tree(data, children)

fun <T> ppTree(tree: Tree<T>, print: (T) -> String, indentation: Int = 0, index: Int): String =
        ppData(tree.data, print, indentation, index) + "\n" + ppChildren(tree.children, print, indentation + 1)

fun <T> ppData(data: T, print: (T) -> String, indentation: Int, index: Int): String =
        "  ".repeat(indentation) + (index + 1) + ": " + print(data)

fun <T> ppChildren(children: List<Tree<T>>, print: (T) -> String, indentation: Int): String =
        children.mapIndexed { index, tree -> ppTree(tree, print, indentation, index) }
                .joinToString("")