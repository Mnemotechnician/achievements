package com.github.mnemotechnician.achievements.mod.util

import com.github.mnemotechnician.mkui.delegates.bundle

object Bundles {
	val locked by abundle()
	val moreInfo by abundle()
	val lessInfo by abundle()
	val description by abundle()
	val objectives by abundle()

	fun abundle() = bundle("achievements-mod.")
}
