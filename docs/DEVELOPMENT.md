# Development

See the [Morphe development docs](https://github.com/MorpheApp/morphe-documentation/blob/main/docs/morphe-development/README.md)
and the [patcher docs](https://github.com/MorpheApp/morphe-patcher/blob/main/docs/1_patcher_intro.md).

## Setup

1. Install JDK 21 or newer.
2. Create a GitHub [personal access token (classic)](https://github.com/settings/tokens/new?scopes=read:packages&description=Morphe)
   with the `read:packages` scope. Morphe's Gradle plugin and patcher are on GitHub Packages.
3. Add it to `~/.gradle/gradle.properties`:
   ```properties
   gpr.user = <GitHub username>
   gpr.key = <personal access token>
   ```
   Or set the `GITHUB_ACTOR` and `GITHUB_TOKEN` environment variables.

## Layout

| Path | What |
|---|---|
| `patches/src/main/kotlin/io/github/joancomasfdz/patches/` | Patches (Kotlin). `shared/Constants.kt` declares the supported BaconReader packages and versions |
| `extensions/baconreader/` | Extension code (Java) bundled as `extensions/baconreader.mpe` and called from patches |
| `extensions/shared/` | Bundles the Morphe extensions library as `extensions/shared.mpe`, loaded by `sharedExtensionPatch` |

## Build and patch

- Run `./gradlew buildAndroid`. The patches file is `patches/build/libs/patches-*.mpp`.
- Patch an APK with [Morphe Desktop](https://github.com/MorpheApp/morphe-desktop):
  ```bash
  java -jar morphe-desktop-*-all.jar patch --patches patches/build/libs/patches-*.mpp --out baconreader-patched.apk baconreader.apk
  ```
  Add `--install` to install on a device connected over ADB.

## Testing a branch

Every push to a branch other than `main` runs `dev_build.yml`, which uploads the built `.mpp` and the latest Morphe Desktop jar as workflow artifacts.

Pushes that only change Markdown files, `docs/`, `LICENSE`, `NOTICE` or issue templates skip `dev_build.yml` and `release.yml`.

## Releases

- **Make all changes on the `dev` branch.**
- Use [semantic commit](https://kapeli.com/cheat_sheets/Semantic_Commits.docset/Contents/Resources/Documents/index) messages:
  - `feat: ...` and `fix: ...` create a pre-release from `dev`.
  - `chore: ...` creates no release.
- Pre-releases are available in Morphe Manager when pre-releases are enabled for this patch source.
- For a stable release, merge `dev` into `main` with a merge commit (no squash). `open_pull_request.yml` keeps a `dev` → `main` pull request open.
- Releases are created only by `release.yml`. Do not create releases by hand, do not edit `patches-list.json`,
  `patches-bundle.json` or `CHANGELOG.md`, and do not force-push release commits.

