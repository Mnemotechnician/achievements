package com.github.mnemotechnician.achievements.mod.content

import arc.graphics.Color
import com.github.mnemotechnician.achievements.core.Achievement
import com.github.mnemotechnician.achievements.core.dsl.achievement
import com.github.mnemotechnician.achievements.core.dsl.rootAchievement
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvents.BuildingEvent
import com.github.mnemotechnician.achievements.core.objective.impl.*
import mindustry.content.*
import mindustry.gen.Icon
import mindustry.world.blocks.production.Drill.DrillBuild

object CoreAchievements {
	lateinit var root: Achievement

	fun load() {
		root = rootAchievement("beginning", Blocks.conveyor) {
			achievement("enemies-coming", UnitTypes.dagger.region) { //not using uiIcon for the funnies
				+ BuildBlocksObjective(10, Blocks.copperWall, Blocks.copperWallLarge)
				+ BuildBlocksObjective(3, Blocks.duo)

				achievement("kill-enemy", Icon.defense.tint(0.95f, 0.8f, 0.8f, 1f)) {
					+ KillUnitsObjective(3, UnitTypes.dagger, UnitTypes.flare)

					achievement("siege", Icon.commandAttack.tint(Color.crimson)) {
						+ DestroyBlocksObjective(10, Blocks.copperWall, Blocks.duo, Blocks.scatter)
						+ DestroyBlocksObjective(40, Blocks.conveyor, Blocks.titaniumConveyor, Blocks.router)

						achievement("boss", Icon.commandAttack.tint(0.84f, 0.6f, 0.6f, 1f)) {
							+ DestroyBlocksObjective(1, Blocks.coreShard, Blocks.coreFoundation, Blocks.coreNucleus)
						}
					}

					achievement("tower-defense") {
						+ KillUnitsObjective(12, UnitTypes.dagger, UnitTypes.crawler)
						+ KillUnitsObjective(6, UnitTypes.flare, UnitTypes.horizon)
						+ KillUnitsObjective(2, UnitTypes.mace)
					}

					achievement("air-threat") {
						+ BuildBlocksObjective(4, Blocks.scatter)
						+ KillUnitsObjective(8, UnitTypes.flare, UnitTypes.horizon)
					}
				}

				achievement("alien-technology", Blocks.logicProcessor) {
					+ BuildBlocksObjective(1, Blocks.microProcessor, Blocks.logicProcessor)
				}
			}

			achievement("begin-mining", Blocks.mechanicalDrill) {
				+ BuildBlocksObjective(4, Blocks.mechanicalDrill)
				+ BuildBlocksObjective(10, Blocks.conveyor, Blocks.titaniumConveyor)

				achievement("pressure-powered", Blocks.pneumaticDrill) {
					+ BuildBlocksObjective(3, Blocks.pneumaticDrill)
				}

				achievement("get-hydrated", Liquids.water) {
					+ BuildBlocksObjective(Blocks.mechanicalPump)
					+ BuildBlocksObjective(10, Blocks.conduit)

					achievement("underground-waters", Blocks.waterExtractor) {
						+ BuildBlocksObjective(5, Blocks.waterExtractor)
					}
				}
			}

			achievement("logistics", Blocks.conveyor) {
				+ BuildBlocksObjective(35, Blocks.conveyor)
				+ BuildBlocksObjective(2, Blocks.router)
				+ BuildBlocksObjective(3, Blocks.junction)

				achievement("over-the-hill", Blocks.itemBridge) {
					+ BuildBlocksObjective(2, Blocks.itemBridge)
				}

				achievement("upgrades-people", Icon.up.tint(Color.green)) {
					+ BuildBlocksObjective(10, Blocks.titaniumConveyor)

					achievement("why", Blocks.router) {
						+ BuildBlocksObjective(5, Blocks.router, Blocks.router, Blocks.router).filter {
							(it as? BuildingEvent ?: return@filter false).building.proximity.any {
								it?.block() == Blocks.router
							}
						}
					}
				}

				achievement("let-be-light", Blocks.combustionGenerator) {
					+ BuildBlocksObjective(3, Blocks.combustionGenerator)
					+ BuildBlocksObjective(5, Blocks.mechanicalDrill, Blocks.pneumaticDrill).filter {
						((it as? BuildingEvent)?.building as? DrillBuild)?.dominantItem == Items.coal
					}

					achievement("steampunk", Blocks.steamGenerator) {
						+ BuildBlocksObjective(3, Blocks.steamGenerator)
						+ UnlockAchievementObjective("pressure-powered")
					}

					achievement("ecological", Blocks.solarPanel) {
						+ BuildBlocksObjective(10, Blocks.solarPanel)
						+ BuildBlocksObjective(5, Blocks.battery, Blocks.batteryLarge)
					}
				}

				achievement("better-choice", Blocks.overflowGate) {
					+ BuildBlocksObjective(1, Blocks.overflowGate, Blocks.underflowGate)
				}
			}

			achievement("stonks", Blocks.coreFoundation) {
				+ BuildBlocksObjective(Blocks.coreFoundation)
				+ UnlockAchievementObjective("steampunk")
				+ UnlockAchievementObjective("pressure-powered")
				
				achievement("maximum-efficiency", Blocks.coreNucleus) {
					+ BuildBlocksObjective(Blocks.coreNucleus)
				}
			}
		}
	}
}
