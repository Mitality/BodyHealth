<p align="center">
  <a href="https://github.com/Mitality/BodyHealth/wiki/Code-Quality"><img src="https://www.codefactor.io/repository/github/mitality/bodyhealth/badge" alt="Code Quality"></a>
  <a href="https://github.com/mitality/bodyhealth/releases/latest"><img src="https://img.shields.io/github/v/release/mitality/bodyhealth" alt="Latest Release"></a>
  <a href="https://github.com/Mitality/BodyHealth?tab=License-1-ov-file"><img src="https://img.shields.io/badge/license-custom-%23A60CBF" alt="License"></a>
</p>

<div align="center">
 
| [Spigot Page](https://www.spigotmc.org/resources/bodyhealth.119966/) | [Modrinth Page](https://modrinth.com/plugin/bodyhealth) | [Dev Builds](https://ci.breweryteam.dev/job/Mitality/job/BodyHealth/) |
|---|---|---|

</div>

<p align="center">
  <img width="60%" src="https://raw.githubusercontent.com/Mitality/BodyHealth/refs/heads/main/src/main/resources/assets/BodyHealth_GitHub.png" alt="Image featuring a stylized, color-coded character model highlighting different body parts, an in-game health display, configuration files, scripting options, a raytracing combat scene, and a resource pack, with text emphasizing features like fine-tuned damage, PlaceholderAPI support, persistence through restarts, and full compatibility with all types of damage.">
  <img width="50%" src="https://raw.githubusercontent.com/Mitality/BodyHealth/refs/heads/main/src/main/resources/assets/about.png" alt="About">
</p>

<p align="center"><b>BodyHealth - Health Management Per Body Part!</b></p>

<p align="center">
  BodyHealth introduces a new way to handle player health<br>
  by managing damage and healing per body part.
</p>

<p align="center">
  Built for compatibility, it works well with plugins like<br>
  <a href="https://www.spigotmc.org/resources/realistic-survival.93795/">Realistic Survival</a>, 
  <a href="https://mythiccraft.io/index.php?resources/mythicmobs.1/">MythicMobs</a>, 
  <a href="https://projectkorra.com">ProjectKorra</a><br>
  and their custom damage sources
</p>

<p align="center">
  Official Addon:<br>
  <a href="https://modrinth.com/plugin/bodyhealthaddon-locationalarmoraddon">LocationalArmorAddon</a>
</p>

<p align="center">
  <img width="50%" src="https://raw.githubusercontent.com/Mitality/BodyHealth/refs/heads/main/src/main/resources/assets/features.png" alt="Features">
</p>

<p align="center"><b>Fine-Tuned Damage</b><br>
Customize how different damage types<br>(fall, fire, drowning, etc.) affect specific body parts</p>

<p align="center"><b>In-Game Display</b><br>
Integrates with <a href="https://www.spigotmc.org/resources/placeholderapi.6245/">PlaceholderAPI</a> and 
<a href="https://www.spigotmc.org/resources/BetterHud.115559/">BetterHud</a><br>for real-time body part health displays</p>

<p align="center"><b>Incorporates all factors</b><br>
Fully supports armor, enchantments, potion effects,<br>and even attributes when calculating damage</p>

<p align="center"><b>Custom Effects</b><br>
Script your own effects and specify exactly what<br>should happen under what circumstances</p>

<p align="center"><b>Raytracing</b><br>
Uses raytracing for precision in determining which<br>body part is hit.
For damage that isn't caused by entities,<br>vector calculations are used instead</p>

<p align="center"><b>Comprehensive config</b><br>
You may even set different max health per body part<br>and have it depend on the player’s vanilla max health.<br>
Check it out <a href="https://github.com/Mitality/BodyHealth/blob/main/src/main/resources/config.yml">here</a>, the possibilities are endless!</p>

<p align="center"><b>API</b><br>
Directly hook into BodyHealth with your own<br>plugin to further expand its functionality<br>
(usage guide can be found <a href="https://github.com/Mitality/BodyHealth/wiki/API">here</a>)</p>

<p align="center"><b>Addon System</b><br>
Supports <a href="https://github.com/Mitality/BodyHealth/wiki/Addons">addons</a> like 
<a href="https://modrinth.com/plugin/bodyhealthaddon-locationalarmoraddon">this one</a> that<br>directly integrate with BodyHealth<br>without 3rd party plugins</p>

<p align="center"><b>Plugin Hooks</b><br>
Hooks into plugins like WorldGuard to<br>further enhance your experience</p>

<p align="center"><b>Performance</b><br>
Safe to say this won't be the<br>plugin lagging your server</p>

<p align="center"><b>Top Code Quality</b><br>
<a href="https://github.com/Mitality/BodyHealth/wiki/Code-Quality">
  <img src="https://www.codefactor.io/repository/github/mitality/bodyhealth/badge" alt="CodeFactor" />
</a>
</p>

<p align="center">
  <img width="50%" src="https://raw.githubusercontent.com/Mitality/BodyHealth/refs/heads/main/src/main/resources/assets/commands.png" alt="Commands">
</p>

<p align="center"><b>/bodyhealth reload</b><br><em>reloads the configuration, including effects and language files</em></p>

<p align="center"><b>/bodyhealth heal [player] [body part]</b><br><em>heal a player (sets the health of affected body parts to 100%)</em></p>

<p align="center"><b>/bodyhealth get [player] [body part]</b><br><em>retrieve a player's health (percent) for a specific body part, or all</em></p>

<p align="center"><b>/bodyhealth set [player] [body part] &lt;health&gt;</b><br><em>set a player's health for a body part (or all if not specified)</em></p>

<p align="center"><b>/bodyhealth add [player] [body part] &lt;health&gt;</b><br><em>add health to all parts or the given one for a given player or yourself</em></p>

<p align="center">
  <img width="50%" src="https://raw.githubusercontent.com/Mitality/BodyHealth/refs/heads/main/src/main/resources/assets/permissions.png" alt="Permissions">
</p>

<p align="center"><b>bodyhealth.update-notify</b><br><em>notifications upon new updates</em></p>
<p align="center"><b>bodyhealth.reload</b><br><em>allows reloading the system</em></p>
<p align="center"><b>bodyhealth.heal</b><br><em>allows using the heal command</em></p>
<p align="center"><b>bodyhealth.get</b><br><em>allows using the get command</em></p>
<p align="center"><b>bodyhealth.set</b><br><em>allows using the set command</em></p>
<p align="center"><b>bodyhealth.add</b><br><em>allows using the add command</em></p>
<p align="center"><em>+ <a href="https://github.com/Mitality/BodyHealth/blob/7a0c68af2ef66e8ec5c3b449bbde7d24aa3a2fd5/src/main/resources/config.yml#L35-L42">bypass permissions</a></em></p>

<p align="center">
  <img width="50%" src="https://raw.githubusercontent.com/Mitality/BodyHealth/refs/heads/main/src/main/resources/assets/placeholders.png" alt="Placeholders">
</p>

<p align="center">
  <a href="https://github.com/Mitality/BodyHealth/">BodyHealth</a> works seamlessly with 
  <a href="https://www.spigotmc.org/resources/placeholderapi.6245/">PlaceholderAPI</a><br>for detailed placeholders per body part
</p>

<p align="center"><b>%bodyhealth_health_&lt;BodyPart&gt;%</b><br><em>(returns the current amount of health for a body part in percent)</em></p>
<p align="center"><b>%bodyhealth_health_&lt;BodyPart&gt;_rounded%</b><br><em>(same as the one above, just without decimal places)</em></p>
<p align="center"><b>%bodyhealth_state_&lt;BodyPart&gt;%</b><br><em>(returns the current state of a body part)</em></p>

<p align="center">
  <img width="50%" src="https://raw.githubusercontent.com/Mitality/BodyHealth/refs/heads/main/src/main/resources/assets/setup.png" alt="Setup">
</p>

<p align="center">Please have a look at our <a href="https://github.com/Mitality/BodyHealth/wiki/Setup">Setup Guide</a></p>

<p align="center">Can't update to 1.21 yet?<br>
Check out <a href="https://github.com/Mitality/BodyHealthLegacy/releases/latest">BodyHealthLegacy</a></p>

<p align="center">
  <img width="50%" src="https://raw.githubusercontent.com/Mitality/BodyHealth/refs/heads/main/src/main/resources/assets/support.png" alt="Support">
</p>

<p align="center">
  Ask questions, request new features or report bugs either<br>
  <a href="https://github.com/Mitality/BodyHealth/issues/new/choose">here</a> or on our Discord server (click the icon below to join)
</p>

<p align="center">
  <a href="https://discord.gg/3FkNaNDnta">
    <img src="https://content.moonlitmc.net/discord.png" alt="Discord Logo">
  </a>
</p>
