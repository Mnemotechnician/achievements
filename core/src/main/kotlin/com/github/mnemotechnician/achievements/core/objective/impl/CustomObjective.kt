package com.github.mnemotechnician.achievements.core.objective.impl

import com.github.mnemotechnician.achievements.core.objective.AbstractCounterObjective
import com.github.mnemotechnician.achievements.core.objective.Objective
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvent
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveNotification

/**
 * An objective that requires the [checker] function to return true [targetCount] times.
 *
 * You must manually create bundle entries for this kind of objective,
 * see the KDoc of [Objective] and [AbstractCounterObjective] to get more info.
 *
 * Optionally, you can provide a list of lambdas as the [bundleArgs]
 * parameter. The values they return will be added to the list of parameters
 * of the bundle entry; the 0th lambda replaces `{0}` in the entry,
 * the 1st replaces `{1}` and so on.
 *
 * @param internalName the internal name of this objective.
 * Used for bundle entry resolution and state saving. Do not change.
 * @param targetCount the amount of times [checker] must return true.
 * @param bundleArgs a list of lambdas whose return values will be
 * provided to the description bundle of this achievement as parameters.
 * The contents of the iterable are copied once upon the creation of this objective,
 * so modifying it won't have any effect.
 * @param checker A lambda function checking whether the current conditions satisfy the objective.
 */
open class CustomObjective(
	internalName: String,
	targetCount: Int = 1,
	val bundleArgs: Iterable<() -> Any?>? = null,
	val checker: () -> Boolean
) : AbstractCounterObjective(targetCount, internalName, NO_EVENTS){
	override fun modifyBundleParams(list: MutableList<() -> Any?>) {
		bundleArgs?.forEach {
			list.add(it)
		}
	}

	override fun receiveEvent(event: ObjectiveEvent) = false

	override fun update() {
		if (checker()) count++
	}
}
