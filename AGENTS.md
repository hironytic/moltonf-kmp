# AGENTS.md

This file provides guidance to coding agents when working with code in this repository.

## Commands

- Android app: `./gradlew :androidApp:assembleDebug`
- Desktop app: `./gradlew :desktopApp:run` (or `./gradlew :desktopApp:hotRun --auto` for hot reload)
- iOS app: open `iosApp/` in Xcode and run from there
- Tests:
  - Android: `./gradlew :shared:testAndroidHostTest`
  - Desktop (JVM): `./gradlew :shared:jvmTest`
  - iOS: `./gradlew :shared:iosSimulatorArm64Test`
  - Single test class: append `--tests "com.hironytic.moltonfkmp.story.ArchiveTest"` to a test task
- SQLDelight generates `MoltonfDatabase` from the `.sq` files under `shared/src/commonMain/sqldelight/`; regenerate with `./gradlew :shared:generateCommonMainMoltonfDatabaseInterface` if the IDE hasn't picked up a schema change.

## Architecture

Kotlin Multiplatform (Android / Desktop-JVM / iOS) with Compose Multiplatform for UI. Almost all code lives in `shared/`; `androidApp`, `desktopApp`, and `iosApp` are thin entry points. Koin provides DI (`di/AppModule.kt` + per-platform `AppModule.<platform>.kt`), Navigation Compose provides navigation (`navigation/Route.kt`, wired up in `App.kt`).

The domain is centered on **story archives** for a werewolf-style (人狼) game, called "villages":

- `story/Archive.kt` parses a "Jindolf XmlScheme" XML document into a `Story` (`story/Story.kt`), a tree of `Period`s each holding a list of `StoryElement`s (`story/StoryElement.kt`) — talks, deaths, votes, role reveals, etc. This format and its element/attribute vocabulary are external and not documented in this repo; when in doubt about what a field means, ask rather than guessing from the parser code alone.
- A `Story` is immutable and persisted once, as a JSON blob (`storage/Story.sq` — `storyRecord.json`). A `Workspace` (`workspace/Workspace.kt`, `storage/Workspace.sq`) is the mutable, user-facing wrapper around a story: it tracks which story it points to, the viewing character, and how far the user has read. `storage/WorkspaceStore.kt` is the single access point for both tables via SQLDelight; there is no schema migration mechanism yet (`DatabaseDriverFactory` only calls `Schema.create` on first run), so schema/serialization changes are break-and-recreate for now.
- The "Watching" feature (`ui/watching/`) is the main reading UI: `WatchingViewModel` loads a `Story`/`Workspace` and derives per-day, per-viewpoint-character visible elements (`WatchingElement.kt`) — this is where spoiler-prevention (hiding talks/events the current viewpoint character shouldn't know yet, per `dayProgress`) is implemented. `MessageSegment.kt` turns `>>123` talk-number and `12:34`/`3d12:34` time mentions inside message text into tappable links; `TalkThread.kt` models the expandable link-following thread opened from those taps.
- `story/CharacterMap.kt` and `story/TalkMap.kt` are derived indexes built once per loaded `Story` (avatar → role/alive-until; talk-number/time → `Talk`), used throughout the watching UI instead of scanning `Story.periods` repeatedly.

UI text is Japanese, written directly in Kotlin (no string resources). `ui/theme/` (`Palette`, `MoltonfColors`, `Theme.kt`, `Components.kt`) defines a dark, Tailwind-gray-based color scheme with red as the accent color — treat these as the source of truth for styling rather than introducing ad hoc colors.
