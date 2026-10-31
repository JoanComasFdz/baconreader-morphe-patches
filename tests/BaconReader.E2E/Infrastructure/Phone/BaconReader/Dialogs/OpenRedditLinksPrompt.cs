// SPDX-FileCopyrightText: 2026 JoanComasFdz
// SPDX-License-Identifier: GPL-3.0-only

using OpenQA.Selenium.Appium.Android;

namespace BaconReader.E2E.Infrastructure.Phone.BaconReader.Dialogs;

/// <summary>
/// "Open Reddit links with BaconReader?", the dialog the "Prompt to open Reddit links" patch shows once: "SET UP" opens
/// Android's "Open by default" settings, "NOT NOW" closes it. Its buttons show in capitals: the patch shows Android's
/// AlertDialog, in BaconReader's theme (Reseam's dialog shows "Set up" and "Not now").
/// </summary>
public sealed class OpenRedditLinksPrompt(AndroidDriver driver)
{
    private static readonly OpenQA.Selenium.By Title = Locate.Text("Open Reddit links with BaconReader?");
    private static readonly OpenQA.Selenium.By SetUp = Locate.Text("SET UP");
    private static readonly OpenQA.Selenium.By NotNow = Locate.Text("NOT NOW");

    /// <summary>Waits until the dialog is shown.</summary>
    public Task WaitUntilShownAsync() =>
        Waiting.UntilShownAsync(driver, Title, TimeSpan.FromSeconds(10), "The \"Open Reddit links with BaconReader?\" dialog");

    /// <summary>Taps "SET UP": Android's "Open by default" settings for BaconReader open.</summary>
    public Task ClickSetUpAsync() => OnScreen.PressAsync(driver, SetUp);

    /// <summary>Taps "NOT NOW", which closes the dialog.</summary>
    public Task ClickNotNowAsync() => OnScreen.PressAsync(driver, NotNow);
}
