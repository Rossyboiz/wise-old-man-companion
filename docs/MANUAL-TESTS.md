# User-run acceptance checklist

Only the user performs login and game inputs. Automated checks cannot establish in-game behavior.

1. Launch with `gradlew run`; use RuneLite's official Jagex Account sidebar login and select the Ironman. Do not copy authentication tokens into scripts.
2. In a fresh plugin configuration, verify it starts disabled and produces no new files. Enable it; verify new observation times reach the dashboard through the separate bridge.
3. Manually change equipment. Confirm the model and gear correspond to the same revision.
4. Gain XP manually. Check skill/goal updates and that login XP was not counted as a training gain.
5. Open the bank. Check stacks, noted items and an untradeable item; then close it and verify the bank's last-seen timestamp stays explicit.
6. With Loot Tracker enabled, manually kill a supported monster and receive loot. Verify credited counts update once and nearby deaths are not treated as yours.
7. Manually inspect a partial and completed/cancelled GE offer when available. A cleared slot must not falsely imply bought/sold.
8. Verify a genuine quest-state change when practical. Hidden stages remain unknown; do not alter game variables for testing.
9. Log out and switch accounts. Verify the first account's bank/goal activity is not credited to the second. Open the second bank to establish its own snapshot.
10. Disable the plugin. After any in-progress write finishes, verify no new periodic exports. Old files remain last-seen.
11. Re-enable and check a fresh capture; close RuneLite and confirm shutdown does not hang.

Use only one Companion-enabled client per data directory. Preserve existing configuration; do not wipe settings to simulate a fresh profile.

Record RuneLite version, date, checks performed and failures. User confirmation remains outstanding until this checklist has been exercised.
