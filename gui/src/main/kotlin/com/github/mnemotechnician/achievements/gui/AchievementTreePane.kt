package com.github.mnemotechnician.achievements.gui

import arc.graphics.Color
import arc.graphics.g2d.*
import arc.input.KeyCode
import arc.math.*
import arc.math.geom.Rect
import arc.math.geom.Vec2
import arc.scene.Element
import arc.scene.Group
import arc.scene.event.*
import arc.scene.ui.Image
import arc.scene.ui.Label
import arc.scene.ui.layout.*
import arc.util.*
import com.github.mnemotechnician.achievements.core.Achievement
import com.github.mnemotechnician.achievements.core.AchievementManager
import com.github.mnemotechnician.achievements.gui.util.Bundles
import com.github.mnemotechnician.mkui.extensions.dsl.*
import mindustry.gen.Icon
import mindustry.graphics.Pal
import kotlin.math.*

/**
 * Displays all achievements registered in tue [AchievementManager] in the form of a tree.
 */
@Suppress("LeakingThis", "MemberVisibilityCanBePrivate")
open class AchievementTreePane : WidgetGroup() {
	var position = Vec2()
	/** The velocity with which the [position] changes. */
	var cameraVelocity = Vec2()
	var zoom = 1f
		set(value) {
			field = value.coerceIn(zoomRange)
			recalculateGrid()
		}
	val viewportWidth get() = width / zoom
	val viewportHeight get() = height / zoom

	val treeSize = Rect()

	/** The radius of the background hexagons. */
	var gridHexRadius = 50f
		set(value) {
			field = value.coerceIn(radiusRange)
			recalculateGrid()
		}
	private var hexMiddleLine = 0f
	private var hexVerticalOffset = 0f
	private val hexPositions = Array(4) { Vec2() }
	private val hexVertices = Array(hexGridSides + 1) { Vec2() }

	/** Whether to clip this element. */
	var clip = true
	/** Whether to keep the camera within the bounds of the achievement tree */
	var enforceBounds = true

	/** Root achievement nodes. */
	val rootNodes = ArrayList<Node>()
	/** All nodes, both root and child ones. */
	val allNodes = ArrayList<Node>()
	var nodePadding = 20f

	init {
		transform = true
		setOrigin(Align.bottomLeft)

		addCaptureListener(object : ElementGestureListener() {
			override fun pan(event: InputEvent?, x: Float, y: Float, deltaX: Float, deltaY: Float) {
				position.sub(deltaX / zoom, deltaY / zoom)
			}

			override fun zoom(event: InputEvent?, initialDistance: Float, distance: Float) {
				zoom *= sqrt(distance / initialDistance)
			}

			override fun fling(event: InputEvent?, velocityX: Float, velocityY: Float, button: KeyCode?) {
				cameraVelocity.sub(velocityX / zoom / 60f, velocityY / zoom / 60f)
			}
		})

		recalculateGrid()
	}


	override fun act(delta: Float) {
		super.act(delta)
		if (enforceBounds) keepInBounds()

		position.add(cameraVelocity.x * Time.delta, cameraVelocity.y * Time.delta)
		cameraVelocity.lerpDelta(0f, 0f, 0.1f)
	}

	/**
	 * Keeps the visible area of this pane within the bounds of the tree.
	 */
	private fun keepInBounds() {
		val trns = Tmp.v3.set(0f, 0f)

		if (position.x < treeSize.x) {
			trns.add(treeSize.x - position.x, 0f)
		} else if (position.x > treeSize.x + treeSize.width) {
			trns.add(treeSize.x + treeSize.width - position.x, 0f)
		}
		if (position.y < treeSize.y) {
			trns.add(0f, treeSize.y - position.y)
		} else if (position.y > treeSize.y + treeSize.height) {
			trns.add(0f, treeSize.y + treeSize.height - position.y)
		}

		if (!trns.isZero) {
			cameraVelocity.add(trns.scl(1 / 60f))
		}
	}

