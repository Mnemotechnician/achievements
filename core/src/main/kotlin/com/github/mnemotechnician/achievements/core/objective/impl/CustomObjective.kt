package com.github.mnemotechnician.achievements.core.objective.impl

import com.github.mnemotechnician.achievements.core.objective.AbstractCounterObjective
import com.github.mnemotechnician.achievements.core.objective.Objective
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvent
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveNotification
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveNotifications.UpdateNotification

/**
 * An objective that requires the [checker] function to return true [targetCount] times.
 *
 * You must manually create bundle entries for this kind of objective,
 * see the KDoc of [Objective] and [AbstractCounterObjective] to get more info.
 *
 * By default, two parameters are providen to the underlying description
 * bundle of this objective:
 * * {0} - current count
 * * {1} - target count.
 *
 * Optionally, you can provide a list of lambdas as the [bundleArgs]
 * parameter. The values they return will be added to the list of parameters:
 * {2} - the return value of the first lambda, {3} - of the second, etc.
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
class CustomObjective(
	internalName: String,
	targetCount: Int = 1,
	val bundleArgs: Iterable<() -> Any?>? = null,
	val checker: () -> Boolean
) : AbstractCounterObjective(targetCount, internalName, acceptedEvents){
	override fun modifyBundleParams(list: MutableList<() -> Any?>) {
		bundleArgs?.forEach {
			list.add(it)
		}
	}

	override fun receiveEvent(event: ObjectiveEvent): Boolean {
		return event is ObjectiveNotification && checker()
	}

	companion object {
		val acceptedEvents = setOf(UpdateNotification::class.java)
	}
}
