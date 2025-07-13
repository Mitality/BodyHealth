[![codefactor](https://www.codefactor.io/repository/github/mitality/bodyhealth/badge)](https://github.com/Mitality/BodyHealth/wiki/Code-Quality) [![release](https://img.shields.io/github/v/release/mitality/bodyhealth)](https://github.com/mitality/bodyhealth/releases/latest) [![license](https://img.shields.io/badge/license-custom-%23A60CBF)](https://github.com/Mitality/BodyHealth?tab=License-1-ov-file)

### Introducing BodyHealth - A system that manages health per body part!

### [[Spigot Page](https://www.spigotmc.org/resources/bodyhealth.119966/)] [[Modrinth Page](https://modrinth.com/plugin/bodyhealth)] [[Config](https://github.com/Mitality/BodyHealth/blob/main/src/main/resources/config.yml)]  [[Setup](https://github.com/Mitality/BodyHealth/wiki/Setup)]

This was meant to be part of a private plugin of mine, but things escalated and here we are.

This plugin is capable of determining where players were hit by using a combination of vector
calculations and raytracing. This should work for all possible ways of being damaged with more
accuracy than mojang has with displaying body rotation and arrows stuck in your body in game.
It also supports attributes and potion effects for both the player and the damage source!

Everything I could imagine is highly customizable and if there's something important I missed,
you can ask for it to be added and I'll see what I can do! It should work without any changes
to the default configuration, however it isn't meant to be used that way and I highly encourage
you to read through it and adjust all values to your needs, as the default configuration is just
there to show off what's possible.

To display health per body part in game, BodyHealth uses [BetterHud](https://www.spigotmc.org/resources/%E2%AD%90betterhud%E2%AD%90a-beautiful-hud-plugin-you-havent-seen-before%E2%9C%85auto-resource-pack-build%E2%9C%85.115559/) and BetterHud needs
[PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) for conditions. You just need to install them and per default, both of them will be automatically configured to display health per body part to all players
in survival mode at the bottom right corner of their screen. You can disable this behaviour and customize BetterHud and PAPI
 yourself though, if you want to implement your own display.

**For the best experience, use this plugin together with [Realistic Survival](https://www.spigotmc.org/resources/realistic-survival.93795/) from Val_Mobile.**

**If you just see a yellow bossbar with weird rectangle characters above, you need to use "plugins/BetterHud/build" as a resource pack! An icon and mcmeta file for said pack will be automatically generated and added by BodyHealth. If you don't manage to do that, open a [question ticket](https://github.com/Mitality/BodyHealth/issues/new?assignees=&labels=question&projects=&template=question.yml) or say something in [discussions](https://github.com/Mitality/BodyHealth/discussions/categories/general), thak you.**
