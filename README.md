# Stickman Fighter

2D Android stickman fighting game. No Unity, no game engine — native Android
(`SurfaceView` + Kotlin game loop thread).

## Status: Phase 1 — Project Foundation + Basic Playable Battle Scene

What exists right now:
- A single `Activity` hosting a `SurfaceView` (`GameView`).
- A fixed-timestep game loop on a background thread (`GameLoop`).
- Two stickman figures drawn with canvas primitives (`StickmanFighter`) — one
  controllable, one idle placeholder for the next phase's AI.
- Touch controls: left / right movement buttons + jump button.
- Simple gravity + ground collision.

Not built yet (later phases per the spec): main menu, character data system,
50+ character roster, transformations, real combat/hitboxes, AI, arenas, VFX,
audio, save system, settings, MODRIN.

## Building the APK

This project has **no committed Gradle wrapper jar** (binary file — generate
it once yourself, see below). Two ways to build:

### Option A — GitHub Actions (recommended, no local Android setup needed)
1. Push this repo to a **private** GitHub repository.
2. Actions → the `Build APK` workflow runs automatically on push to `main`
   (or trigger it manually via "Run workflow").
3. Download `stickman-fighter-debug-apk` from the workflow run's Artifacts.

### Option B — Android Studio locally
1. Open the project folder in Android Studio (it will offer to generate the
   Gradle wrapper automatically).
2. Run on an emulator or a device connected via USB debugging.

## Known limitations of this Phase 1 drop
- Not compiled or run in the environment that generated this code — no Android
  SDK/emulator was available there. Build it via one of the two options above
  to get a real pass/fail result, and report back any compiler error exactly
  as shown so it can be fixed before Phase 2 starts.
