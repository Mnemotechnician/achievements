package com.github.mnemotechnician.achievements.core.misc

import arc.struct.Seq

/**
 * Optimal version of for-in-range, not allocating a kotlin range.
 *
 * Same as
 * ```java
 * for (int i = initial; doWhile(i); x += step(i)) action(i)
 * ```
 */
inline fun optFor(initial: Int, doWhile: (Int) -> Boolean, step: (Int) -> Int = { 1 }, action: (Int) -> Unit) {
	var num = initial
	while (doWhile(num)) {
		action(num)
		num += step(num)
	}
}

/**
 * See [optFor(Int, (Int) -> Boolean, (Int) -> Int, (Int) -> Unit)][optFor]
 */
inline fun optFor(initial: Float, doWhile: (Float) -> Boolean, step: (Float) -> Float = { 1f }, action: (Float) -> Unit) {
	var num = initial
	while (doWhile(num)) {
		action(num)
		num += step(num)
	}
}

/** Optimal version of [List.forEach], not allocating anything. */
inline fun <T> List<T>.optForEach(action: (T) -> Unit) {
	optFor(0, { it < size }) {
		action(this[it])
	}
}

/** Optimal version of [Array.forEach], not allocating anything. */
inline fun <T> Array<T>.optForEach(action: (T) -> Unit) {
	optFor(0, { it < size }) {
		action(this[it])
	}
}

/** Optimal version of [Seq.each], not allocating anything. */
inline fun <T> Seq<T>.optForEach(action: (T) -> Unit) {
	optFor(0, { it < size }) {
		action(this[it])
	}
}
