### Introducing BodyHealth - A system that manages health per body part!

This was meant to be part of a private plugin of mine, but things escalated and here we are.

This plugin is capable of determining where players were hit by using a combination of vector
calculations and raytracing. This should work for all possible ways of being damaged with more
accuracy than mojang has with displaying body rotation and arrows stuck in your body in game.

Everything I could imagine is highly customizable and if there's something important I missed,
you can ask for it to be added and I'll see what I can do! It should work without any changes
to the default configuration, however it isn't meant to be used that way and I highly encourage
you to read through it and adjust all values to your needs, as the default configuration is just
there to show off what's possible.

To display health per body part in game, BodyHealth uses [BetterHud](https://www.spigotmc.org/resources/%E2%AD%90betterhud%E2%AD%90a-beautiful-hud-plugin-you-havent-seen-before%E2%9C%85auto-resource-pack-build%E2%9C%85.115559/) and BetterHud needs
[PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) for conditions. You just need to install them and per default, both of them will be automatically configured to display health per body part to all players
in survival mode at the bottom right corner of their screen. You can disable this behaviour and customize BetterHud and PAPI
 yourself though, if you want to implement your own display.

**If you just see a yellow bossbar with weird rectangle characters above, you need to use "plugins/BetterHud/build" as a resource pack! An icon and mcmeta file for said pack will be automatically generated and added by BodyHealth. If you don't manage to do that, open a [question ticket](https://github.com/Mitality/BodyHealth/issues/new?assignees=&labels=question&projects=&template=question.yml) or say something in [discussions](https://github.com/Mitality/BodyHealth/discussions/categories/general), thak you.**
