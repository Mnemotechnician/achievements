package com.github.mnemotechnician.achievements.mod.content

import arc.math.Mathf
import arc.scene.ui.TextField
import arc.util.Log
import arc.util.Reflect
import com.github.mnemotechnician.achievements.core.StateManager
import com.github.mnemotechnician.achievements.mod.util.ModBundles
import com.github.mnemotechnician.mkui.extensions.dsl.*
import com.github.mnemotechnician.mkui.extensions.elements.content
import mindustry.Vars
import mindustry.gen.Icon
import mindustry.gen.Tex
import mindustry.ui.Styles
import mindustry.ui.dialogs.BaseDialog

object ASettings {
	fun init() {
		try {
			Reflect.get(Vars.ui.settings, "dataDialog") as BaseDialog
		} catch (e: Exception) {
			Log.err("Cannot access Vars.ui.settings,dataDialog", e)
			return
		}.cont.apply {
			addTable(Tex.button) {
				top().defaults().size(280f, 60f).left()

				addLabel(ModBundles.achievements).row()

				// reset achievements in the current map/campaign
				button(ModBundles.resetCurrent, Icon.trash, Styles.flatt) {
					Vars.ui.showConfirm(ModBundles.resetCurrentWarning) {
						StateManager.loadState(null)
					}
				}.row()

				// reset all achievements
				button(ModBundles.resetAll, Icon.trash, Styles.flatt) {
					val confirmNumber = Mathf.random(1000, 9999).toString()
					val resetAllConfirm by ModBundles.mdynamic({ confirmNumber })

					createDialog {
						lateinit var textField: TextField
						cont.addTable(Tex.button) {
							// warning
							addLabel(ModBundles.resetAllWarning, wrap = true).fillX().row()
							hsplitter(padTop = 30f, padBottom = 4f)
							addLabel(resetAllConfirm, wrap = true).growX().row()

							// confirmation field
							textField = textField().with {
								it.filter = TextField.TextFieldFilter.digitsOnly
							}.padBottom(10f).growX().get()
						}.fillX().minWidth(300f).row()

						// button row
						cont.addTable(Tex.button) {
							textButton("@cancel") { this@createDialog.hide() }.growX()
							textButton("@confirm") {
								StateManager.root.deleteDirectory()
								StateManager.loadState(null)
							}.disabled { textField.content != confirmNumber }.growX()
						}.fillX()
					}.show()
				}
			}.fillY()
		}
	}
}
