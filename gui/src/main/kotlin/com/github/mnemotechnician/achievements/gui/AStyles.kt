package com.github.mnemotechnician.achievements.gui

import arc.Core
import arc.graphics.Color
import arc.scene.style.TextureRegionDrawable
import arc.scene.ui.TextButton.TextButtonStyle
import mindustry.gen.Tex
import mindustry.ui.Fonts
import mindustry.ui.Styles

object AStyles {
	val accent = Color.valueOf("#566594")!!
	val secondary = Color.valueOf("2c2d38")!!

	val whiteui = Tex.whiteui as TextureRegionDrawable
	val grayui = whiteui.tint(0.4f, 0.4f, 0.4f, 0.4f)!!
	val flatBorder1 = drawable("flat-border-1") as TextureRegionDrawable

	val achievementBackground = drawable("achievement-background")

	val achievementCornerUp = drawable("achievement-button-corner-up")
	val achievementCornerDown = drawable("achievement-button-corner-down")

	val achievementb = TextButtonStyle(achievementCornerUp, achievementCornerDown, achievementCornerDown, Fonts.def)
	val clearFlatTogglet = Styles.flatTogglet.copy().also {
		it.up = flatBorder1.tint(0.3f, 0.3f, 0.3f, 1f)
		it.down = flatBorder1.tint(0.1f, 0.1f, 0.1f, 1f)
		it.over = flatBorder1.tint(0.4f, 0.4f, 0.4f, 1f)
		it.checked = flatBorder1.tint(0.2f, 0.2f, 0.2f, 1f)
	}

	/** Finds a drawable of this mod or throws an exception. */
	fun drawable(name: String) = Core.atlas.drawable("achievements-$name")!!.also {
		if (it !is TextureRegionDrawable) return@also // probably always false
		if (!Core.atlas.isFound(it.region)) throw RuntimeException("Region $name is not found! (Are you accessing AStyles before the end of texture packing?)")
	}

	fun TextButtonStyle.copy() = TextButtonStyle(this)
}
