# Plugin Hub submission preparation

Source is published at https://github.com/Rossyboiz/wise-old-man-companion for review. No Plugin Hub approval has been granted; the user-run acceptance checklist remains pending.

## Candidate changes

- Real metadata and release-candidate version; `CompanionSyncPlugin` and `CompanionDevLauncher` replace the draft class names.
- Disabled by default on new installs. Existing display/internal name, enable key and data directory remain unchanged.
- Unused greeting config and legacy OBJ writer removed from active source.
- Java 11 target, standard Hub build and reproducible archives; no added runtime dependencies.
- Root and binary-packaged licenses, privacy disclosure and manual acceptance checklist.

## Review focus

The plugin writes only local-player observations to local files. It makes no network calls and accepts no commands. The intended integration has a separately started bridge forwarding these files to the owner's Pi over HTTP. Disclose that complete data flow. RuneLite's player-information/HTTP restriction needs maintainer assessment; acceptance is not promised.

Review the local mesh export and retained third-party attribution too. No captured models, game assets or personal snapshots are bundled. This exporter is useful with a separate consumer; it is not a standalone AI service.

## Before submitting

1. Complete and record the user-run manual tests.
2. Public author credit and repository owner are Rossyboiz. Source commits use GitHub no-reply identities.
3. The curated source repository is public. Provide reviewable bridge source or a public reference if reviewers request it; exclude credentials and account files.
4. Build against current RuneLite dependencies. Local checks are not a Plugin Hub CI result or approval.
5. Create `plugins/wise-old-man-companion` in a fork of `runelite/plugin-hub`, with real values:

```text
repository=<public HTTPS Git repository URL>
commit=<actual full 40-character commit SHA>
```

6. Submit one PR, follow CI/maintainer feedback and update its commit marker as needed. Do not invent a repository URL, commit or approval.

## Draft PR description

Wise Old Man Companion exports read-only observations about the local player to RuneLite-managed local files for an optional companion dashboard: inventory/equipment, observed bank, skills/XP, quests, GE offer observations, credited loot/counts and the local model. New installations start disabled. There are no network calls, listening server, game inputs or subprocesses in the plugin.

The intended separate bridge reads those files and forwards observations to the owner's Pi. This integration is disclosed for review; maintainer guidance is requested on its acceptability under the HTTP/player-information restriction. The existing development enable key and file location are retained. Attach actual automated results and user acceptance evidence before requesting approval.

## Official references

- https://github.com/runelite/plugin-hub/blob/master/README.md
- https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features
- https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts
- https://secure.runescape.com/m=news/third-party-client-guidelines?oldschool=1
