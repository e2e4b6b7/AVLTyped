import AdditionResult.*
import AvlTreeInternal.*
import AvlTreeInternal.Node.Companion.Node
import Diff.*
import kotlin.jvm.JvmName

/**
 * Numbers to control the height of the tree.
 */
class Z
class S<T>

/**
 * Allowed differences between the heights of the left and right subtrees.
 */
sealed class Diff<L, R, Sum> {
    class Equal<H> : Diff<H, H, S<H>>()
    class LeftMore<H> : Diff<S<H>, H, S<S<H>>>()
    class RightMore<H> : Diff<H, S<H>, S<S<H>>>()
}

sealed interface AvlTreeInternal<T : Comparable<T>, H> {
    class Empty<T : Comparable<T>> : AvlTreeInternal<T, Z>

    class Node<T : Comparable<T>, L, R, H>(
        val value: T, val left: AvlTreeInternal<T, L>, val right: AvlTreeInternal<T, R>, val diff: Diff<L, R, H>
    ) : AvlTreeInternal<T, H> {
        @Suppress("FunctionName")
        companion object {
            /**
             * Helper functions to automatically infer the balance of height in the tree.
             */
            @JvmName("NodeEq")
            fun <T : Comparable<T>, H> Node(
                value: T, left: AvlTreeInternal<T, H>, right: AvlTreeInternal<T, H>
            ): AvlTreeInternal<T, S<H>> = Node(value, left, right, Equal())

            @JvmName("NodeRightMore")
            fun <T : Comparable<T>, H> Node(
                value: T, left: AvlTreeInternal<T, H>, right: AvlTreeInternal<T, S<H>>
            ): AvlTreeInternal<T, S<S<H>>> = Node(value, left, right, RightMore())

            @JvmName("NodeLeftMore")
            fun <T : Comparable<T>, H> Node(
                value: T, left: AvlTreeInternal<T, S<H>>, right: AvlTreeInternal<T, H>
            ): AvlTreeInternal<T, S<S<H>>> = Node(value, left, right, LeftMore())
        }
    }
}

/**
 * Auxiliary data structure to encapsulate the result in the same type.
 */
sealed interface AdditionResult<T : Comparable<T>, H> {
    class Same<T : Comparable<T>, H>(val tree: AvlTreeInternal<T, H>) : AdditionResult<T, H>
    class Rise<T : Comparable<T>, H>(val tree: AvlTreeInternal<T, S<H>>) : AdditionResult<T, H>
}

fun <T : Comparable<T>, H> addInternal(tree: AvlTreeInternal<T, H>, value: T): AdditionResult<T, H> = when (tree) {
    is Empty -> Rise(Node(value, Empty(), Empty()))
    is Node<T, *, *, H> -> addToNode(tree, value)
}

fun <T : Comparable<T>, L, R, H> addToNode(node: Node<T, L, R, H>, value: T): AdditionResult<T, H> = when {
    value < node.value -> when (val left = addInternal(node.left, value)) {
        is Same -> Same(Node(node.value, left.tree, node.right, node.diff))
        is Rise -> when (node.diff) {
            is Equal<*> -> Rise(Node(node.value, left.tree, node.right))
            is RightMore -> Same(Node(node.value, left.tree, node.right))
            is LeftMore -> rotateLeft1(left.tree, node.right, node.value)
        }
    }

    value > node.value -> when (val right = addInternal(node.right, value)) {
        is Same -> Same(Node(node.value, node.left, right.tree, node.diff))
        is Rise -> when (node.diff) {
            is Equal<*> -> Rise(Node(node.value, node.left, right.tree))
            is LeftMore -> Same(Node(node.value, node.left, right.tree))
            is RightMore -> rotateRight1(node.left, right.tree, node.value)
        }
    }

    else -> Same(node)
}

/**
 * AVL-tree rotations.
 * Every new function is created to introduce a new type parameter,
 * which actually behaves like an existential variable to control the height of trees.
 */

