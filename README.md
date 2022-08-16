# Mindustry Achievements
An overcomplicated achievement framework and a standalone mod at the same time.

The functionality of this project is split into 3 separate subprojects:

| Subproject: |                                                                                         Module: |
|:------------|------------------------------------------------------------------------------------------------:|
| **core**    |                                                                      the achievement framework. |
| **gui**     |       contains an achievement tree dialog necessary to display the achievements the player has. |
| **mod-src** | includes some built-in achievements and creates a hud button that shows the achievement dialog. |


# TODO
* More objectives and more objective events / notifications
* Create an annotation processor for listeners of `ObjectiveEvent`s / `ObjectiveNotification`s
* Custom icons for achievements
* Add an objective that requires the player to own a kind of a block on the map
* Add an objevtive that requires the player to build a block of a kind (turret, wall, ...)
* Add an objective that requires the player to play in a specific environment

TOFIX
* Fix BuildBlocksObjective not decrementing on deconstruction
* Fix the first achievement not being completed when the tree is reset
