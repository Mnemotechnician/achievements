package com.github.mnemotechnician.achievements.core.objective.impl

import com.github.mnemotechnician.achievements.core.Achievement
import com.github.mnemotechnician.achievements.core.objective.Objective
import com.github.mnemotechnician.achievements.core.objective.ObjectiveEvent
import com.github.mnemotechnician.achievements.core.util.lazyBundle

class UnlockAchievementObjective(
	val achievement: Achievement
) : Objective("unlock-achievement") {
	override val description by lazyBundle({ bundleName }, { parent.name })
	override val isFulfilled: Boolean get() = achievement.isCompleted
	override val progress get() = achievement.progress

	override fun init() {
		super.init()

		var parent: Achievement? = parent
		while (parent != null) {
			if (parent == achievement) throw IllegalStateException("UnlockAchievementObjective can not depend on a parent achievement.")
			parent = parent.parent
		}
	}

	override fun handleEvent(event: ObjectiveEvent) { }
}
