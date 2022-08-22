package com.github.mnemotechnician.achievements.core.objective.impl

import arc.Core
import com.github.mnemotechnician.achievements.core.objective.AbstractCounterObjective
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvent
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvents
import com.github.mnemotechnician.achievements.core.objective.impl.BuildBlockKindObjective.BlockKind
import mindustry.Vars
import mindustry.world.Block
import mindustry.world.blocks.ConstructBlock
import mindustry.world.blocks.defense.Wall
import mindustry.world.blocks.defense.turrets.BaseTurret
import mindustry.world.blocks.distribution.*
import mindustry.world.blocks.liquid.*
import mindustry.world.blocks.logic.LogicBlock
import mindustry.world.blocks.power.*
import mindustry.world.blocks.production.*
import mindustry.world.blocks.storage.CoreBlock
import mindustry.world.blocks.storage.Unloader
import kotlin.math.max

/**
 * Similar to [BuildBlocksObjective], but requires the player to build blocks of a specific kind
 * (e.g. turret, wall) rather than a one of specific blocks.
 *
 * Like [BuildBlocksObjective], this class decrements [count] when the user deconstructs a block
 * specified in [kinds]. However, due to how [BlockKind] and [TileIndexer] work, it's rather
 * impossible to acquire the [Building] that was deconstructed. Therefore, when the player
 * deconstructs a block specified in [kinds], the counter is decremented without checking
 * the filters and requirements of this objective.
 */
class BuildBlockKindObjective(
	targetCount: Int,
	vararg val kinds: BlockKind
) : AbstractCounterObjective(targetCount, "build-blocks", BuildBlocksObjective.acceptedEvents) {
	val kindsDescription by lazy { kinds.joinToString(", ") { it.displayName } }

	constructor(vararg kinds: BlockKind) : this(1, *kinds)

	override fun modifyBundleParams(list: MutableList<() -> Any?>) {
		list.add(0) { kindsDescription }
	}

	// mostly copied from BuildBlocksObjective
	override fun receiveEvent(event: ObjectiveEvent): Boolean {
		if (event is ObjectiveEvents.ConstructionEvent) {
			return event.building.team() == Vars.player.team() && kinds.any { it.check(event.building.block) }
		} else if (event is ObjectiveEvents.DeconstructionEvent) {
			val block: Block = (event.building as? ConstructBlock.ConstructBuild)?.let {
				it.previous ?: it.prevBuild.takeIf { !it.isEmpty }?.first()?.block
			} ?: event.building.block

			// decrement if the target building was deconstructed...
			// since we can't acquire a Building, which filters and requirements rely on, isAccepted() is not called.
			if (event.building.team() == Vars.player.team() && kinds.any { it.check(block) }) {
				count = max(0, count - 2)
			}
		}
		return false
	}

	enum class BlockKind(val check: (Block) -> Boolean) {
		/** Any block. */
		ANY({ true }),
		/** Nothing, just you and your imagination. */
		NOTHING({ false }),

		TURRET({ it is BaseTurret }),
		WALL({ it is Wall }),

		CONVEYOR({ it is Conveyor }),
		DUCT({ it is Duct }),
		/** Router, junction, flow gate, sorter... */
		DISTRIBUTION({ it is Router || it is Sorter || it is OverflowGate || it is Junction }),
		DUCT_DISTRIBUTION({ it is DuctRouter }),
		BRIDGE({ it is ItemBridge }),
		DUCT_BRIDGE({ it is DuctBridge }),
		ANY_LOGISTIC({
			CONVEYOR.check(it) || DUCT.check(it) || DISTRIBUTION.check(it) || DUCT_DISTRIBUTION.check(it)
				|| BRIDGE.check(it) || DUCT_BRIDGE.check(it) || UNLOADER.check(it)
		}),

		UNLOADER({ it is Unloader || it is DirectionalUnloader }),


		PROCESSOR({ it is LogicBlock }),
		ANY_LOGIC({ PROCESSOR.check(it) }),

		CONDUIT({ it is Conduit }),
		CONDUIT_BRIDGE({ it is LiquidBridge }),
		CONDUIT_DISTRIBUTION({ it is LiquidRouter || it is LiquidRouter }),

		GENERATOR({ it is PowerGenerator }),
		REACTOR({ it is NuclearReactor }),
		BATTERY({ it is Battery }),
		POWER_NODE({ it is BeamNode || it is PowerNode }),

		DRILL({ it is BeamDrill || it is Drill }),
		PUMP({ it is Pump }),
		FACTORY({ it is GenericCrafter }),

		CORE({ it is CoreBlock });

		val displayName by lazy { Core.bundle.get("block-kind.${
			name.lowercase().replace('_', '-')
		}.name") }
	}
}
