package com.github.mnemotechnician.achievements.mod.content

import arc.graphics.Color
import com.github.mnemotechnician.achievements.core.Achievement
import com.github.mnemotechnician.achievements.core.dsl.achievement
import com.github.mnemotechnician.achievements.core.dsl.rootAchievement
import com.github.mnemotechnician.achievements.core.objective.impl.*
import mindustry.content.*
import mindustry.gen.Icon

object CoreAchievements {
	lateinit var root: Achievement

	fun load() {
		root = rootAchievement("beginning", Blocks.conveyor.uiIcon) {
			+ BuildBlocksObjective(1, Blocks.conveyor)

			achievement("enemies-coming", UnitTypes.dagger.region) { //not using uiIcon for the funnies
				+ BuildBlocksObjective(10, Blocks.copperWall, Blocks.copperWallLarge)
				+ BuildBlocksObjective(3, Blocks.duo)

				achievement("kill-enemy", Icon.defense.tint(Color.red)) {
					+ KillUnitsObjective(1, UnitTypes.dagger, UnitTypes.flare)

					achievement("siege", Icon.commandAttack.tint(Color.red)) {
						+ DestroyBlocksObjective(10, Blocks.copperWall, Blocks.duo, Blocks.scatter)
						+ DestroyBlocksObjective(40, Blocks.conveyor, Blocks.titaniumConveyor, Blocks.router)
					}
				}
			}

			achievement("begin-mining", Blocks.mechanicalDrill.uiIcon) {
				+ BuildBlocksObjective(4, Blocks.mechanicalDrill)
				+ BuildBlocksObjective(10, Blocks.conveyor)

				achievement("pressure-powered", Blocks.pneumaticDrill.uiIcon) {
					+ BuildBlocksObjective(3, Blocks.pneumaticDrill)
				}

				achievement("get-hydrated", Liquids.water.uiIcon) {
					+ BuildBlocksObjective(Blocks.mechanicalPump)
					+ BuildBlocksObjective(10, Blocks.conduit)
				}
			}

			achievement("logistics", Blocks.conveyor.uiIcon) {
				+ BuildBlocksObjective(35, Blocks.conveyor)
				+ BuildBlocksObjective(2, Blocks.router)
				+ BuildBlocksObjective(3, Blocks.junction)

				achievement("over-the-hill", Blocks.itemBridge.uiIcon) {
					+ BuildBlocksObjective(2, Blocks.itemBridge)
				}
				achievement("upgrades-people", Icon.up.tint(Color.green)) {
					+ BuildBlocksObjective(10, Blocks.titaniumConveyor)
				}
				achievement("better-choice", Blocks.overflowGate.uiIcon) {
					+ BuildBlocksObjective(1, Blocks.overflowGate, Blocks.underflowGate)
				}
			}
		}
	}
}
