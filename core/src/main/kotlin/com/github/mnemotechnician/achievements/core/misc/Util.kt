package com.github.mnemotechnician.achievements.core.misc

import mindustry.Vars
import mindustry.ctype.UnlockableContent
import mindustry.gen.Unit as MindustryUnit

val MindustryUnit?.isThePlayer get() = this != null && this == Vars.player?.unit()

val MindustryUnit?.playerTeam get() = this != null && this.team() == Vars.player?.team()

/** Returns true if the current game mode is fair. */
val isFair get() = !Vars.state.rules.infiniteResources && !Vars.state.isEditor

inline val Boolean.int get() = if (this) 1 else 0

inline fun <T> computeIf(condition: Boolean, block: () -> T): T? {
	return if (condition) block() else null
}

/** Returns the emoji of this content, or if it doesn't have one, returns its localised name. */
fun UnlockableContent.emojiOrName() = if (hasEmoji()) emoji() else localizedName
