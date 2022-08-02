# Mindustry Achievements
An overcomplicated achievement framework and a standalone mod at the same time.

The functionality of this project is split into 3 separate subprojects:

| Subproject: | Module:                                                                                         |
| :----       | ----:                                                                                           |
| **core**    | the achievement framework.                                                                      |
| **gui**     | contains an achievement tree dialog neccessary to display the achievements the player has.      |
| **mod-src** | includes some built-in achievements and creates a hud button that shows the achievement dialog. |


# TODO
* More objectives and more objective events / notifications
* Use `Seq` instead of `ArrayList` in places with heavy recursion
* Optimise the AchievementTreePane grid rendering
* Create an annotation processor for listeners of `ObjectiveEvent`s / `ObjectiveNotification`s
* Custom icons for achievements
* Add "requirements" as a replacement for (or addiction to?) objective filters. Requirements modify objectives in a way that the player can easily understand.
* fix the exploit that allows to complege buildblockobjectives by rebuilding the same block
