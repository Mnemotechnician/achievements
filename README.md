# Mindustry Achievements
An overcomplicated achievement framework and a standalone mod at the same time.

The functionality of this project is split into 3 different modules:

| Subproject: |                                                                                                             Module: |
|:------------|--------------------------------------------------------------------------------------------------------------------:|
| **core**    |                The achievement framework. Must never be used as an `implementation` dependency, on;y `compileOnly`! |
| **gui**     | Contains an achievement tree dialog and various related stuff necessary to display the achievements the player has. |
| **mod-src** |                                      Includes some built-in achievements and creates the hud button/settings stuff. |


# TODO
* Much more achievements (at least 50 serpulo ones are needed before creating a release)
* More objectives and more objective events / notifications
* Create an annotation processor for listeners of `ObjectiveEvent`s / `ObjectiveNotification`s
* Custom icons for achievements
* Add an objective that requires the player to play in a specific environment
