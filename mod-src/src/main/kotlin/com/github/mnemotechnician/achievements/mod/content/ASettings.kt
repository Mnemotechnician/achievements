package com.github.mnemotechnician.achievements.mod.content

import arc.graphics.Color
import arc.math.Mathf
import arc.scene.style.TextureRegionDrawable
import arc.scene.ui.TextField
import arc.scene.ui.layout.Table
import arc.util.Log
import arc.util.Reflect
import com.github.mnemotechnician.achievements.core.*
import com.github.mnemotechnician.achievements.core.objective.AbstractCounterObjective
import com.github.mnemotechnician.achievements.core.objective.Objective
import com.github.mnemotechnician.achievements.core.objective.impl.EitherObjective
import com.github.mnemotechnician.achievements.mod.misc.ModBundles
import com.github.mnemotechnician.achievements.mod.ui.PasswordInputDialog
import com.github.mnemotechnician.mkui.extensions.dsl.*
import com.github.mnemotechnician.mkui.extensions.elements.content
import com.github.mnemotechnician.mkui.extensions.elements.hint
import com.github.mnemotechnician.mkui.ui.element.toggle
import mindustry.Vars
import mindustry.gen.Icon
import mindustry.gen.Tex
import mindustry.ui.Styles
import mindustry.ui.dialogs.BaseDialog
import java.lang.reflect.Modifier

object ASettings {
	val debugPasswordHash = byteArrayOf(-103, 45, -75, -30, 89, 87, 37, -17, 104, -21, 80, 102, -32, 20, -38, -116, 127, 107, -84, -96, -81, -87, 111, 16, 33, -75, 107, 102, 120, 33, 51, -14)
	val debugDialog by lazy { PasswordInputDialog(debugPasswordHash) }

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

					createDialog { dialog ->
						lateinit var textField: TextField
						addTable(Tex.button) {
							// warning
							addLabel(ModBundles.resetAllWarning, wrap = true).fillX().row()
							hsplitter(padTop = 30f, padBottom = 4f)
							addLabel(resetAllConfirm, wrap = true).growX().row()

							// confirmation field
							textField = textField().with {
								it.filter = TextField.TextFieldFilter.digitsOnly
							}.padBottom(10f).growX().get()
						}.fillX().minWidth(400f).row()

						// button row
						addTable(Tex.button) {
							textButton("@cancel") { dialog.hide() }.growX()
							textButton("@confirm") {
								StateManager.root.deleteDirectory()
								StateManager.loadState(null)
							}.disabled { textField.content != confirmNumber }.growX()
						}.fillX()
					}.show()
				}.row()

				addLabel("").growY().row()

				imageButton(Icon.terminal, Styles.flati) {
					debugDialog.show()
				}.color(Color.valueOf("494d5655"))
			}.fillY()
		}

		// debug menu dialog
		debugDialog.privateContainer.apply {
			// not creating language bundles for these
			addLabel("The secret debug dialog.").marginBottom(20f).row()

			addTable {
				defaults().fill().left()

				addLabel("achievements")
				textButton("complete all") {
					AchievementManager.allAchievements.forEach { it.complete(false) }
				}
				textButton("lock all") {
					AchievementManager.allAchievements.forEach { it.isCompleted = false }
				}
				textButton("choose") {
					createBaseDialog("choose achievements to lock/unlock", addCloseButton = true) {
						scrollPane {
							defaults().fill()
							AchievementManager.allAchievements.sortedBy { it.name }.forEach { achievement ->
								textToggle(achievement.name) {
									achievement.isCompleted = it
									if (it) achievement.complete(false) // fire an unlock event
								}.toggle(achievement.isCompleted)

								if (children.size % 5 == 0) row()
							}
						}.grow()
					}.show()
				}
				textButton("define") {
					// possible icons
					val iconVariants = mapOf(
						"icon" to Icon::class.java.declaredFields.filter { Modifier.isStatic(it.modifiers) }.mapNotNull {
							(it.get(null) as? TextureRegionDrawable)?.region
						},
						"blocks" to Vars.content.blocks().map { it.fullIcon },
						"resources" to Vars.content.run { items() + liquids() }.map { it.fullIcon },
						"units" to Vars.content.units().map { it.fullIcon }
					)
					// dialog itself
					createBaseDialog("define an achievement", addCloseButton = true) {
						lateinit var name: TextField
						lateinit var displayName: TextField
						lateinit var description: TextField
						lateinit var parentAchievement: TextField
						var achievementIcon = Icon.none.region
						var iconTint = Color.white

						defaults().fill()

						// i don't care about this allocation, this is a debug menu, and it will be optimised by jit anyways
						addImage({ TextureRegionDrawable(achievementIcon).tint(iconTint) }).size(48f).get().clicked {
							// show a dialog for the user to choose an icon
							createDialog("choose an icon", addCloseButton = true) {
								lateinit var buttons: Table

								// icon groups
								addTable(Tex.button) {
									iconVariants.entries.forEach { (name, images) ->
										// icon group switch
										textButton(name) {
											buttons.clear()
											images.forEach { image ->
												// icon
												buttons.imageButton(image) {
													achievementIcon = image
												}.size(48f)
												if (buttons.children.size % 10 == 0) buttons.row()
											}
										}
									}
								}.row()
								// icon list - populated by the buttons above
								scrollPane {
									background = Tex.button
									buttons = this
								}.minHeight(400f).grow()
							}.show()
						}
						button("tint") {
							Vars.ui.picker.show(iconTint) { iconTint = it }
						}
						// internal name
						textField("new-achievement").with {
							name = it
							it.hint = "internal name"
						}.width(200f)
						// display name
						textField("New Achievement").with {
							displayName = it
							it.hint = "display name"
						}.width(300f).row()
						// description
						textArea("This is a new achievement").with {
							description = it
							it.hint = "description"
						}.colspan(4).fillX().row()
						// parent name
						textField().with {
							parentAchievement = it
							it.hint = "parent name or empty"
						}.fillX().colspan(3)
						// define button
						textButton("define") {
							try {
								require(name.content.isNotEmpty()) { "Internal name can not be empty!" }
								require(AchievementManager.allAchievements.none { it.name == name.content }) { "Achievement ${name.content} already exists!"}

								val achievement = Achievement(name.content, achievementIcon, iconTint).also {
									if (displayName.content.isNotEmpty()) it.displayName = displayName.content
									if (description.content.isNotEmpty()) it.description = description.content
								}
								if (parentAchievement.content.isNotEmpty()) {
									val parent = AchievementManager.getForName(parentAchievement.content, true)
										?: error("Achievement with name '${parentAchievement.content}' doesn't exist!")
									parent.addChild(achievement)
								}
								AchievementManager.register(achievement)
							} catch (e: Exception) {
								Vars.ui.showException(e)
							}
						}.fillX()
					}.show()
				}

				row()

				addLabel("objectives")
				textButton("fulfill all") {
					AchievementManager.allAchievements.forEach { achievement ->
						// almost all objectives are abstract counter objectives
						fun complete(obj: Objective) {
							when (obj) {
								is AbstractCounterObjective -> obj.count = obj.targetCount
								is EitherObjective -> obj.objectives.forEach { complete(it) }
							}
						}
						achievement.objectives.forEach { complete(it) }
					}
				}
				textButton("reset all") {
					AchievementManager.allAchievements.forEach {
						it.objectives.forEach(Objective::reset)
					}
				}
			}
		}
	}
}
