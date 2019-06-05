package fboeller

interface Tree<T> {
    val children: List<Tree<T>>
    fun retrieve(indices: List<Int>): TreeNode<T>
}

data class Root<T>(override val children: List<TreeNode<T>>) : Tree<T> {

    override fun retrieve(indices: List<Int>): TreeNode<T> = when {
        indices.isEmpty() -> throw RuntimeException("Root can not be retrieved by index")
        else -> children[indices[0]].retrieve(indices.drop(1))
    }
}

data class TreeNode<T>(val data: T, override val children: List<TreeNode<T>>) : Tree<T> {

    override fun retrieve(indices: List<Int>): TreeNode<T> = when {
        indices.isEmpty() -> this
        else -> children[indices[0]].retrieve(indices.drop(1))
    }
}

fun <T> leaf(data: T) = TreeNode(data, listOf())

fun <T> tree(data: T, children: List<TreeNode<T>>) = TreeNode(data, children)

fun <T> root(children: List<TreeNode<T>>) = Root(children)

fun <T> ppNode(tree: TreeNode<T>, print: (T) -> String, indentation: Int = 0, index: Int): String =
        ppData(tree.data, print, indentation, index) + "\n" + ppChildren(tree.children, print, indentation + 1)

fun <T> ppData(data: T, print: (T) -> String, indentation: Int, index: Int): String =
        "  ".repeat(indentation) + index + ": " + print(data)

fun <T> ppChildren(children: List<TreeNode<T>>, print: (T) -> String, indentation: Int): String =
        children.mapIndexed { index, tree -> ppNode(tree, print, indentation, index) }
                .joinToString("")

fun <T> ppRoot(root: Root<T>, print: (T) -> String): String =
        ppChildren(root.children, print, 0)

fun <T> ppTree(tree: Tree<T>, print: (T) -> String): String = when (tree) {
    is TreeNode<T> -> ppChildren(tree.children, print, 0)
    is Root<T> -> ppRoot(tree, print)
    else -> throw RuntimeException("Unsupported tree type")
}
