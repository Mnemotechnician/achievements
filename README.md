# Mindustry Achievements
An overcomplicated achievement framework and a standalone mod at the same time.

The functionality of this project is split into 3 separate subprojects:

|
|  -----: | Subproject: | Module:                                                                                         |
|         | **core**    | the achievement framework.                                                                      |
|         | **gui**     | contains an achievement tree dialog neccessary to display the achievements the player has.      |
|         | **mod-src** | includes some built-in achievements and creates a hud button that shows the achievement dialog. |


# TODO
* More objectives and more objective events / notifications
* Use `Seq` instead of `ArrayList` in places with heavy recursion
* Create an annotation processor for listeners of `ObjectiveEvent`s / `ObjectiveNotification`s
* Custom icons for achievements
* A gradle task generating a class containing accessors for these icons
* Split achievement saves for different saves / campaign
* StateManager should be a custom implementation of Settings, possibly one that uses kotlinx.serialization.json
