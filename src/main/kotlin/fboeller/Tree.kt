package fboeller

interface Tree<T> {
    val children: List<Tree<T>>
    fun retrieve(indices: List<Int>): TreeNode<T>?
    fun cutEarlyLeafs(level: Int): Tree<T>
}

data class Root<T>(override val children: List<TreeNode<T>>) : Tree<T> {

    override fun retrieve(indices: List<Int>): TreeNode<T>? = when {
        indices.isEmpty() -> null
        else -> children.elementAtOrNull(indices[0])?.retrieve(indices.drop(1))
    }

    override fun cutEarlyLeafs(level: Int): Root<T> =
            if (level <= 0) this
            else root(children.map { it.cutEarlyLeafs(level - 1) })
}

data class TreeNode<T>(val data: T, override val children: List<TreeNode<T>>) : Tree<T> {

    override fun retrieve(indices: List<Int>): TreeNode<T>? = when {
        indices.isEmpty() -> this
        else -> children.elementAtOrNull(indices[0])?.retrieve(indices.drop(1))
    }

    override fun cutEarlyLeafs(level: Int): TreeNode<T> =
        if (level <= 0) this
        else {
            val newChildren = children
                    .map { it.cutEarlyLeafs(level - 1) }
                    .filter { it.children.isNotEmpty() }
            tree(data, newChildren)
        }
}

fun <T> leaf(data: T) = TreeNode(data, listOf())

fun <T> tree(data: T, children: List<TreeNode<T>>) = TreeNode(data, children)

fun <T> root(children: List<TreeNode<T>>) = Root(children)

fun <T> ppNode(tree: TreeNode<T>, print: (T) -> String, indentation: Int = 0, index: Int): String {
    val childrenString = ppChildren(tree.children, print, indentation + 1)
    return ppData(tree.data, print, indentation, index) + (if (childrenString.isEmpty()) "" else "\n") + childrenString
}

fun <T> ppData(data: T, print: (T) -> String, indentation: Int, index: Int): String =
        "  ".repeat(indentation) + index + ": " + print(data)

fun <T> ppChildren(children: List<TreeNode<T>>, print: (T) -> String, indentation: Int): String =
        children.mapIndexed { index, tree -> ppNode(tree, print, indentation, index) }
                .joinToString("\n")

fun <T> ppRoot(root: Root<T>, print: (T) -> String): String =
        ppChildren(root.children, print, 0)

fun <T> ppTree(tree: Tree<T>, print: (T) -> String): String = when (tree) {
    is TreeNode<T> -> ppChildren(tree.children, print, 0)
    is Root<T> -> ppRoot(tree, print)
    else -> throw RuntimeException("Unsupported tree type")
}
