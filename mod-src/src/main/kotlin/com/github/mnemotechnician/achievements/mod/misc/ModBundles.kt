package com.github.mnemotechnician.achievements.mod.misc

import com.github.mnemotechnician.achievements.core.StateManager
import com.github.mnemotechnician.mkui.delegates.bundle
import com.github.mnemotechnician.mkui.delegates.dynamicBundle

object ModBundles {
	val achievements by mbundle()
	val resetCurrent by mbundle()
	val resetAll by mbundle()
	val enterPassword by mbundle()

	val resetCurrentWarning by mdynamic({ StateManager.determineSaveName() })
	val resetAllWarning by mbundle()

	const val prefix = "achievements-mod"

	fun mbundle() = bundle(prefix)
	fun mdynamic(vararg funcs: () -> Any?) = dynamicBundle(prefix, *funcs)
}
