package com.github.mnemotechnician.achievements.core.dsl

import arc.scene.style.Drawable
import com.github.mnemotechnician.achievements.core.Achievement
import com.github.mnemotechnician.achievements.core.AchievementManager
import com.github.mnemotechnician.achievements.core.objective.Objective

typealias Objectives = ArrayList<Objective>

/**
 * Creates a root achievement, configures it and adds it to the [AchievementManager].
 */
inline fun rootAchievement(
	name: String,
	icon: Drawable? = null,
	constructor: Achievement.() -> Unit
): Achievement = Achievement(name, icon).also {
	it.apply(constructor)
	AchievementManager.register(it)
}

/**
 * Adds a child achievement to this achievement and configures it.
 */
inline fun Achievement.achievement(
	name: String,
	icon: Drawable? = null,
	constructor: Achievement.() -> Unit
): Achievement = Achievement(name, icon).also {
	addChild(it)
	it.apply(constructor)
	AchievementManager.register(it)
}

/** Adds an objective to this achievement. */
context(Objective, Achievement)
operator fun Objective.unaryPlus() {

}
