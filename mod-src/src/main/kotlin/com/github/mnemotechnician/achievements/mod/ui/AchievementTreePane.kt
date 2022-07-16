package com.github.mnemotechnician.achievements.mod.ui

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Lines
import arc.math.Angles
import arc.math.Mathf
import arc.math.geom.Vec2
import arc.scene.event.ElementGestureListener
import arc.scene.event.InputEvent
import arc.scene.ui.*
import arc.scene.ui.layout.Table
import arc.scene.ui.layout.WidgetGroup
import arc.util.*
import com.github.mnemotechnician.achievements.core.Achievement
import com.github.mnemotechnician.achievements.core.AchievementManager
import com.github.mnemotechnician.achievements.mod.ui.*
import com.github.mnemotechnician.achievements.core.util.Bundles
import com.github.mnemotechnician.mkui.extensions.dsl.*
import mindustry.gen.Icon
import mindustry.gen.Tex
import mindustry.graphics.Pal
import java.lang.Float.max
import kotlin.properties.Delegates.observable

/**
 * Displays all achievements registered in tue [AchievementManager] in the form of a tree.
 */
class AchievementTreePane : WidgetGroup() {
	var position = Vec2()
	/** The velocity with which the [position] changes. */
	var cameraVelocity = Vec2()
	var zoom = 1f
		set(value) {
			field = value.coerceIn(zoomRange)
			recalculateGrid()
		}

	var treeWidth = 100f
	var treeHeight = 100f

	val viewportWidth get() = treeWidth / zoom
	val viewportHeight get() = treeHeight / zoom

	/** The radius of the background hexagons. */
	var gridHexRadius by observable(50f) { _, _, _ -> recalculateGrid() }
	private var hexMiddleLine = 0f
	private val hexPositions = Array(4) { Vec2() }
	private val hexVertices = Array(hexGridSides + 1) { Vec2() }

	/** Whether to clip this element. */
	var clip = true

	/** Root achievement nodes. */
	val rootNodes = ArrayList<Node>()

	init {
		transform = true
		setOrigin(Align.bottomLeft)

		addCaptureListener(object : ElementGestureListener() {
			override fun pan(event: InputEvent?, x: Float, y: Float, deltaX: Float, deltaY: Float) {
				position.add(deltaX, deltaY)
			}

			override fun zoom(event: InputEvent?, initialDistance: Float, distance: Float) {
				zoom *= distance / initialDistance
			}
		})

		recalculateGrid()
	}

	override fun act(delta: Float) {
		super.act(delta)
		keepInBounds()

		position.add(cameraVelocity.x * Time.delta, cameraVelocity.y * Time.delta)
		cameraVelocity.lerpDelta(0f, 0f, 0.1f)
	}

	override fun draw() {
		drawGrid()
		super.draw()
	}

	override fun layout() {
		rebuild()
		recalculateGrid()
	}

	/**
	 * Keeps the visible area of this pane within the bounds of the tree.
	 */
	private fun keepInBounds() {
		val trns = Tmp.v3.set(0f, 0f)

		if (position.x < 0) {
			trns.add(-position.x, 0f)
		} else if (position.x + viewportWidth > treeWidth) {
			trns.add(treeWidth - position.x - viewportWidth, 0f)
		}
		if (position.y < 0) {
			trns.add(0f, -position.y)
		} else if (position.y + viewportHeight > treeHeight) {
			trns.add(0f, treeHeight - position.y - viewportHeight)
		}

		if (!trns.isZero) {
			cameraVelocity.add(trns.lerpDelta(0f, 0f, 0.85f))
		}
	}

	protected fun recalculateGrid() {
		val radius = gridHexRadius * zoom

		// you better not know.
		val cornerAngle = 180f * (hexGridSides - 2) / hexGridSides
		val middleCorner = 180f - cornerAngle
		val sideSqr = 2 * radius * radius * (1 - Mathf.cosDeg(middleCorner))
		val middleLine = Mathf.sqrt(radius * radius - sideSqr / 4) * 2

		val oddOffsetAngle = hexVertexAngleStart - 360 / hexGridSides * 3.5f
		val oddOffset = Tmp.v3.set(Angles.trnsx(oddOffsetAngle, middleLine), Angles.trnsy(oddOffsetAngle, middleLine))

		hexPositions[0].set(0f, 0f) // left top
		hexPositions[1].set(middleLine, 0f) // right top
		hexPositions[2].set(oddOffset) // bottom left
		hexPositions[3].set(oddOffset).add(middleLine, 0f) // bottom right

		repeat(hexVertices.size) {
			val angle = hexVertexAngleStart + (360f / hexGridSides) * it
			hexVertices[it].set(Angles.trnsx(angle, radius), Angles.trnsy(angle, radius))
		}

		hexMiddleLine = middleLine
	}

