package fboeller

sealed class Tree<T>
data class Leaf<T>(val data: T) : Tree<T>()
data class Node<T>(val children: List<Tree<T>>) : Tree<T>()