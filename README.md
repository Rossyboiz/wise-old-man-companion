# Wise Old Man Companion

An opt-in, read-only RuneLite plugin that exports observations about **your own character** to local JSON files for a separate companion dashboard.

This is a release candidate published for review, not an approved Plugin Hub release. It is not affiliated with Jagex or RuneLite.

## Features and consent

The plugin exports inventory/equipment, last-observed bank contents, skill levels/XP, observed quest states, GE offer observations, credited loot/counts and your local character model/combat setup. Installing **Wise Old Man Companion** from the Plugin Hub enables these local exports. You can disable it in RuneLite's plugin list at any time. Existing development users retain their previous enabled setting.

It does not control the game, click, attack, trade, change prayers or send chat. It does not make network requests, run an HTTP server, launch programs or accept dashboard commands.

The plugin writes only within its RuneLite-managed directory, normally:

```text
~/.runelite/plugin-data/wise-old-man-companion/
    snapshot.json
    account-items.json
```

Observations are copied on the client thread; a coalescing background worker replaces files atomically. An approximately 30-game-tick account heartbeat supplements event-triggered updates. There is no entire-scene scan. Disabling requests worker shutdown and stops new captures; an in-progress write may finish and existing files remain.

The stable, derived account key and exported bank/gear information are personal data. Passwords, Jagex login credentials, other players' profiles and complete chat logs are not exported. See [Privacy and data flow](docs/PRIVACY.md).

## Companion integration

A separately started bridge can read the files and forward them over authenticated HTTP to the owner's configured Pi dashboard. **That external bridge and dashboard are not part of this plugin or any Plugin Hub approval.** The plugin does not start them. Their data flow is disclosed for reviewer assessment, not assumed to be accepted.

Run only one Companion-enabled client against a given RuneLite directory. Otherwise the latest writer wins. Account switches reset in-memory bank/quest/activity observations; open the new account's bank to establish its own snapshot. Cached or missing data is not current game state.

## Development and build

Use the supplied Gradle wrapper. The locally tested JDK is 21; Java compilation targets release 11. No runtime dependencies are added beyond RuneLite's graph. The wrapper distribution has a pinned SHA-256.

```powershell
.\gradlew.bat clean test jar --console=plain
.\gradlew.bat run --console=plain
```

Log in yourself using RuneLite's [official Jagex Account sidebar flow](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts), select the intended character, enable the plugin and open the bank. The development client plays the live game; it is not restricted to a test account.

`build/libs/` contains the slim plugin JAR. The template's optional `shadowJar` task creates a fat development launcher; **do not submit that JAR to the Hub**. Plugin Hub builds reviewed source from a public Git commit using `build=standard`.

## Limitations and verification

- Bank data is unknown until observed and may later be cached. Logout does not erase old files.
- Loot/count tracking depends on RuneLite's credited events and game messages; it is not a full historical record. Loot Tracker must be enabled for its events.
- Hidden quest flags, exact journal stages, diary tasks and combat achievement task progress are not complete datasets here.
- Blowpipe details require a player-triggered check. The plugin never clicks Check.
- RuneLite updates can require API changes and rebuilding.

Unit/build success is not an in-game test. Complete the [manual acceptance checklist](docs/MANUAL-TESTS.md) before submission.

## License and submission

Project code is BSD-2-Clause with retained [third-party notices](THIRD-PARTY-NOTICES.md). RuneScape assets and names remain subject to their owners' rights. No captured meshes, account files or credentials belong in this repository.

See [submission preparation](docs/SUBMISSION.md) for review scope and outstanding steps. Plugin Hub approval has not been granted.
