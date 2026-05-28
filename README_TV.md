# 📺 ReVanced Manager for Android TV — Developer & Testing Guide

This guide provides step-by-step instructions for building, deploying, and testing **ReVanced Manager** on the **Android TV 9.0 (API Level 28)** environment configured for this repository.

---

## 🛠️ 1. Environment & Path Configurations

To avoid local build environment conflicts, the project is configured with a hybrid SDK/NDK layout. 

### Local SDK/NDK Mapping
Your local **[local.properties](file:///f:/Project/revanced-manager-for-tv/local.properties)** file decouples the SDK path (stored on your high-capacity G: drive) from the valid, complete NDK compilation binaries (stored on your C: drive):
```properties
# Location of the SDK. This is only used by Gradle.
sdk.dir=G:\\a1
ndk.dir=C:\\Users\\tusha\\AppData\\Local\\Android\\Sdk\\ndk\\27.0.12077973
```

---

## 🔑 2. GitHub Packages Dependency Credentials

ReVanced core dependencies (like `patcher-android` and `library-android`) are hosted on GitHub Packages. Authentication is **required** to download these dependencies.

### Configure Global Credentials (Safe & Secure)
Open or create your global Gradle configuration file:
📁 **[c:\Users\tusha\.gradle\gradle.properties](file:///c:/Users/tusha/.gradle/gradle.properties)**

And paste your personal GitHub username and Personal Access Token (PAT):
```properties
githubPackagesUsername=TUSHAR91316
githubPackagesPassword=ghp_your_personal_access_token_here
```
*(Note: Generate a free token with the `read:packages` scope at [GitHub Token Settings](https://github.com/settings/tokens/new?scopes=read:packages&description=ReVanced).)*

---

## 📦 3. Compiling the TV App

Once your credentials are saved, compile and build the TV-optimized debug package using your terminal:

```powershell
# From the root of this project (in PowerShell)
.\gradlew.bat assembleDebug
```

### 📁 Output Target APK
Upon a successful build, your deployable APK will be packaged at:
👉 **[app/build/outputs/apk/debug/revanced-manager-2.6.1.apk](file:///f:/Project/revanced-manager-for-tv/app/build/outputs/apk/debug/revanced-manager-2.6.1.apk)**

---

## 🖥️ 4. Emulating & Testing on Android TV 9.0

We have pre-configured and registered a native **Android TV 9.0 (API 28)** virtual device on your machine to test Leanback launcher layouts and D-Pad focus traversal.

### 🧩 TV Emulator x86 ABI Compatibility Note
Standard TV emulators run on 32-bit `x86` architectures. The project's build file is configured to conditionally package `x86` native libraries **only for Debug builds** (`assembleDebug`). This ensures your release builds remain size-optimized while your local emulator testing installs seamlessly without crashing on `INSTALL_FAILED_NO_MATCHING_ABIS` errors!

### Step 1: Start the Android TV Emulator
Execute this command in your PowerShell terminal to boot up the TV screen:
```powershell
& G:\a1\emulator\emulator.exe -avd Android_TV_9
```

### Step 2: Install Your Compiled TV APK
Install the freshly compiled package onto the running emulator:
```powershell
& G:\a1\platform-tools\adb.exe install -r F:\Project\revanced-manager-for-tv\app\build\outputs\apk\debug\revanced-manager-2.6.1.apk
```

### Step 3: Launch ReVanced Manager on TV
Launch the application directly onto the screen using ADB:
```powershell
& G:\a1\platform-tools\adb.exe shell monkey -p app.revanced.manager.flutter -c android.intent.category.LAUNCHER 1
```

---

## 🕹️ 5. Remote D-Pad Traversal Verification

Android TV uses remote control inputs rather than touchscreens. Test your UI layout focus handles by simulating physical D-Pad remote events via ADB:

| Input Remote Action | ADB Shell Keyevent Command |
|---|---|
| ⬆️ **D-Pad Up** | `& G:\a1\platform-tools\adb.exe shell input keyevent 19` |
| ⬇️ **D-Pad Down** | `& G:\a1\platform-tools\adb.exe shell input keyevent 20` |
| ⬅️ **D-Pad Left** | `& G:\a1\platform-tools\adb.exe shell input keyevent 21` |
| ➡️ **D-Pad Right** | `& G:\a1\platform-tools\adb.exe shell input keyevent 22` |
| 🟢 **D-Pad Center (Select)** | `& G:\a1\platform-tools\adb.exe shell input keyevent 23` |
| 🔙 **Back button** | `& G:\a1\platform-tools\adb.exe shell input keyevent 4` |
