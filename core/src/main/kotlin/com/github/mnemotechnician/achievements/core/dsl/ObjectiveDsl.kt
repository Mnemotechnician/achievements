@file:Suppress("UNSUPPORTED_FEATURE" /* false positive */, "unused")
package com.github.mnemotechnician.achievements.core.dsl

import com.github.mnemotechnician.achievements.core.Achievement
import com.github.mnemotechnician.achievements.core.objective.AbstractCounterObjective
import com.github.mnemotechnician.achievements.core.objective.AbstractCounterObjective.*
import com.github.mnemotechnician.achievements.core.objective.Objective
import com.github.mnemotechnician.achievements.core.objective.impl.EitherObjective

// ignore intellij giving compilation errors, it's ok.
// this doesn't work, there might be an issue with the kotlin compiler
/** Adds an objective to this achievement, returns this objective for chaining. */
//context(Achievement)
//operator fun <T : Objective> T.unaryPlus() = also {
//	this@Achievement.addObjective(this@T)
//}

/**
 * Adds a requirement to this objective and returns the same requirement for chaining.
 */
infix fun <T : AbstractCounterObjective> T.with(requirement: Requirement) = also {
	this.requirement(requirement)
}

/** Same as `EitherObjective(*objectives)`. */
context(Achievement) // don't want it to be invocable outside dsl trees.
fun either(vararg objectives: Objective) = EitherObjective(*objectives)
