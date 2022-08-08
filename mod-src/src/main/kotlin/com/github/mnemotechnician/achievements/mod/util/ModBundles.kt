package com.github.mnemotechnician.achievements.mod.util

import com.github.mnemotechnician.achievements.core.StateManager
import com.github.mnemotechnician.mkui.delegates.bundle
import com.github.mnemotechnician.mkui.delegates.dynamicBundle

object ModBundles {
	val achievements by mbundle()
	val resetCurrent by mbundle()
	val resetAll by mbundle()

	val resetCurrentWarning by mdynamic({ StateManager.determineSaveName() })
	val resetAllWarning by mbundle()

	fun mbundle() = bundle("achievements-mod")
	fun mdynamic(vararg funcs: () -> Any?) = dynamicBundle("achievements-mod", *funcs)
}
