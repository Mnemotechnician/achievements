package com.github.mnemotechnician.achievements.gui

import arc.graphics.Color
import arc.graphics.g2d.*
import arc.scene.Element
import arc.scene.ui.layout.Table
import arc.util.Align.center
import arc.util.Align.left
import com.github.mnemotechnician.achievements.core.Achievement
import com.github.mnemotechnician.achievements.gui.misc.Bundles
import com.github.mnemotechnician.mkui.extensions.dsl.addLabel
import com.github.mnemotechnician.mkui.extensions.dsl.addTable
import mindustry.graphics.Pal
import java.lang.Float.min

/**
 * Displays the list of objectives of an achievement.
 *
 * When the amount of objectives the achievement has changes,
 * this table is rebuilt.
 */
class ObjectivesList(
	val achievement: Achievement
) : Table() {
	private var lastObjectiveCount = 0

	init {
		isTransform = true
		setClip(true)
		rebuild()
	}

	override fun act(delta: Float) {
		super.act(delta)

		if (achievement.objectives.size != lastObjectiveCount) {
			rebuild()
		}
	}

	override fun drawChildren() {
		if (lastObjectiveCount > 0) {
			//Draw.color(Color.gray, parentAlpha)
			//Lines.stroke(2f)

			//var minY = 999f
			//children.forEach {
			//	val y = it.getY(center)
			//	Lines.line(0f, y, it.getX(left), y)
			//	minY = min(minY, y)
			//}

			//Lines.line(0f, top,0f, minY)
		}

		super.drawChildren()
	}

	fun rebuild() {
		clearChildren()
		if (achievement.objectives.isEmpty()) {
			addLabel(Bundles.noObjectives, align = left).color(Pal.lightishGray).growX()
		} else {
			achievement.objectives.forEach { objective ->
				// completion indicator
				add(object : Element() {
					override fun draw() {
						Draw.color(if (objective.isFulfilled) Color.green else Color.crimson, parentAlpha)
						Fill.rect(x + width / 2, y + height / 2, width, height)
					}
				}).width(5f).padRight(2f).fillY()

				addTable {
					objective.display(this)
				}.growX().row()
			}
		}

		lastObjectiveCount = achievement.objectives.size
	}
}
