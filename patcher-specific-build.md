<!-- SPDX-FileCopyrightText: 2026 JoanComasFdz -->
<!-- SPDX-License-Identifier: GPL-3.0-only -->

# Patcher-specific: build (Morphe)

How to build, list and patch with Morphe. The steps they serve are in [AGENTS.md](AGENTS.md). Morphe's own docs: [development](https://github.com/MorpheApp/morphe-documentation/blob/main/docs/morphe-development/README.md), [patcher](https://github.com/MorpheApp/morphe-patcher/blob/main/docs/1_patcher_intro.md).

## Tools

- JDK 21 or newer.
- A GitHub [personal access token (classic)](https://github.com/settings/tokens/new?scopes=read:packages&description=Morphe) with `read:packages`: Morphe's Gradle plugin and patcher are on GitHub Packages. Put it in `~/.gradle/gradle.properties`, never in the repo:

  ```properties
  gpr.user = <GitHub username>
  gpr.key = <token>
  ```

  Or set `GITHUB_ACTOR` and `GITHUB_TOKEN`.
- [Morphe Desktop](https://github.com/MorpheApp/morphe-desktop), the patcher: `morphe-desktop-*-all.jar` from its releases, or from the `morphe-desktop` artifact `dev_build.yml` uploads. In an AndroidDev cloud session, android-workbench's `tools/morphe-desktop.sh` installs it.

## Build

```bash
./gradlew buildAndroid --no-daemon
```

Output: `patches/build/libs/patches-<version>.mpp`. Stamp a test build with `-Pversion=<version>-build-<n>`.

Every push to a branch other than `main` runs `dev_build.yml`, which uploads the `.mpp` and the latest Morphe Desktop jar as artifacts. Pushes that only change Markdown, `docs/`, `LICENSE`, `NOTICE` or issue templates skip it.

## List

```bash
java -jar morphe-desktop.jar list-patches --patches patches/build/libs/patches-*.mpp
```

## Patch

```bash
java -jar morphe-desktop.jar patch --patches patches/build/libs/patches-*.mpp --out patched.apk BaconReader.apk
```

- Add `--install` to install on a device connected over ADB.
- The patch's output lists each applied patch; none may fail.
