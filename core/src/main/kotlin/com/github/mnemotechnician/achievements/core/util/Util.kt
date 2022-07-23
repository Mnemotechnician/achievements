package com.github.mnemotechnician.achievements.core.util

import mindustry.Vars
import mindustry.gen.Unit as MindustryUnit


val MindustryUnit?.isThePlayer get() = this != null && this == Vars.player?.unit()

val MindustryUnit?.playerTeam get() = this != null && this.team() == Vars.player?.team()

val isFair get() = !Vars.state.rules.infiniteResources

inline fun <T> computeIf(condition: Boolean, block: () -> T): T? {
	return if (condition) block() else null
}
