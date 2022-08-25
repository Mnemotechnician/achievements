package com.github.mnemotechnician.achievements.core.objective.impl

import arc.scene.ui.layout.Table
import com.github.mnemotechnician.achievements.core.Achievement
import com.github.mnemotechnician.achievements.core.AchievementManager
import com.github.mnemotechnician.achievements.core.objective.Objective
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvent
import com.github.mnemotechnician.mkui.delegates.dynamicBundle
import com.github.mnemotechnician.mkui.extensions.dsl.*
import kotlin.math.roundToInt

/**
 * Requires the player to unlock an achievement with the name [achievementName]
 * (case-insensitive).
 *
 * Must not be used before initialisation.
 */
open class UnlockAchievementObjective(
	val achievementName: String
) : Objective("unlock-achievement") {
	override val description by dynamicBundle(bundleName, { achievement.displayName })
	override val isFulfilled: Boolean get() = achievement.isCompleted
	override val progress get() = achievement.progress

	val achievement: Achievement by lazy {
		AchievementManager.getForName(achievementName, true)?.also { achievement ->
			var parent: Achievement? = parent
			while (parent != null) {
				if (parent == achievement) throw IllegalStateException("UnlockAchievementObjective can not depend on a parent achievement.")
				parent = parent.parent
			}
		} ?: throw IllegalStateException("No achievement with the name '$achievementName' found! Is the objective accessed before initialising the parent achievement?")
	}

	override fun handleEvent(event: ObjectiveEvent) { }

	override fun reset() { } // can't reset this objective

	override fun display(target: Table) {
		target.top()
		super.display(target)
		target.label({ "${(progress * 100).roundToInt()}%" })
	}
}
