package com.github.mnemotechnician.achievements.mod.ui

import arc.Core
import arc.graphics.Color
import arc.scene.style.TextureRegionDrawable
import arc.scene.ui.ProgressBar
import mindustry.gen.Tex
import mindustry.ui.Styles

object AStyles {
	val accent = Color.valueOf("2C2D38FF")!!

	val whiteui = Tex.whiteui as TextureRegionDrawable
	val grayui = whiteui.tint(0.4f, 0.4f, 0.4f, 0.4f)!!

	val achievementBackground = drawable("achievement-background")

	val progressBar = ProgressBar.ProgressBarStyle(grayui, grayui).also {
		it.knobBefore = grayui
	}

	fun drawable(name: String) = Core.atlas.drawable("achievements-$name")!!
}
