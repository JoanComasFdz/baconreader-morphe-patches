# 🥓🧩 JoanComasFdz BaconReader Patches

BaconReader patches for use with [Morphe](https://morphe.software).

## ❓ About

Fixes and improvements for [BaconReader](https://play.google.com/store/apps/details?id=com.onelouder.baconreader),
the Reddit client. The patches target BaconReader 6.1.4, both `com.onelouder.baconreader` and
`com.onelouder.baconreader.premium`.

This project is not affiliated with or endorsed by the Morphe project.

### How to use these patches

Click here to add these patches to Morphe: https://morphe.software/add-source?github=JoanComasFdz/baconreader-morphe-patches

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->
> **[v1.0.0-dev.1](https://github.com/JoanComasFdz/baconreader-morphe-patches/releases/tag/v1.0.0-dev.1)**&nbsp;&nbsp;•&nbsp;&nbsp;`dev`&nbsp;&nbsp;•&nbsp;&nbsp;4 patches total
<details open>
<summary>📦 BaconReader&nbsp;&nbsp;•&nbsp;&nbsp;2 patches</summary>
<br>

**🎯 Supported versions:**

| 6.1.4 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Fix Redgifs API](#fix-redgifs-api) | Fixes Redgifs media not loading by using temporary Redgifs API tokens. |  |
| [Spoof client](#spoof-client) | Makes logging in to Reddit work again by using another Reddit app's client ID. | • Application client ID<br>• Application redirect URI<br>• Application user agent |

</details>

<details open>
<summary>📦 BaconReader Premium&nbsp;&nbsp;•&nbsp;&nbsp;2 patches</summary>
<br>

**🎯 Supported versions:**

| 6.1.4 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Fix Redgifs API](#fix-redgifs-api) | Fixes Redgifs media not loading by using temporary Redgifs API tokens. |  |
| [Spoof client](#spoof-client) | Makes logging in to Reddit work again by using another Reddit app's client ID. | • Application client ID<br>• Application redirect URI<br>• Application user agent |

</details>

<!-- PATCHES_END -->

## 🧑‍💻 Development

See the [Morphe development docs](https://github.com/MorpheApp/morphe-documentation/blob/main/docs/morphe-development/README.md)
and the [patcher docs](https://github.com/MorpheApp/morphe-patcher/blob/main/docs/1_patcher_intro.md).

### Setup

1. Install JDK 21 or newer.
2. Create a GitHub [personal access token (classic)](https://github.com/settings/tokens/new?scopes=read:packages&description=Morphe)
   with the `read:packages` scope. Morphe's Gradle plugin and patcher are on GitHub Packages.
3. Add it to `~/.gradle/gradle.properties`:
   ```properties
   gpr.user = <GitHub username>
   gpr.key = <personal access token>
   ```
   Or set the `GITHUB_ACTOR` and `GITHUB_TOKEN` environment variables.

### Layout

| Path | What |
|---|---|
| `patches/src/main/kotlin/io/github/joancomasfdz/patches/` | Patches (Kotlin). `shared/Constants.kt` declares the supported BaconReader packages and versions |
| `extensions/baconreader/` | Extension code (Java) bundled as `extensions/baconreader.mpe` and called from patches |

### Build and patch

- Run `./gradlew buildAndroid`. The patches file is `patches/build/libs/patches-*.mpp`.
- Patch an APK with [Morphe Desktop](https://github.com/MorpheApp/morphe-desktop):
  ```bash
  java -jar morphe-desktop-*-all.jar patch --patches patches/build/libs/patches-*.mpp --out baconreader-patched.apk baconreader.apk
  ```
  Add `--install` to install on a device connected over ADB.

### Releases

- **Make all changes on the `dev` branch.**
- Use [semantic commit](https://kapeli.com/cheat_sheets/Semantic_Commits.docset/Contents/Resources/Documents/index) messages:
  - `feat: ...` and `fix: ...` create a pre-release from `dev`.
  - `chore: ...` creates no release.
- Pre-releases are available in Morphe Manager when pre-releases are enabled for this patch source.
- For a stable release, merge `dev` into `main` with a merge commit (no squash). `open_pull_request.yml` keeps a `dev` → `main` pull request open.
- Releases are created only by `release.yml`. Do not create releases by hand, do not edit `patches-list.json`,
  `patches-bundle.json` or `CHANGELOG.md`, and do not force-push release commits.

## 📜 License

JoanComasFdz BaconReader Patches are licensed under the [GNU General Public License v3.0](LICENSE).
See [NOTICE](NOTICE) for the Morphe name restriction.
