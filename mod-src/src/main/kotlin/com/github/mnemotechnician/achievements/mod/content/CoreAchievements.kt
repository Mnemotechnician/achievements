package com.github.mnemotechnician.achievements.mod.content

import arc.graphics.Color
import com.github.mnemotechnician.achievements.core.Achievement
import com.github.mnemotechnician.achievements.core.dsl.*
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvents
import com.github.mnemotechnician.achievements.core.objective.impl.*
import com.github.mnemotechnician.achievements.core.objective.impl.BuildBlockKindObjective.BlockKind
import com.github.mnemotechnician.achievements.core.objective.requirement.MiningRequirement
import com.github.mnemotechnician.achievements.core.objective.requirement.ProximityRequirement
import com.github.mnemotechnician.achievements.mod.gen.ASprites
import mindustry.content.*
import mindustry.gen.Icon

object CoreAchievements {
	lateinit var root: Achievement

	fun load() {
		root = rootAchievement("beginning", ASprites.iconSunrise) {
			achievement("enemies-coming", ASprites.iconInvader) {
				+ BuildBlockKindObjective(10, BlockKind.WALL)
				+ BuildBlockKindObjective(3, BlockKind.TURRET)

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

						achievement("massive-grind") {
							// kill 1000 units
							+ EventCounterObjective<ObjectiveEvents.UnitDestroyedEvent>(1000, "kill-enemies-total", { true })
						}
					}

					achievement("air-threat") {
						+ BuildBlocksObjective(4, Blocks.scatter)
						+ EventCounterObjective<ObjectiveEvents.UnitDestroyedEvent>(20, "kill-air-enemies") {
							it.unit.type.flying
						}

						achievement("retired-villian") {
							+ KillUnitsObjective(1, UnitTypes.antumbra, UnitTypes.eclipse)
						}
					}
				}

				achievement("alien-technology", Blocks.logicProcessor) {
					+ BuildBlocksObjective(1, Blocks.microProcessor, Blocks.logicProcessor)
				}
			}

			achievement("begin-mining", Blocks.mechanicalDrill) {
				+ BuildBlocksObjective(4, Blocks.mechanicalDrill)
				+ BuildBlockKindObjective(10, BlockKind.CONVEYOR)

				achievement("pressure-powered", Blocks.pneumaticDrill) {
					+ BuildBlocksObjective(3, Blocks.pneumaticDrill)

					achievement("advanced-optics", Blocks.laserDrill) {
						+ BuildBlocksObjective(2, Blocks.laserDrill)
						// todo this is dumb, must be changed when i implement the OwnBlockObjevtive
						+ either(
							BuildBlocksObjective(Blocks.waterExtractor)
								.with(ProximityRequirement(Blocks.laserDrill)),
							BuildBlocksObjective(1, Blocks.conduit, Blocks.pulseConduit, Blocks.bridgeConduit)
								.with(ProximityRequirement(Blocks.laserDrill))
						)

						achievement("shaking-ground", Blocks.blastDrill) {
							+ BuildBlocksObjective(Blocks.blastDrill)
						}
					}
				}

				achievement("get-hydrated", Liquids.water) {
					+ BuildBlockKindObjective(BlockKind.PUMP)
					+ BuildBlockKindObjective(10, BlockKind.CONDUIT)

					achievement("underground-waters", Blocks.waterExtractor) {
						+ BuildBlocksObjective(5, Blocks.waterExtractor)
					}
				}
			}

			achievement("logistics", Blocks.conveyor) {
				+ BuildBlockKindObjective(35, BlockKind.CONVEYOR)
				+ BuildBlockKindObjective(5, BlockKind.DISTRIBUTION)

				achievement("upgrades-people", Icon.up.tint(Color.green)) {
					+ BuildBlocksObjective(10, Blocks.titaniumConveyor)

					achievement("why", Blocks.router) {
						+ BuildBlocksObjective(5, Blocks.router)
							.with(ProximityRequirement(Blocks.router))
					}
				}

				achievement("let-be-light", Blocks.combustionGenerator) {
					+ BuildBlocksObjective(3, Blocks.combustionGenerator)
					+ BuildBlockKindObjective(5, BlockKind.DRILL)
						.with(MiningRequirement(Items.coal))
					+ BuildBlockKindObjective(BlockKind.POWER_NODE)

					achievement("steampunk", Blocks.steamGenerator) {
						+ BuildBlocksObjective(3, Blocks.steamGenerator)
						+ UnlockAchievementObjective("pressure-powered")
					}

					achievement("ecological", Blocks.solarPanel) {
						+ BuildBlocksObjective(10, Blocks.solarPanel, Blocks.largeSolarPanel)
						+ BuildBlockKindObjective(5, BlockKind.BATTERY)
					}
				}

				achievement("better-choice", Blocks.overflowGate) {
					+ BuildBlocksObjective(1, Blocks.overflowGate, Blocks.underflowGate)
				}
			}

			achievement("stonks", Blocks.coreFoundation) {
				+ OwnBlocksObjective(1, Blocks.coreFoundation, Blocks.coreNucleus)
				
				achievement("maximum-efficiency", Blocks.coreNucleus) {
					+ OwnBlocksObjective(1, Blocks.coreNucleus)

					achievement("truly-rich", Items.copper) {
						+ CollectItemsObjective(5000, Items.surgeAlloy)
						+ CollectItemsObjective(2500, Items.phaseFabric)
						+ CollectItemsObjective(Items.silicon)
						+ UnlockAchievementObjective("quite-stock")
					}
				}

				achievement("theres-more", Blocks.vault) {
					+ BuildBlocksObjective(2, Blocks.container, Blocks.vault)
						.with(ProximityRequirement(false, Blocks.coreShard, Blocks.coreFoundation, Blocks.coreNucleus))
				}

				achievement("quite-stock", Items.copper) {
					+ CollectItemsObjective(Items.copper)
					+ CollectItemsObjective(Items.lead)
				}
			}
		}
	}
}
