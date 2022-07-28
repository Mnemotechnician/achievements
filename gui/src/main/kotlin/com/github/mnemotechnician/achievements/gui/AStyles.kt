package com.github.mnemotechnician.achievements.gui

import arc.Core
import arc.graphics.Color
import arc.scene.style.TextureRegionDrawable
import arc.scene.ui.TextButton.TextButtonStyle
import mindustry.gen.Tex
import mindustry.ui.Fonts

object AStyles {
	val accent = Color.valueOf("2C2D38FF")!!

	val whiteui = Tex.whiteui as TextureRegionDrawable
	val grayui = whiteui.tint(0.4f, 0.4f, 0.4f, 0.4f)!!

	val achievementsIcon = drawable("icon-achievements")
	val achievementBackground = drawable("achievement-background")

	val achievementCornerUp = drawable("achievement-button-corner-up")
	val achievementCornerDown = drawable("achievement-button-corner-down")

	val achievementb = TextButtonStyle(achievementCornerUp, achievementCornerDown, achievementCornerDown, Fonts.def)

	/** Finds a drawable of this mod or throws an exception. */
	fun drawable(name: String) = Core.atlas.drawable("achievements-$name")!!.also {
		if (it !is TextureRegionDrawable) return@also // probably always false
		if (!Core.atlas.isFound(it.region)) throw RuntimeException("Region $name is not found! (Are you accessing AStyles before the end of texture packing?)")
	}
}