	protected fun recalculateGrid() {
		val radius = gridHexRadius * zoom

		// you better not know.
		val cornerAngle = 180f * (hexGridSides - 2) / hexGridSides
		val middleCorner = 180f - cornerAngle
		val sideSqr = 2 * radius * radius * (1 - Mathf.cosDeg(middleCorner))
		val middleLine = Mathf.sqrt((radius * radius + gridHexThickness * 2) - sideSqr / 4) * 2

		val oddOffsetAngle = hexVertexAngleStart - 360 / hexGridSides * 3.5f
		val oddOffset = Tmp.v3.set(Angles.trnsx(oddOffsetAngle, middleLine), Angles.trnsy(oddOffsetAngle, middleLine))

		hexMiddleLine = middleLine
		hexVerticalOffset = abs(oddOffset.y * 2)

		hexPositions[0].set(0f, 0f) // left top
		hexPositions[1].set(middleLine, 0f) // right top
		hexPositions[2].set(oddOffset) // bottom left
		hexPositions[3].set(oddOffset).add(middleLine, 0f) // bottom right

		repeat(hexVertices.size) {
			val angle = hexVertexAngleStart + (360f / hexGridSides) * it
			hexVertices[it].set(Angles.trnsx(angle, radius), Angles.trnsy(angle, radius))
		}
	}

	override fun draw() {
		drawGrid()
		super.draw()
	}

	override fun drawChildren() {
		// todo render connections
		super.drawChildren()
	}

