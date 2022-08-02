package com.github.mnemotechnician.achievements.gui

import arc.Events
import arc.graphics.Color
import arc.scene.Action
import arc.scene.Scene
import arc.scene.ui.*
import arc.scene.ui.layout.Collapser
import arc.scene.ui.layout.Table
import com.github.mnemotechnician.achievements.core.Achievement
import com.github.mnemotechnician.achievements.core.AchievementManager
import com.github.mnemotechnician.achievements.core.util.isFair
import com.github.mnemotechnician.achievements.gui.util.Bundles
import com.github.mnemotechnician.mkui.extensions.dsl.*
import com.github.mnemotechnician.mkui.extensions.elements.hint
import com.github.mnemotechnician.mkui.extensions.elements.scaleFont
import mindustry.gen.Icon
import mindustry.gen.Tex
import mindustry.ui.Styles
import kotlin.math.roundToInt

/**
 * A dialog that allows to view the achievement tree.
 */
class AchievementTreeDialog : Dialog() {
	/** If true, the inner pane will be rebuilt the next time the dialog is drawn. */
	var isInvalid = true

	val treePane = AchievementTreePane()

	lateinit var searchBar: TextField
	lateinit var searchCollapser: Collapser
	lateinit var searchPane: Table

	var maxSearchResults = 10

	init {
		setFillParent(true)
		closeOnBack()
		addCloseButton()

		titleTable.apply {
			val achievementsTitle by Bundles.adynamic({ AchievementManager.allAchievements.size })

			clearChildren()
			addLabel({ achievementsTitle }).color(AStyles.accent).growX().row()
			hsplitter(AStyles.secondary, padBottom = 0f)
		}

		cont.addStack {
			add(treePane)
			// search bar
			addTable {
				top().right()

				addTable(Tex.button) {
					textField("", Styles.areaField) {
						searchFor(it)
					}.growX().with {
						searchBar = it
						it.removeInputDialog()
						it.hint = Bundles.searchHint
					}.row()

					addCollapser(false) {
						scrollPane {
							defaults().marginBottom(5f).pad(5f).row()
							searchPane = this
						}.growX()
					}.with {
						searchCollapser = it
					}.growX().maxHeight(400f)
				}.width(400f)
			}
		}.grow()

		Events.on(Achievement.AchievementUnlockEvent::class.java) {
			isInvalid = true
		}
	}

	/** Updates the search pane. */
	fun searchFor(name: String?) {
		if (name.isNullOrBlank()) {
			searchCollapser.isCollapsed = true
		} else {
			searchPane.clearChildren()
			searchCollapser.isCollapsed = false

			val entries = AchievementManager.allAchievements
				.filter { it.parent?.isCompleted != false && (it.name.contains(name, true) || it.displayName.contains(name, true)) }
				.sortedBy {
					if (it.displayName.length <= 2) 0 else it.displayName[0].code * 10000 + it.displayName[1].code
				}

			entries.take(maxSearchResults).forEach {
				searchPane.add(SearchResult(it)).growX().row()
			}

			if (entries.size > maxSearchResults) {
				searchPane.row()
				searchPane.addLabel(Bundles.tooManyResults)
			}
		}
	}

	override fun draw() {
		if (isInvalid) treePane.rebuild()
		super.draw()
	}

	override fun show(stage: Scene?, action: Action?): Dialog {
		isInvalid = true
		return super.show(stage, action)
	}

	/** An element representing the result of a search query entered in the search bar. */
	inner class SearchResult(val achievement: Achievement) : Button() {
		val node get() = treePane.allNodes.find { it.achievement == achievement }

		init {
			addLabel("${(achievement.progress * 100).roundToInt()}%".padStart(4, '0')).color(Color.gray)
			addSpace(5f)
			addLabel(achievement.displayName).color(AStyles.accent)

			clicked {
				node?.let { treePane.traverseToNode(it) }
			}
		}
	}
}
