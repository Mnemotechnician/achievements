package com.github.mnemotechnician.achievements.core.objective.impl

import com.github.mnemotechnician.achievements.core.Achievement
import com.github.mnemotechnician.achievements.core.AchievementManager
import com.github.mnemotechnician.achievements.core.objective.Objective
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvent
import com.github.mnemotechnician.mkui.delegates.dynamicBundle
import kotlin.math.roundToInt

/**
 * Requires the player to unlock an achievement with the name [achievementName]
 * (case-insensitive).
 *
 * Must not be used before initialisation.
 */
class UnlockAchievementObjective(
	val achievementName: String
) : Objective("unlock-achievement") {
	override val description by dynamicBundle(bundleName, { achievement.displayName }, { (achievement.progress * 100).roundToInt() })
	override val isFulfilled: Boolean get() = achievement.isCompleted
	override val progress get() = achievement.progress

	val achievement: Achievement by lazy {
		AchievementManager.getForName(achievementName, true) ?: run {
			throw IllegalStateException("No achievement with the name '$achievementName' found! Is the objective accessed before initialising the parent achievement?")
		}
	}

	override fun init() {
		super.init()

		var parent: Achievement? = parent
		while (parent != null) {
			if (parent == achievement) throw IllegalStateException("UnlockAchievementObjective can not depend on a parent achievement.")
			parent = parent.parent
		}
	}

	override fun handleEvent(event: ObjectiveEvent) { }

	override fun reset() { } // can't reset this objective
}