	/** Draws the background grid of this element. */
	private fun drawGrid() {
		if (clip) {
			if (!clipBegin()) return
		}

		// what the fuck
		val diameter = gridHexRadius * zoom * 2
		val xStep = (hexMiddleLine * 2).toInt()
		val yStep = (hexVerticalOffset).toInt()

		val middle = Tmp.v1.set(0f, 0f).sub(position).scl(zoom).also {
			it.x %= xStep
			it.y %= yStep
		}.add(x, y)
		val zoomc = Tmp.v2.set(width, height).scl(-1 / zoom).add(width, height).scl(0.5f)

		val xStart = (middle.x - diameter - zoomc.x % xStep).toInt() - xStep
		val yStart = (middle.y - diameter - zoomc.y % yStep).toInt() - yStep
		val xEnd = (middle.x + diameter + width).toInt() + xStep
		val yEnd = (middle.x + diameter + height).toInt() + yStep

		Draw.color(AStyles.accent)
		Lines.stroke(gridHexThickness * zoom)

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

	override fun computeTransform(): Mat {
		return super.computeTransform().also {
			// this mat is an affine transform
			it.`val`[Mat.M00] *= zoom
			it.`val`[Mat.M11] *= zoom
			it.`val`[Mat.M02] += -position.x * zoom + width / 2f
			it.`val`[Mat.M12] += -position.y * zoom + height / 2f

			(groupWorldTransformField.get(this) as Affine2).set(it)
		}
	}

	override fun hit(x: Float, y: Float, touchable: Boolean): Element? {
		if (touchable && this.touchable == Touchable.disabled) return null

		val children = this.children.items
		((children.size - 1) downTo 0).forEach {
			val child = children[it]
			if (child == null || !child.visible) return@forEach

			child.parentToLocalCoordinates(unproject(tmpVec.set(x, y)))
			child.hit(tmpVec.x, tmpVec.y, touchable)?.also { return it }
		}

		if (x >= translation.x && x < width + translation.x && y >= translation.y && y < height + translation.y) return this
		return null
	}

	/** Transforms a point in the pane coordinate system into a point in the element's coordinate system. */
	fun project(point: Vec2) = point.also {
		it.x = it.x * zoom - position.x * zoom + width / 2f
		it.y = it.y * zoom - position.y * zoom + height / 2f
	}

	/** Transforms a point in the element's coordinate system into a point in the pane coordinate system. */
	fun unproject(point: Vec2) = point.also {
		it.x = (it.x - width / 2f) / zoom + position.x
		it.y = (it.y - height / 2f) / zoom + position.y
	}

	override fun layout() {
		rebuild()
		recalculateGrid()
	}

	/** Rebuilds this pane, reusing the old nodes. */
	fun rebuild() {
		var offset = 0f
		AchievementManager.achievements.forEach {
			val node = rootNodes.find { node -> node.achievement == it } ?: Node(it).also {
				rootNodes.add(it)
				allNodes.add(it)
			}

			node.rebuild()
			node.pack()

			node.x = x + offset + node.branchSize / 2
			node.y = y + 20f
			offset += node.branchSize + nodePadding

			rebuildNodeChildren(node)
		}

		treeSize.set(0f, 0f, 0f, 0f)
		allNodes.forEach {
			val coords = it.localToAscendantCoordinates(this, Tmp.v1.set(0f, 0f))
			treeSize.x = min(coords.x, treeSize.x)
			treeSize.y = min(coords.y, treeSize.y)
		}
		allNodes.forEach {
			val coords = it.localToAscendantCoordinates(this, Tmp.v1.set(0f, 0f))
			treeSize.width = max(coords.x - treeSize.x + it.width, treeSize.width)
			treeSize.height = max(coords.y - treeSize.y + it.height, treeSize.height)
		}
	}

	private fun rebuildNodeChildren(node: Node) {
		var offset = 0f
		node.childNodes.forEach { child ->
			child.rebuild()
			child.pack()

			child.x = node.x + node.prefWidth / 2 - child.prefWidth / 2 + offset + child.branchSize / 2 - node.branchSize / 2
			child.y = node.y + node.height + 40f
			offset += child.branchSize + nodePadding

			rebuildNodeChildren(child)
		}
	}

	/**
	 * Represents an achievement node.
	 * Nodes are always bound to their parent panes; when a node is created, it is added to the parent pane as a child.
	 */
	inner class Node(val achievement: Achievement) : Table(AStyles.achievementBackground) {
		private var wasUnlocked = false
		val childNodes = ArrayList<Node>()

		var branchSize = 0f

		init {
			rebuild(true)
			this@AchievementTreePane.addChild(this)
			this@AchievementTreePane.allNodes.add(this)
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

			if (achievement.parent?.isCompleted != false) createChildren()

			childNodes.forEach { it.rebuild() }
		}

		private fun rebuildImpl() {
			clearChildren()
			if (achievement.parent == null || achievement.parent!!.isCompleted) {
				addTable {
					// top bar
					addStack {
						add(object : Element() {
							override fun draw() {
								Draw.color(Pal.accent, 0.5f)
								val w = width * Mathf.clamp(achievement.progress, 0f, 1f)
								Fill.rect(x + width / 2f, y + height / 2f, w, height)
							}
						})

						add(createTable {
							addImage(achievement.icon ?: Icon.none).size(48f).margin(3f)
							addLabel(achievement.displayName).pad(3f)
						})
					}.pad(5f).growX().row()

					// more info
					lateinit var collapser: Collapser
					// todo custom style
					textToggle(Bundles.lessInfo, Bundles.moreInfo) {
						collapser.isCollapsed = !it
					}.expandX().right().row()

					addCollapser(false) {
						left().defaults().growX()

						// description
						addLabel(Bundles.description, align = Align.left).color(Color.gray).row()
						addLabel(achievement.description, wrap = true, align = Align.left).expand(false, false).row()

						// objectives
						addLabel(Bundles.objectives, align = Align.left).color(Color.gray).row()
						achievement.objectives.forEach { obj ->
							addTable {
								addLabel(if (obj.isFulfilled) "[✓] " else "").color(Color.green)

								addLabel({ obj.description }, wrap = true, align = Align.left).color(Pal.lightishGray).growX()
							}.row()
						}
					}.also { collapser = it.get() }.growX().row()
				}.minWidth(300f)
			} else {
				addStack {
					add(Image(lockedIcon).also { it.setSize(96f) })
					add(Label(Bundles.locked).also {
						it.setAlignment(Align.top)
						it.setHeight(96f)
						it.setColor(Color.red)
					})
				}.minWidth(100f)
			}
		}

		override fun layout() {
			var w = -nodePadding
			childNodes.forEach {
				it.validate()
				w += it.branchSize + nodePadding
			}
			super.layout()
			branchSize = max(prefWidth, w)
		}

		override fun draw() {
			super.draw()

			// lines between this and child nodes
			Draw.color(Color.white)
			Lines.stroke(2f)

			val from = Tmp.v1.set(getX(Align.center), getY(Align.top))
			childNodes.forEach { node ->
				val to = Tmp.v2.set(node.getX(Align.center), node.getY(Align.bottom))
				if (to.y <= from.y) return@forEach

				val vmiddle = (to.y - from.y) / 2
				Lines.curve(
					from.x, from.y,
					from.x, from.y + vmiddle,
					to.x, to.y - vmiddle,
					to.x, to.y,
					from.dst(to).roundToInt()
				)
			}
		}
	}

	companion object {
		private val groupWorldTransformField = Group::class.java.getDeclaredField("worldTransform").also {
			it.isAccessible = true
		}
		private val tmpVec = Vec2()

		val zoomRange = 0.5f..3f
		val radiusRange = 10f..500f

		const val gridHexThickness = 5f
		const val hexGridSides = 6
		const val hexVertexAngleStart = 90f

		val lockedIcon = Icon.lock!!
	}
}
