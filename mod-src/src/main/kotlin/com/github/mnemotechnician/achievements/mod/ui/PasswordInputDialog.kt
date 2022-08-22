package com.github.mnemotechnician.achievements.mod.ui

import arc.Core
import arc.graphics.Color
import arc.math.Interp
import arc.scene.Action
import arc.scene.Scene
import arc.scene.actions.Actions
import arc.scene.actions.Actions.*
import arc.scene.ui.*
import arc.scene.ui.layout.Table
import com.github.mnemotechnician.achievements.mod.misc.ModBundles
import com.github.mnemotechnician.mkui.extensions.dsl.*
import com.github.mnemotechnician.mkui.extensions.elements.content
import com.github.mnemotechnician.mkui.extensions.elements.hint
import mindustry.gen.Icon
import mindustry.gen.Tex
import mindustry.ui.Styles
import java.security.MessageDigest

/**
 * Requires the user to enter a password in order to get access to [privateContainer].
 */
class PasswordInputDialog(
	val passwordHash: ByteArray
) : Dialog() {
	private lateinit var inputField: TextField

	lateinit var talkyLabel: Label
	lateinit var privateContainer: Table

	private var isUnlocked = false

	var currentPhrase = 0
	var currentErrorPhrase = 0

	init {
		if (phrases.size <= 1) throw IllegalStateException("A language bundle file must declare a least 2 secret menu phrases!")
		if (errorPhrases.isEmpty()) throw IllegalStateException("A language bundle file must declare a least 1 secret menu error phrase!")

		closeOnBack()
		addCloseButton()

		cont.addStack {
			addTable(Tex.button) {
				addLabel("", Styles.outlineLabel, wrap = true).with {
					talkyLabel = it
				}.growX().colspan(2).row()

				textField("", Styles.areaField).with {
					it.isPasswordMode = true
					it.hint = ModBundles.enterPassword
					inputField = it
				}.growX()

				imageButton(Icon.right) {
					checkPassword()
				}
			}.visible { !isUnlocked }

			addTable(Tex.button) {
				privateContainer = this
			}.visible { isUnlocked }
		}.minWidth(400f)
	}

	override fun show(stage: Scene?, action: Action?): Dialog {
		inputField.content = ""
		nextPhrase(false)
		return super.show(stage, action)
	}

	/**
	 * Shows the next phrase. If [error] is true, shows the next error phrase instead
	 */
	fun nextPhrase(error: Boolean) {
		talkyLabel.content = if (!error) {
			currentPhrase = (currentPhrase + 1) % phrases.size
			phrases[currentPhrase]
		} else {
			currentErrorPhrase = (currentErrorPhrase + 1) % errorPhrases.size
			errorPhrases[currentErrorPhrase]
		}

		talkyLabel.actions(
			translateBy(0f, 9f, 0.3f, Interp.pow2Out),
			translateBy(0f, -9f, 0.4f) { Interp.pow2In.apply(Interp.bounceOut.apply(it)) }
		)
	}

	fun checkPassword() {
		val enteredBytes = MessageDigest.getInstance("SHA-256").digest(inputField.content.toByteArray())

		if (enteredBytes.contentEquals(passwordHash)) {
			isUnlocked = true
		} else {
			nextPhrase(true)
			inputField.content = ""
			inputField.hint = ">:("
			inputField.isDisabled = true

			inputField.addAction(parallel(
				sequence(
					translateBy(-40f, 0f, 0.2f, Interp.sineOut),
					translateBy(40f, 0f, 0.5f) { Interp.pow2In.apply(Interp.bounceOut.apply(it)) },
					Actions.run {
						inputField.isDisabled = false
						inputField.hint = ModBundles.enterPassword
					}
				),
				sequence(
					color(Color.crimson, 0.2f, Interp.smooth),
					color(Color.white, 0.8f, Interp.pow2Out)
				)
			))
		}
	}

	companion object {
		val phrases = mutableListOf<String>()
		val errorPhrases = mutableListOf<String>()

		init {
			val phraseCount by ModBundles.mbundle()
			val errorPhraseCount by ModBundles.mbundle()

			for (i in 0 until phraseCount.toInt()) phrases.add(Core.bundle.get("${ModBundles.prefix}.phrases.$i"))

			for (i in 0 until errorPhraseCount.toInt()) errorPhrases.add(Core.bundle.get("${ModBundles.prefix}.err-phrases.$i"))
		}
	}
}
