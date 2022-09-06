package com.github.mnemotechnician.achievements.mod.content

import arc.graphics.Color
import com.github.mnemotechnician.achievements.core.Achievement
import com.github.mnemotechnician.achievements.core.dsl.*
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvents
import com.github.mnemotechnician.achievements.core.objective.impl.*
import com.github.mnemotechnician.achievements.core.objective.impl.BuildBlockKindObjective.BlockKind
import com.github.mnemotechnician.achievements.core.objective.requirement.*
import com.github.mnemotechnician.achievements.mod.gen.ASprites
import mindustry.Vars
import mindustry.ai.types.LogicAI
import mindustry.content.*
import mindustry.gen.*
import mindustry.world.blocks.power.PowerNode.PowerNodeBuild

object CoreAchievements {
	lateinit var root: Achievement

	fun load() {
		root = rootAchievement("beginning", ASprites.iconSunrise) {
			achievement("enemies-coming", ASprites.iconInvader) {
				+ BuildBlockKindObjective(10, BlockKind.WALL)
				+ BuildBlockKindObjective(3, BlockKind.TURRET)

				achievement("kill-enemy", Icon.defense.tint(0.95f, 0.8f, 0.8f, 1f)) {
					+ KillUnitsObjective(3, UnitTypes.dagger, UnitTypes.flare)

					achievement("siege", ASprites.iconBullets) {
						+ DestroyBlocksObjective(10, Blocks.copperWall, Blocks.duo, Blocks.scatter)
						+ DestroyBlocksObjective(40, Blocks.conveyor, Blocks.titaniumConveyor, Blocks.router)

						achievement("boss", ASprites.iconBullets.tint(0.95f, 0.6f, 0.6f, 1f)) {
							+ DestroyBlocksObjective(1, Blocks.coreShard, Blocks.coreFoundation, Blocks.coreNucleus)
						}
					}

					achievement("tower-defense", ASprites.iconTower) {
						+ KillUnitsObjective(12, UnitTypes.dagger, UnitTypes.crawler)
						+ KillUnitsObjective(6, UnitTypes.flare, UnitTypes.horizon)
						+ KillUnitsObjective(2, UnitTypes.mace)

						achievement("massive-grind", ASprites.iconGrind) {
							// kill 1000 units
							+ EventCounterObjective<ObjectiveEvents.UnitDestroyedEvent>(1000, "kill-enemies-total") { true }
						}
					}

					achievement("air-threat", ASprites.iconFlakTurret) {
						+ BuildBlocksObjective(4, Blocks.scatter)
						+ EventCounterObjective<ObjectiveEvents.UnitDestroyedEvent>(20, "kill-air-enemies") {
							it.unit.type.flying
						}

						achievement("retired-villain", UnitTypes.antumbra) {
							+ KillUnitsObjective(1, UnitTypes.antumbra, UnitTypes.eclipse)
						}
					}
				}

				achievement("unbreakable", Blocks.mendProjector) {
					+ BuildBlocksObjective(8, Blocks.mender, Blocks.mendProjector)
				}

				achievement("little-miner", UnitTypes.mono) {
					+ BuildUnitsObjective(1, UnitTypes.mono)

					achievement("french-polyce", UnitTypes.poly) {
						+ BuildUnitsObjective(1, UnitTypes.poly)
					}
				}

				achievement("alien-technology", Blocks.logicProcessor) {
					+ BuildBlockKindObjective(1, BlockKind.PROCESSOR)

					achievement("slavery", UnitTypes.flare) {
						+ CustomObjective("logic-control-unit") {
							Groups.unit.contains { it.team == Vars.player.team() && it.controller() is LogicAI }
						}
					}
				}
			}

			achievement("begin-mining", Blocks.mechanicalDrill) {
				+ BuildBlocksObjective(4, Blocks.mechanicalDrill)
				+ BuildBlockKindObjective(10, BlockKind.CONVEYOR)

				achievement("pressure-powered", Blocks.pneumaticDrill) {
					+ BuildBlocksObjective(3, Blocks.pneumaticDrill)

					achievement("advanced-optics", Blocks.laserDrill) {
						+ OwnBlocksObjective(2, Blocks.laserDrill)
							.with(LiquidRequirement(Blocks.laserDrill.liquidCapacity, Liquids.water))

						achievement("shaking-ground", Blocks.blastDrill) {
							+ BuildBlocksObjective(Blocks.blastDrill)
						}
					}
				}

				achievement("get-hydrated", Liquids.water) {
					+ BuildBlockKindObjective(BlockKind.PUMP)
					+ BuildBlockKindObjective(10, BlockKind.CONDUIT)

					achievement("cold-water", Blocks.cryofluidMixer) {
						+ OwnBlocksObjective(4, Blocks.cryofluidMixer)
							.with(EfficiencyRequirement(1f))

						achievement("well-stuffed", Blocks.liquidTank) {
							+ OwnBlocksObjective(2, Blocks.liquidTank)
								.with(LiquidRequirement(Blocks.liquidTank.liquidCapacity, Liquids.cryofluid))
						}
					}

					achievement("underground-waters", Blocks.waterExtractor) {
						+ BuildBlocksObjective(5, Blocks.waterExtractor)
					}
				}
			}

			achievement("logistics", Blocks.conveyor) {
				+ BuildBlockKindObjective(35, BlockKind.CONVEYOR)
				+ BuildBlockKindObjective(5, BlockKind.DISTRIBUTION)

				achievement("upgrades-people", ASprites.iconUpgrade) {
					+ BuildBlocksObjective(10, Blocks.titaniumConveyor)

					achievement("why", Blocks.router) {
						+ BuildBlocksObjective(5, Blocks.router)
							.with(ProximityRequirement(Blocks.router))

						achievement("traitor", Blocks.router, Color.red) {
						//	+ CustomObjective("possess-router") {
						//		(Vars.player.unit() as? BlockUnitUnit)?.blockOn() == Blocks.router
						//	}
						}
					}
				}

				achievement("let-be-light", Blocks.combustionGenerator) {
					+ BuildBlocksObjective(3, Blocks.combustionGenerator)
					+ BuildBlockKindObjective(5, BlockKind.DRILL)
						.with(MiningRequirement(Items.coal))
					+ BuildBlockKindObjective(BlockKind.POWER_NODE)

					achievement("steampunk", Blocks.steamGenerator) {
						+ BuildBlocksObjective(3, Blocks.steamGenerator)
							.with(ItemRequirement(5, Items.coal))
							.with(LiquidRequirement(2f, Liquids.water))
						+ UnlockAchievementObjective("pressure-powered")

						achievement("nuclear-power", Blocks.thoriumReactor) {
							+ OwnBlocksObjective(1, Blocks.thoriumReactor)
								.with(LiquidRequirement(Blocks.thoriumReactor.liquidCapacity, Liquids.cryofluid))
								.with(ItemRequirement(Blocks.thoriumReactor.itemCapacity, Items.thorium))

							achievement("safe-way", Blocks.rtgGenerator) {
								+ BuildBlocksObjective(10, Blocks.rtgGenerator)
							}

							achievement("feel-power", Blocks.impactReactor) {
								+ OwnBlocksObjective(1, Blocks.impactReactor)
									.with(WarmupRequirement(1f))
							}
						}

						achievement("share-power", Blocks.surgeTower) {
							+ OwnBlocksObjective(2, Blocks.surgeTower)
								.with(CustomRequirement<PowerNodeBuild>("connected-to-surge-tower") {
									this.block == Blocks.surgeTower && power.links.items.any { it != 0 && Vars.world.build(it)?.block == Blocks.surgeTower }
								})
						}
					}
					
					achievement("power-savings", Blocks.batteryLarge) {
						+ BuildBlockKindObjective(20, BlockKind.BATTERY)

						achievement("ecological", Blocks.solarPanel) {
							+ BuildBlocksObjective(10, Blocks.solarPanel, Blocks.largeSolarPanel)
						}
					}
				}

				achievement("better-choice", Blocks.overflowGate) {
					+ BuildBlocksObjective(1, Blocks.overflowGate, Blocks.underflowGate)

				//	achievement("conveyor-loop") {
				//		+ BuildBlockKindObjective(1, BlockKind.CONVEYOR)
				//			.with(CustomRequirement<ConveyorBuild>("looped-conveyor") {
				//				var current: Building = this
				//				var c = 0
				//				while (current != null && ++c < 100) {
				//					current = current.next()
				//					if (current == this) return@CustomRequirement true
				//				}
				//				false
				//			})
				//	}
				}
			}

			achievement("stonks", Blocks.coreFoundation) {
				+ OwnBlocksObjective(1, Blocks.coreFoundation, Blocks.coreNucleus)
				
				achievement("maximum-efficiency", Blocks.coreNucleus) {
					+ OwnBlocksObjective(1, Blocks.coreNucleus)

					achievement("truly-rich", Items.surgeAlloy) {
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

					achievement("garbage-piles", Items.scrap) {
						+ either(
							CollectItemsObjective(Items.scrap),
							CollectItemsObjective(Items.sand),
							CollectItemsObjective(Items.coal)
						)
					}
				}
			}
		}
	}
}
