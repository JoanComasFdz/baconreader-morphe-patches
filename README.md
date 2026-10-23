<div align="center">

# 🥓 BaconReader Patches

**BaconReader, alive again.** Patches for [Morphe](https://morphe.software) that make BaconReader work with today's Reddit.

[![Morphe](https://img.shields.io/badge/Morphe-patches-ff4500?style=flat-square)](https://morphe.software)
[![BaconReader](https://img.shields.io/badge/BaconReader-6.1.4-f57c00?style=flat-square)](#-install)
[![Patches](https://img.shields.io/badge/patches-2-2ea44f?style=flat-square)](#-whats-inside)
[![License](https://img.shields.io/badge/license-GPL--3.0-blue?style=flat-square)](LICENSE)

[**➕ Add to Morphe Manager**](https://morphe.software/add-source?github=JoanComasFdz/baconreader-morphe-patches)

</div>

## ⚡ Install

1. Install [Morphe Manager](https://morphe.software).
2. On your phone, open [**Add to Morphe Manager**](https://morphe.software/add-source?github=JoanComasFdz/baconreader-morphe-patches) and add the source. Or, in Morphe Manager, add `https://github.com/JoanComasFdz/baconreader-morphe-patches` as a remote source.
3. Patch your **BaconReader 6.1.4** APK, free or Premium.
4. Uninstall the original BaconReader, then install the patched one.

> [!WARNING]
> Use an APK you trust. Repackaged "Premium" APKs often carry malware.

## 🧩 What's inside

**🔧 Fixes**: what broke since 2023

| Patch | |
|---|---|
| Spoof client | Log in to Reddit again |
| Fix Redgifs API | Redgifs videos play again |

Spoof client uses RedReader's Reddit client by default, so it needs no setup. Its client ID, redirect URI and user agent can be changed as patch options.

## 📋 Patch details

<!-- PATCHES_START EXPANDED -->
> **[v1.0.0](https://github.com/JoanComasFdz/baconreader-morphe-patches/releases/tag/v1.0.0)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;4 patches total
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

## 🛠️ Development

How to set up, build, test and release: [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md).

## 📜 License

[GPL-3.0](LICENSE). The patches are ported from [ReVanced Patches](https://gitlab.com/ReVanced/revanced-patches). Not affiliated with Reddit, BaconReader or Morphe.

## 🤖 AI disclosure

I developed these patches with AI assistance (Claude Code). The AI wrote most of the code following my requirements, and I test the patches on my device and review the results. The commits say so too.
