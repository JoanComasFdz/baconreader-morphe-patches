// SPDX-FileCopyrightText: 2026 JoanComasFdz
// SPDX-License-Identifier: GPL-3.0-only

using System.Security.Cryptography;
using System.Text;
using System.Text.Json;
using BaconReader.E2E.Infrastructure.Vocabulary;

namespace BaconReader.E2E.Infrastructure.Device;

/// <summary>
/// Patches the clean BaconReader APK with a scenario's patches and installs it on the emulator, as cheaply as a slow
/// link to it allows (a cloud session's reaches it at ~3–4 Mbit/s):
/// - each set of patches is patched once per bundle (<c>build/e2e/patched/&lt;hash&gt;.apk</c>; Morphe Desktop takes about six seconds);
/// - every APK is signed with the one key Morphe Desktop keeps next to its jar (<c>morphe.keystore</c>), so one installs over
///   another; an install signed with another key (the owner's, another session's) is uninstalled first;
/// - an APK already installed (the device's copy has the same SHA-256) isn't sent again: sending one takes ~40 s.
/// </summary>
public static class PatchedApps
{
    private const string DeviceApk = "/data/local/tmp/e2e/app.apk";
    private static readonly SemaphoreSlim OneAtATime = new(1, 1);

    private static string MorpheDesktop =>
        Environment.GetEnvironmentVariable("E2E_MORPHE_DESKTOP") is { Length: > 0 } jar ? jar : "/opt/patch-tools/morphe-desktop.jar";

    /// <summary>Patches the clean APK with <paramref name="patches"/> (once per bundle) and returns the patched APK.</summary>
    public static async Task<string> PatchAsync(IReadOnlyList<Patch> patches)
    {
        await OneAtATime.WaitAsync();
        try
        {
            CheckExists(E2EDevice.Bundle, "the bundle: build it with ./gradlew buildAndroid (README)");
            CheckExists(MorpheDesktop, "Morphe Desktop: tools/morphe-desktop.sh installs it (E2E_MORPHE_DESKTOP names another jar)");
            CheckExists(E2EDevice.BaseApk, "the clean BaconReader APK: the emulator's connection script downloads it (README, \"The emulator\")");
            var names = patches.Select(patch => patch.ToShownText()).Order(StringComparer.Ordinal).ToArray();
            var key = Hash($"{HashFile(E2EDevice.Bundle)}\n{HashFile(E2EDevice.BaseApk)}\n{string.Join('\n', names)}")[..16];
            var patched = Path.Combine(E2EDevice.WorkFolder, "patched", key + ".apk");
            if (File.Exists(patched))
            {
                return patched;
            }

            Directory.CreateDirectory(Path.GetDirectoryName(patched)!);
            var writing = Path.Combine(Path.GetDirectoryName(patched)!, key + "-writing.apk");
            var report = Path.ChangeExtension(writing, ".json");
            var arguments = new List<string>
            {
                "-jar", MorpheDesktop, "patch",
                "--patches", E2EDevice.Bundle,
                "--exclusive",
                "--result-file", report,
                "--out", writing,
            };
            foreach (var name in names)
            {
                arguments.AddRange(["--enable", name]);
            }

            arguments.Add(E2EDevice.BaseApk);
            var ran = await Commands.RunAsync("java", [.. arguments]);
            var log = ran.Output + ran.Error;
            if (!ran.Succeeded || !File.Exists(report) || !AppliedEvery(File.ReadAllText(report), names))
            {
                throw new InvalidOperationException($"Morphe Desktop couldn't apply every patch of {string.Join(", ", names)}:\n{log}");
            }

            File.Delete(report);
            File.Move(writing, patched, overwrite: true);
            return patched;
        }
        finally
        {
            OneAtATime.Release();
        }
    }

    /// <summary>Installs <paramref name="apk"/> on the emulator, with its runtime permissions granted, unless it's already the one installed.</summary>
    public static async Task InstallAsync(string apk)
    {
        var sha = HashFile(apk);
        if (await ReadInstalledShaAsync() == sha)
        {
            return;
        }

        await Commands.ShellAsync("mkdir -p /data/local/tmp/e2e");
        await Commands.AdbAsync("push", apk, DeviceApk);
        var installed = await Commands.ShellAsync($"pm install -r -g {DeviceApk} 2>&1 || true");
        if (installed.Contains("INSTALL_FAILED_UPDATE_INCOMPATIBLE", StringComparison.Ordinal))
        {
            await Commands.ShellAsync($"pm uninstall {E2EDevice.Package}");
            installed = await Commands.ShellAsync($"pm install -g {DeviceApk} 2>&1 || true");
        }

        await Commands.ShellAsync($"rm -f {DeviceApk}");
        if (!installed.Contains("Success", StringComparison.Ordinal))
        {
            throw new InvalidOperationException($"Installing {Path.GetFileName(apk)} failed: {installed}");
        }
    }

    /// <summary>
    /// Leaves the installed app as if just installed: stopped, its data cleared (logins, settings, what it cached) and
    /// its runtime permissions granted again (clearing its data revokes them, and Android 14 would ask for notifications).
    /// </summary>
    public static async Task ResetAsync()
    {
        await Commands.ShellAsync($"am force-stop {E2EDevice.Package}");
        await Commands.ShellAsync($"pm clear {E2EDevice.Package}");
        await Commands.ShellAsync($"pm grant {E2EDevice.Package} android.permission.POST_NOTIFICATIONS");
    }

    private static async Task<string?> ReadInstalledShaAsync()
    {
        var path = await Commands.ShellAsync($"pm path {E2EDevice.Package} | grep base.apk | sed 's/^package://' || true");
        if (path.Length == 0)
        {
            return null;
        }

        var sum = await Commands.ShellAsync($"sha256sum {path}");
        return sum.Split(' ', 2)[0];
    }

    // Morphe Desktop's report of the run: it succeeded, none failed, and every patch asked for was applied (it also applies what they depend on).
    private static bool AppliedEvery(string report, IReadOnlyList<string> names)
    {
        using var json = JsonDocument.Parse(report);
        var root = json.RootElement;
        var applied = root.GetProperty("appliedPatches").EnumerateArray().Select(patch => patch.GetString()).ToHashSet();
        return root.GetProperty("success").GetBoolean()
            && root.GetProperty("failedPatches").GetArrayLength() == 0
            && names.All(applied.Contains);
    }

    private static void CheckExists(string path, string what)
    {
        if (!File.Exists(path))
        {
            throw new FileNotFoundException($"{path} is missing: {what}.", path);
        }
    }

    private static string HashFile(string path)
    {
        using var stream = File.OpenRead(path);
        return Convert.ToHexStringLower(SHA256.HashData(stream));
    }

    private static string Hash(string text) => Convert.ToHexStringLower(SHA256.HashData(Encoding.UTF8.GetBytes(text)));
}
