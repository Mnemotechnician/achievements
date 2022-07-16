package com.github.mnemotechnician.achievements.core.util

import com.github.mnemotechnician.mkui.delegates.bundle

object Bundles {
	val locked by abundle()
	val objectives by abundle()

	fun abundle() = bundle("achievements-mod.")
}
