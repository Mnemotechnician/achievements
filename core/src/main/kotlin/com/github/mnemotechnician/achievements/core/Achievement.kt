package com.github.mnemotechnician.achievements.core

import arc.Core
import arc.Events
import arc.graphics.Color
import arc.graphics.g2d.TextureRegion
import arc.scene.style.Drawable
import arc.scene.style.TextureRegionDrawable
import com.github.mnemotechnician.achievements.core.misc.optForEach
import com.github.mnemotechnician.achievements.core.objective.Objective
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvent
import mindustry.ctype.UnlockableContent

/**
 * Represents a mindustry achievement.
 *
 * Every achievement has an [internal name][name], an [icon], a [display name][displayName] and a [description].
 * By default, the display name and description are taken from the following bundle entries:
 * - `achievement.<achievement-name>.name` - the name of the achievement.
 * - `achievement.<achievement-name>.description` - its description.
 *
 * But they can be overridden by initialising the respective fields
 * before passing the achievement to the [AchievementManager].
 *
 * Every non-root achievement must have a list of [objectives].
 *
 * @param name the internal name.
 * @param icon the icon of this achievement displayed in-game.
 */
open class Achievement(
	val name: String,
	val icon: Drawable? = null
) {
	/** The name of this achievement displayed in-game. */
	lateinit var displayName: String
	/** The description of this achievement displayed in-game. */
	lateinit var description: String

	/**
	 * Objectives required to unlock this achievement.
	 * New objectives should be added by calling [addObjective].
	 */
	val objectives = ArrayList<Objective>(5)
	/**
	 * A parent achievement which must be unlocked in order to progress on this achievement.
	 * Null if this is a root achievement.
	 *
	 * Must be set before initialising the achievement.
	 */
	var parent: Achievement? = null
		set(value) {
			require(value != this) { "An achievement can not be a parent of itself." }
			field = value
		}

	/**
	 * Child achievements.
	 * @see parent
	 */
	val children = ArrayList<Achievement>()

	/**
	 * Whether the user has unlocked this achievement.
	 * This property delegates to a setting named "achievement.achievement-name.isCompleted".
	 */
	var isCompleted by StateManager.state(false) { "achievement.$name." }
	/** Whether this achievement should be visible to the player. */
	val isUnlocked get() = parent == null || parent!!.isCompleted

	/**
	 * If [isCompleted] has forcibly been set to true, returns 1.
	 * Otherwise, returns an average of the progress of every achievement.
	 */
	open val progress: Float
		get() = if (isCompleted || objectives.isEmpty()) 1f else run {
			var p = 0f
			objectives.forEach { p += it.progress.coerceIn(progressRange) }
			p / objectives.size
		}

	/** Whether this achievement has been initialised yet. */
	var isInit = false
		protected set

	constructor(name: String, region: TextureRegion, tint: Color? = null)
		: this(name, TextureRegionDrawable(region).let { if (tint != null) it.tint(tint) else it })

	constructor(name: String, iconContent: UnlockableContent, tint: Color? = null)
		: this(name, iconContent.uiIcon, tint)

	/** Initialises this achievement. Called by the [AchievementManager] after the client load. */
	open fun init() {
		if (!::displayName.isInitialized) {
			displayName = Core.bundle.get("achievement.$name.name")
		}
		if (!::description.isInitialized) {
			description = Core.bundle.get("achievement.$name.description")
		}

		objectives.forEach { it.init() }
		isInit = true

		validate()
		update(false)
	}

	/**
	 * Ensures that this achievement is valid. Must not be called prior to initialisation.
	 * @throws IllegalStateException if something is not valid.
	 */
	fun validate() {
		fun step(condition: Boolean, message: String) {
			if (!condition) throw IllegalStateException(message)
		}

		step(isInit, "The achievement must be initialised before validating.")

		var parent = parent
		while (parent != null) {
			step(parent != this, "Cyclic achievement dependencies are not allowed.")
			parent = parent.parent
		}
	}

	/** 
	 * Updates the state of this achievement.
	 * @param silent if true, no events should be fired.
	 */
	open fun update(silent: Boolean) {
		if (objectives.all { it.isFulfilled }) {
			complete(silent)
		}
	}

	/**
	 * Adds a child achievement.
	 * If the providen achievement already has a parent, it's removed from its parent.
	 */
	open fun addChild(child: Achievement) {
		if (child == this) throw IllegalArgumentException("An achievement can not be a child of itself.")

		child.parent?.removeChild(child)
		child.parent = this
		children.add(child)
	}

	open fun removeChild(child: Achievement) = children.remove(child).also {
		if (it) child.parent = null
	}

	/** Adds an objective. */
	open fun addObjective(objective: Objective) {
		objective.parent = this
		objectives.add(objective)

		if (isInit) objective.init()
	}

	/** Adds an objective to this achievement. */
	operator fun <T : Objective> T.unaryPlus() = also {
		this@Achievement.addObjective(this)
	}

	/**
	 * Notifies this achievement that an event has occurred,
	 * progressing this achievement if the event is related to its objectives,
	 * or notifying the child achievements if it's already been completed.
	 */
	open fun handleEvent(event: ObjectiveEvent) {
		if (isCompleted) {
			children.optForEach { it.handleEvent(event) }
		} else {
			objectives.optForEach { it.handleEvent(event) }
			update(false)

			if (isCompleted) {
				// immediately notify children
				children.optForEach { it.handleEvent(event) }
			}
		}
	}

	/** 
	 * Marks this achievement as completed.
	 * @param silent if false and the achievement hasn't been completed yet, an [AchievementUnlockEvent] is fired.
	 */
	fun complete(silent: Boolean = false) {
		if (!isCompleted) {
			isCompleted = true
			if (!silent) Events.fire(AchievementUnlockEvent(this))
		}
	}

	override fun toString(): String {
		return "Achievement(name=$name, icon=$icon, isCompleted=$isCompleted)"
	}

	companion object {
		val progressRange = 0f..1f
	}

	/**
	 * Fired when an achievement is unlocked.
	 */
	class AchievementUnlockEvent(val achievement: Achievement)
}
