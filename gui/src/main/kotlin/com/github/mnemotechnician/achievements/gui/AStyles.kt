package com.github.mnemotechnician.achievements.gui

import arc.Core
import arc.graphics.Color
import arc.scene.style.TextureRegionDrawable
import arc.scene.ui.TextButton.*
import mindustry.gen.Tex
import mindustry.ui.Fonts
import mindustry.ui.Styles
import mindustry.Vars

object AStyles {
	val accent = Color.valueOf("2C2D38FF")!!

	val whiteui = Tex.whiteui as TextureRegionDrawable
	val grayui = whiteui.tint(0.4f, 0.4f, 0.4f, 0.4f)!!

	val achievementsIcon = drawable("icon-achievements")
	val achievementBackground = drawable("achievement-background")

	val achievementCornerUp = drawable("achievement-button-corner-up")
	val achievementCornerDown = drawable("achievement-button-corner-down")

	val achievementb = TextButtonStyle(achievementCornerUp, achievementCornerDown, achievementCornerDown, Fonts.def)

	fun drawable(name: String) = Core.atlas.drawable("achievements-$name")!!
}
