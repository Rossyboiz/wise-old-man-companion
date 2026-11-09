# Privacy and data flow

```text
RuneLite events / own local player
  -> client-thread copy of observations
  -> coalescing background file writer
  -> RuneLite-managed plugin directory
```

The plugin accepts no commands and performs no network, credential, subprocess or input-injection operations.

`account-items.json` contains a stable derived account key, revision/timestamps, inventory/worn/bank IDs/names/quantities, bank observation time, GE offer observations, skills/XP, quest states and goal activity. `snapshot.json` contains local model geometry/colours, equipment, selected combat metadata and timestamps. The raw account hash is used to derive the key and is not written as a separate field. This is pseudonymous, not an anonymity guarantee.

Game-message handling extracts recognised kill/completion counts and verified blowpipe data; it does not retain complete messages or public/private chat logs. Other players' profiles, passwords and login tokens are not exported.

## Separate bridge and dashboard

The owner's independently started bridge reads the JSON, prepares the model and forwards observations over authenticated HTTP to a configured Pi. The dashboard retains last-seen observations and account-scoped goals. Authentication tokens and SSH keys are outside this plugin and must never enter the public source package.

This external code and network behavior must be disclosed to reviewers. RuneLite's current rejection list includes plugins exposing player information over HTTP. Maintainer judgment is needed for this local-export integration; writing local files is not blanket approval of the surrounding system, and no review boundary is being bypassed.

The current account-advice comparison runs on the Pi; the complete snapshot is not sent to the separate Wiki/model helper. This does not constrain arbitrary third-party software reading local files.

## Retention and isolation

- Explicit installation enables local exports; disable the plugin to stop future exports. Existing development enable settings and data-directory paths are retained.
- Disabling stops new captures and requests worker shutdown; an already-started write may finish. Existing JSON remains.
- Logging out does not erase files. The receiver must display last-seen times and handle stale data.
- Account switches reset bank/quest/goal baselines in memory. The new account's bank must be observed.
- Only one Companion-enabled client should write to a given data directory. Multi-client arbitration is not implemented.
- Uninstalling the plugin is not a request to delete receiver records. Removing local snapshots or dashboard records is a separate user action.

Never publish snapshots, captured models, credentials or private bridge configuration with source code.