	/** Draws the background grid of this element. */
	private fun drawGrid() {
		if (clip) {
			if (!clipBegin()) return
		}

		// what the fuck
		val diameter = gridHexRadius * zoom * 2
		val xStep = (hexMiddleLine * 2).toInt()
		val yStep = (hexMiddleLine * 2).toInt()

		val xStart = (x + -diameter - position.x % xStep).toInt()
		val yStart = (y + -diameter - position.y % yStep).toInt()
		val xEnd = (x + diameter + width - position.x % xStep).toInt()
		val yEnd = (y + diameter + height - position.y % yStep).toInt()

		Draw.color(AStyles.accent)
		Lines.stroke(5f * zoom)

		for (hx in xStart..xEnd step xStep) {
			for (hy in yStart..yEnd step yStep) {
				hexPositions.forEachIndexed { i, hex ->
					val pos = Tmp.v3.set(hex).add(hx.toFloat(), hy.toFloat())

					(1 until hexVertices.size).forEach {
						val v1 = hexVertices[it - 1]
						val v2 = hexVertices[it]

						Lines.line(
							pos.x + v1.x,
							pos.y + v1.y,
							pos.x + v2.x,
							pos.y + v2.y
						)
					}
				}
			}
		}

		if (clip) clipEnd()
	}

	/** Rebuilds this pane, reusing the old nodes. */
	fun rebuild() {
		var offset = 0f
		AchievementManager.achievements.forEach {
			val node = rootNodes.find { node -> node.achievement == it } ?: Node(it).also {
				rootNodes.add(it)
			}

			node.rebuild()
			node.pack()

			node.x = x + offset + node.branchSize / 2
			node.y = y + 20f
			offset += node.branchSize + 10f

			rebuildNodeChildren(node)
		}
	}

	private fun rebuildNodeChildren(node: Node) {
		var offset = 0f
		node.childNodes.forEach { child ->
			child.rebuild()
			child.pack()

			child.x = node.x + offset + child.branchSize / 2
			child.y = node.y + node.height + 40f
			offset += child.branchSize

			rebuildNodeChildren(child)
		}
	}

	/**
	 * Represents an achievement node.
	 *
	 */
	inner class Node(val achievement: Achievement) : Table(AStyles.achievementBackground) {
		private var wasUnlocked = false
		val childNodes = ArrayList<Node>()

		var branchSize = 0f

		init {
			rebuild(true)
			this@AchievementTreePane.addChild(this)
		}

		/** Creates nodes for the child achievements. */
		fun createChildren() {
			if (childNodes.isNotEmpty()) childNodes.forEach { it.remove() }
			childNodes.clear()

			achievement.children.forEach {
				childNodes.add(Node(it))
			}

			invalidate()
		}

		/** Rebuilds this node or does nothing if it doesn't have to be rebuilt and [force] is false. */
		fun rebuild(force: Boolean = false) {
			val completed = achievement.isCompleted

			if (!force && wasUnlocked == completed) return
			rebuildImpl()
			wasUnlocked = completed

			if (achievement.parent?.isCompleted ?: true) createChildren()

			childNodes.forEach { it.rebuild() }
		}

		private fun rebuildImpl() {
			clearChildren()
			if (achievement.parent == null || achievement.parent!!.isCompleted) {
				addTable {
					addStack {
						add(ProgressBar(0f, 1f, 0.001f, false, AStyles.progressBar).also {
							it.update { it.value = achievement.progress }
							it.setColor(Pal.accent)
						})

						add(createTable {
							addImage(achievement.icon ?: Icon.none).size(48f).margin(3f)
							addLabel(achievement.displayName)
						})
					}.pad(5f).growX().row()

					addLabel(Bundles.objectives)
					// todo temporary
					achievement.objectives.forEach { obj ->
						addLabel({ obj.description }).row()
					}
				}
			} else {
				addStack {
					add(Image(lockedIcon).also { it.setSize(96f) })
					add(Label(Bundles.locked).also {
						it.setAlignment(Align.bottom)
						it.setColor(Color.red)
					})
				}
			}
		}

		override fun layout() {
			var w = 0f
			childNodes.forEach {
				it.validate()
				w += it.branchSize
			}
			super.layout()
			branchSize = max(prefWidth, w)
		}
	}

	companion object {
		val zoomRange = 0.5f..3f

		const val hexGridSides = 6
		const val hexVertexAngleStart = 90f

		val lockedIcon = Icon.lock!!
	}
}
