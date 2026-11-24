package com.charm.ehr.kiosk.presentation.tour_page

/**
 * NAVIGATION EXTENSIONS
 * * These functions wrap the raw MutableList operations to provide
 * safety checks and standard navigation behavior.
 */

/**
 * Standard "Navigate" - Adds to stack.
 * * Note: This MUST keep <T> because we are adding an item of type T.
 * * @param destination The route to add.
 * @param singleTop If true, prevents adding the route if it is already the top-most item.
 */
fun <T> MutableList<T>.navigateTo(
    destination: T,
    singleTop: Boolean = true
) {
    if (singleTop && this.isNotEmpty() && this[this.lastIndex] == destination) {
        return
    }
    this.add(destination)
}

/**
 * Standard "Pop" - Removes the top screen.
 * * FIX APPLIED: Changed <T> MutableList<T> to MutableList<*>
 * We don't need to know the type T just to remove an item. This fixes inference errors.
 */
fun MutableList<*>.popBackStack(): Boolean {
    return if (this.isNotEmpty()) {
        this.removeAt(this.lastIndex)
        true
    } else {
        false
    }
}

/**
 * The "Safety Pop" for Feature Hosts.
 * * FIX APPLIED: Changed <T> MutableList<T> to MutableList<*>
 * This resolves "Cannot infer type parameter T" errors.
 */
fun MutableList<*>.popBackStackOr(onExit: () -> Unit) {
    if (this.size > 1) {
        this.removeAt(this.lastIndex)
    } else {
        onExit()
    }
}

/**
 * Clears the entire stack and sets a new root.
 * Needs <T> because we are adding 'root'.
 */
fun <T> MutableList<T>.clearAllAndNavigateTo(root: T) {
    this.clear()
    this.add(root)
}

/**
 * Pops the stack until the [destination] is reached.
 * Needs <T> to find the specific destination object.
 */
fun <T> MutableList<T>.popBackStackTo(destination: T, inclusive: Boolean = false) {
    val index = this.indexOf(destination)
    if (index != -1) {
        val clearIndex = if (inclusive) index else index + 1
        if (clearIndex < this.size) {
            this.subList(clearIndex, this.size).clear()
        }
    }
}