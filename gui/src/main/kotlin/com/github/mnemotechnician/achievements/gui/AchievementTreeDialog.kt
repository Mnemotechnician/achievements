package com.github.mnemotechnician.achievements.gui

import arc.Events
import arc.graphics.Color
import arc.math.Interp
import arc.scene.Action
import arc.scene.Scene
import arc.scene.actions.Actions.*
import arc.scene.event.Touchable
import arc.scene.ui.*
import arc.scene.ui.layout.Collapser
import arc.scene.ui.layout.Table
import arc.util.Align
import com.github.mnemotechnician.achievements.core.Achievement
import com.github.mnemotechnician.achievements.core.AchievementManager
import com.github.mnemotechnician.achievements.gui.util.Bundles
import com.github.mnemotechnician.mkui.extensions.dsl.*
import com.github.mnemotechnician.mkui.extensions.elements.hint
import mindustry.Vars
import mindustry.core.GameState.State
import mindustry.gen.Icon
import mindustry.gen.Tex
import mindustry.graphics.Pal
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
			// stats
			addTable {
				top().left()

				lateinit var statTable: Table
				// toggle button
				lateinit var icon: Image
				toggleButton({
					addImage(Icon.left).with { icon = it }.padRight(5f)
					addLabel({ if (!isChecked) Bundles.showInfo else Bundles.hideInfo }).color(Pal.lightishGray)
				}, AStyles.clearFlatTogglet) {
					// show or hide
					statTable.addAction(if (it) fadeIn(0.8f, Interp.smooth) else fadeOut(0.8f, Interp.pow3In))
					// rotate the icon by 90 degrees
					icon.addAction(if (it) rotateTo(90f, 1f, Interp.bounceOut) else rotateTo(0f, 1f, Interp.smooth2))
				}.size(150f, 40f).left().row()

				// info
				addTable(AStyles.flatBorder1.tint(Pal.darkOutline)) {
					statTable = this
					color.a = 0f
					touchable = Touchable.disabled
					left().defaults().left()

					val treeKind by Bundles.adynamic({ when {
						Vars.state.isCampaign -> Bundles.campaign
						Vars.net.client() -> Bundles.multiplayer
						Vars.state.`is`(State.menu).not() -> Vars.state.map?.name()
						else -> Bundles.campaign
					} })
					val totalAchievements by Bundles.adynamic({ AchievementManager.countCompleted() }, { AchievementManager.allAchievements.size })

					addLabel({ treeKind }).marginBottom(10f).row()
					addLabel({ totalAchievements })
				}.pad(5f)
			}
			// search bar
			addTable {
				top().right()

				addTable(AStyles.flatBorder1.tint(Pal.darkOutline)) {
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
				}.width(300f)
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
			addLabel("${(achievement.progress * 100).roundToInt()}%".padStart(4, ' ')).color(Color.gray)

			addLabel(achievement.displayName, align = Align.right).color(AStyles.accent).growX()

			clicked {
				node?.let { treePane.traverseToNode(it) }
			}
		}
	}
}
