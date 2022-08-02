@file:Suppress("UNSUPPORTED_FEATURE" /* false positive */, "unused")
package com.github.mnemotechnician.achievements.core.dsl

import arc.graphics.Color
import arc.graphics.g2d.TextureRegion
import arc.scene.style.Drawable
import com.github.mnemotechnician.achievements.core.Achievement
import com.github.mnemotechnician.achievements.core.AchievementManager
import com.github.mnemotechnician.achievements.core.objective.Objective
import mindustry.ctype.UnlockableContent

typealias Objectives = ArrayList<Objective>

/** Creates a root achievement, configures it and adds it to the [AchievementManager]. */
inline fun rootAchievement(
	name: String,
	icon: Drawable? = null,
	constructor: Achievement.() -> Unit
): Achievement = Achievement(name, icon).also {
	it.apply(constructor)
	AchievementManager.register(it)
}

/** Same as [rootAchievement]. */
inline fun rootAchievement(
	name: String,
	region: TextureRegion,
	color: Color? = null,
	constructor: Achievement.() -> Unit
): Achievement = Achievement(name, region, color).also {
	it.apply(constructor)
	AchievementManager.register(it)
}

/** Same as [rootAchievement]. */
inline fun rootAchievement(
	name: String,
	iconContent: UnlockableContent,
	color: Color? = null,
	constructor: Achievement.() -> Unit
): Achievement = Achievement(name, iconContent, color).also {
	it.apply(constructor)
	AchievementManager.register(it)
}

/** Adds a child achievement to this achievement and configures it. */
inline fun Achievement.achievement(
	name: String,
	icon: Drawable? = null,
	constructor: Achievement.() -> Unit
): Achievement = Achievement(name, icon).also {
	addChild(it)
	it.apply(constructor)
	AchievementManager.register(it)
}

/** Same as [achievement]. */
inline fun Achievement.achievement(
	name: String,
	region: TextureRegion,
	color: Color? = null,
	constructor: Achievement.() -> Unit
): Achievement = Achievement(name, region, color).also {
	addChild(it)
	it.apply(constructor)
	AchievementManager.register(it)
}

/** Same as [achievement]. */
inline fun Achievement.achievement(
	name: String,
	iconContent: UnlockableContent,
	color: Color? = null,
	constructor: Achievement.() -> Unit
): Achievement = Achievement(name, iconContent, color).also {
	addChild(it)
	it.apply(constructor)
	AchievementManager.register(it)
}

/** Adds an objective to this achievement. */
context(Objective, Achievement)
operator fun Objective.unaryPlus() {
	this@Achievement.objectives.add(this@Objective)
}