fun <T : Comparable<T>, H> rotateLeft1(
    left: AvlTreeInternal<T, S<S<H>>>, right: AvlTreeInternal<T, H>, value: T
): AdditionResult<T, S<S<H>>> = when (left) {
    is Node<T, *, *, S<S<H>>> -> rotateLeft2(left, right, value)
    is Empty -> error("Impossible") // Could be inferred by types
}

fun <T : Comparable<T>, LL, LR, H> rotateLeft2(
    left: Node<T, LL, LR, S<S<H>>>, right: AvlTreeInternal<T, H>, value: T
): AdditionResult<T, S<S<H>>> = when (left.diff) {
    is Equal<*> -> Rise(Node(left.value, left.left, Node(value, left.right, right)))
    is LeftMore<*> -> Same(Node(left.value, left.left, Node(value, left.right, right)))
    is RightMore<*> -> when (val lr = left.right) {
        is Node<T, *, *, S<H>> -> Same(rotateLeft3(left, lr, value, right))
        is Empty -> error("Impossible") // Could be inferred by types
    }
}

fun <H, LRL, LRR, T : Comparable<T>> rotateLeft3(
    left: Node<T, H, S<H>, S<S<H>>>, lr: Node<T, LRL, LRR, S<H>>, value: T, right: AvlTreeInternal<T, H>
): AvlTreeInternal<T, S<S<H>>> = when (lr.diff) {
    is Equal<*> -> Node(lr.value, Node(left.value, left.left, lr.left), Node(value, lr.right, right))
    is LeftMore<*> -> Node(lr.value, Node(left.value, left.left, lr.left), Node(value, lr.right, right))
    is RightMore<*> -> Node(lr.value, Node(left.value, left.left, lr.left), Node(value, lr.right, right))
}

fun <T : Comparable<T>, H> rotateRight1(
    left: AvlTreeInternal<T, H>, right: AvlTreeInternal<T, S<S<H>>>, value: T
): AdditionResult<T, S<S<H>>> = when (right) {
    is Node<T, *, *, S<S<H>>> -> rotateRight2(left, right, value)
    is Empty<*> -> error("Impossible") // Could be inferred by types
}

fun <T : Comparable<T>, RL, RR, H> rotateRight2(
    left: AvlTreeInternal<T, H>, right: Node<T, RL, RR, S<S<H>>>, value: T
): AdditionResult<T, S<S<H>>> = when (right.diff) {
    is Equal<*> -> Rise(Node(right.value, Node(value, left, right.left), right.right))
    is RightMore<*> -> Same(Node(right.value, Node(value, left, right.left), right.right))
    is LeftMore<*> -> when (val rl = right.left) {
        is Node<T, *, *, S<H>> -> Same(rotateRight3(left, value, rl, right))
        is Empty<*> -> error("Impossible") // Could be inferred by types
    }
}

fun <H, RLL, RLR, T : Comparable<T>> rotateRight3(
    left: AvlTreeInternal<T, H>, value: T, rl: Node<T, RLL, RLR, S<H>>, right: Node<T, S<H>, H, S<S<H>>>
): AvlTreeInternal<T, S<S<H>>> = when (rl.diff) {
    is Equal<*> -> Node(rl.value, Node(value, left, rl.left), Node(right.value, rl.right, right.right))
    is RightMore<*> -> Node(rl.value, Node(value, left, rl.left), Node(right.value, rl.right, right.right))
    is LeftMore<*> -> Node(rl.value, Node(value, left, rl.left), Node(right.value, rl.right, right.right))
}

/**
 * Interface functions to hide the height of the tree.
 */

typealias AvlTree<T> = AvlTreeInternal<T, *>

fun <T : Comparable<T>> add(tree: AvlTree<T>, value: T): AvlTree<T> = when (addInternal(tree, value)) {
    is Same -> tree
    is Rise -> tree
}

fun main() {
    val tree = Empty<Int>()
    val tree1 = add(tree, 1)
    val tree2 = add(tree1, 2)
}
