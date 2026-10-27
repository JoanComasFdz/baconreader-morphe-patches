// SPDX-FileCopyrightText: 2026 JoanComasFdz
// SPDX-License-Identifier: GPL-3.0-only

using System.Collections.Immutable;
using BaconReader.E2E.Infrastructure.Vocabulary;
using OpenQA.Selenium.Appium.Android;

namespace BaconReader.E2E.Infrastructure.Phone.BaconReader.Screens;

/// <summary>
/// The patches' settings, which "Patches" in BaconReader's settings opens: the extension's one screen, "Patches", with a
/// section for each area ("Images and videos", "Links"…). A section lists its settings, each a title, a summary and a
/// switch; a tap on the title switches it. The views are Android's preferences', with Android's ids: they're found by
/// their texts.
/// </summary>
public sealed class PatchesSettings(AndroidDriver driver)
{
    // The first section: it shows when "Embed comment images" is patched in.
    private static readonly OpenQA.Selenium.By FirstSection = Locate.Text("Images and videos");

    private static OpenQA.Selenium.By Titled(string text) => Locate.Text(text);

    /// <summary>Waits until the screen is shown, its first section listed.</summary>
    public Task WaitUntilShownAsync() =>
        Waiting.UntilShownAsync(driver, FirstSection, TimeSpan.FromSeconds(10), "The patches' settings, \"Patches\"");

    /// <summary>Taps <paramref name="setting"/>'s title, which switches it.</summary>
    public Task TickAsync(PatchSetting setting) => OnScreen.PressAsync(driver, Titled(setting.ToShownText()));

    /// <summary>What the screen shows: the settings it lists, top to bottom.</summary>
    public Task<PatchesSettingsSnapshot> ReadAsync() =>
        OnScreen.ReadAsync(driver, () => new PatchesSettingsSnapshot(
        [
            .. PatchSettingShowing.Listed
                .Select(setting => (setting, title: driver.FindElements(Titled(setting.ToShownText())).FirstOrDefault(title => title.Displayed)))
                .Where(found => found.title is not null)
                .OrderBy(found => found.title!.Location.Y)
                .Select(found => found.setting),
        ]));
}

/// <summary>What the patches' settings show: the settings listed, top to bottom.</summary>
public sealed record PatchesSettingsSnapshot(ImmutableArray<PatchSetting> Settings);
