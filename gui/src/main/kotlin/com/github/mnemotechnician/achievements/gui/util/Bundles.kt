package com.github.mnemotechnician.achievements.gui.util

import com.github.mnemotechnician.mkui.delegates.bundle
import com.github.mnemotechnician.mkui.delegates.dynamicBundle

object Bundles {
	val locked by abundle()
	val completed by abundle()
	val moreInfo by abundle()
	val lessInfo by abundle()
	val description by abundle()
	val objectives by abundle()

	val unfairGame by abundle()
	val searchHint by abundle()
	val tooManyResults by abundle()

	fun abundle() = bundle("achievements-mod")
	fun adynamic(vararg subs: () -> Any?) = dynamicBundle("achievements-mod", *subs)
}
