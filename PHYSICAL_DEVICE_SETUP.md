# GeoCue Android - Testing on Physical Device

## 📱 Complete Guide to Install on Your Real Android Phone

---

## Method 1: USB Cable Installation (Recommended)

### Step 1: Enable Developer Options on Your Phone

1. **Open Settings** on your Android phone
2. **Scroll down** to **About phone** (or **About device**)
3. **Find "Build number"** (might be under "Software information")
4. **Tap "Build number" 7 times** rapidly
5. You'll see a message: "You are now a developer!"

### Step 2: Enable USB Debugging

1. **Go back** to main Settings
2. Look for **Developer options** (usually near the bottom or in System)
3. **Toggle on** "Developer options" if it's off
4. **Scroll down** and find **"USB debugging"**
5. **Toggle it ON**
6. Confirm the popup if it appears

### Step 3: Connect Your Phone to Mac

1. **Connect your phone** to your Mac using a USB cable
2. **Unlock your phone**
3. **Look for a popup** on your phone:
   - "Allow USB debugging?"
   - Check "Always allow from this computer"
   - Tap **OK** or **Allow**

### Step 4: Verify Connection

Open Terminal and run:
```bash
cd /Users/dmeghwal/Documents/myDocs/repo/GeoCueAnd/GeoCueAnd
adb devices
```

You should see output like:
```
List of devices attached
ABC123DEF456    device
```

If you see "unauthorized", check your phone for the USB debugging prompt.

### Step 5: Install the App

```bash
cd /Users/dmeghwal/Documents/myDocs/repo/GeoCueAnd/GeoCueAnd
./gradlew :app:installDebug
```

Or install the existing APK:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Step 6: Launch the App

The app will appear in your app drawer as "GeoCue" with a debug badge.

**Grant permissions when prompted:**
- ✅ Location (while using app)
- ✅ Location (all the time) - for background geofencing
- ✅ Notifications

---

## Method 2: Wireless Installation (Android 11+)

### Enable Wireless Debugging

1. Enable **Developer Options** and **USB Debugging** (see Method 1)
2. In Developer Options, find **"Wireless debugging"**
3. Toggle it **ON**
4. Connect your phone and Mac to the **same Wi-Fi network**

### Pair Devices (First Time Only)

On your phone:
1. Open **Developer Options** → **Wireless debugging**
2. Tap **"Pair device with pairing code"**
3. Note the **IP address**, **port**, and **pairing code**

On your Mac Terminal:
```bash
adb pair <IP_ADDRESS>:<PORT>
# Example: adb pair 192.168.1.100:45678
# Enter the pairing code when prompted
```

### Connect and Install

```bash
# Connect (use the main port from wireless debugging screen, not pairing port)
adb connect <IP_ADDRESS>:<PORT>
# Example: adb connect 192.168.1.100:5555

# Verify connection
adb devices

# Install app
cd /Users/dmeghwal/Documents/myDocs/repo/GeoCueAnd/GeoCueAnd
./gradlew :app:installDebug
```

---

## Method 3: Transfer APK File Directly

If ADB isn't working, you can manually transfer the APK:

### Step 1: Locate the APK on Your Mac

```bash
cd /Users/dmeghwal/Documents/myDocs/repo/GeoCueAnd/GeoCueAnd
open app/build/outputs/apk/debug/
```

The file is: **`app-debug.apk`**

### Step 2: Transfer to Your Phone

Choose one method:

**A. Email**
- Email the APK to yourself
- Open email on your phone
- Download the attachment

**B. Cloud Storage**
- Upload to Google Drive / Dropbox / iCloud
- Download on your phone

**C. USB Transfer**
- Connect phone via USB
- Choose "File Transfer" mode
- Copy APK to Downloads folder

**D. AirDrop** (if supported)
- Right-click APK on Mac
- Share → AirDrop → Your phone

### Step 3: Install on Phone

1. **Open the APK file** on your phone (from Downloads/Files app)
2. You may see: **"Install unknown apps"** or **"Install blocked"**
3. Tap **"Settings"** → Enable **"Allow from this source"**
4. Go back and tap **Install**
5. Tap **Open** when installation completes

---

## Method 4: Install via Android Studio (If Available)

If you have Android Studio installed:

1. Open Android Studio
2. Open the project: `/Users/dmeghwal/Documents/myDocs/repo/GeoCueAnd/GeoCueAnd`
3. Connect your phone via USB
4. Wait for Gradle sync
5. Click the **green Run button** (▶️)
6. Select your physical device from the dropdown
7. Click OK

---

## 🧪 Testing Location Features on Real Device

### Advantages of Physical Device Testing:

1. **Real GPS** - Actual location data, not simulated
2. **Real Movement** - Walk around to test geofence triggers
3. **Background Testing** - Lock phone and move to test background location
4. **Battery Impact** - See real battery usage
5. **Notification Behavior** - Test real notification experience

### How to Test Geofencing:

1. **Create a reminder** at a location near you (your home, a nearby store, etc.)
2. **Set a radius** (start with 100-200 meters for testing)
3. **Enable the reminder**
4. **Walk towards/away** from the location
5. **You should receive a notification** when entering/exiting the geofence

### Tips for Testing:

- **Use a familiar location** first (like your home address)
- **Start with a larger radius** (200-300m) to make testing easier
- **Check Location is "Always Allow"** in Settings → Apps → GeoCue
- **Keep the app open** initially to see logs, then test with app closed
- **Be patient** - GPS can take 10-30 seconds to get accurate location
- **Move at least 50 meters** away to clearly cross geofence boundaries

---

## 🔧 Troubleshooting

### "Device not found" or "No devices attached"

1. **Check USB cable** - Try a different cable (data cable, not just charging)
2. **Unlock your phone** - ADB requires unlocked screen
3. **Accept USB debugging prompt** on phone
4. **Try different USB port** on Mac
5. **Restart ADB server**:
   ```bash
   adb kill-server
   adb start-server
   adb devices
   ```

### "Install blocked" when installing APK

1. Go to **Settings** → **Security** (or **Apps**)
2. Find **"Install unknown apps"** or **"Unknown sources"**
3. Enable for your **File Manager** or **Downloads** app

### App crashes on physical device

Check logs in real-time:
```bash
adb logcat | grep -i geocue
```

### Location not working

1. **Settings** → **Apps** → **GeoCue** → **Permissions**
2. Set Location to **"Allow all the time"**
3. Enable **"Use precise location"**
4. Make sure GPS is enabled in phone's quick settings

### Geofences not triggering

1. **Check permissions** - Must have "Allow all the time" location
2. **Check battery optimization** - Disable for GeoCue:
   - Settings → Battery → Battery optimization
   - Find GeoCue → Don't optimize
3. **Move significant distance** - At least 50m from geofence boundary
4. **Wait a few minutes** - System may batch location updates

---

## 📊 Quick Command Reference

```bash
# Check connected devices
adb devices

# Install/reinstall app
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Or build and install in one step
./gradlew :app:installDebug

# Launch the app
adb shell am start -n com.geocue.android.debug/com.geocue.android.MainActivity

# View live logs
adb logcat | grep -E "(GeoCue|geocue)"

# Clear app data (fresh start)
adb shell pm clear com.geocue.android.debug

# Uninstall app
adb uninstall com.geocue.android.debug

# Check app permissions
adb shell dumpsys package com.geocue.android.debug | grep -A5 "permission"

# Take screenshot
adb exec-out screencap -p > screenshot.png

# Grant location permission manually
adb shell pm grant com.geocue.android.debug android.permission.ACCESS_FINE_LOCATION
adb shell pm grant com.geocue.android.debug android.permission.ACCESS_BACKGROUND_LOCATION
adb shell pm grant com.geocue.android.debug android.permission.POST_NOTIFICATIONS
```

---

## 🎯 Recommended Testing Flow

1. **Install on phone** (use Method 1 - USB)
2. **Grant all permissions** when prompted
3. **Create test reminder** at your current location
4. **Verify on Map tab** - marker should show correctly
5. **Walk 200m away** - test "on exit" notification
6. **Walk back** - test "on entry" notification
7. **Test with app closed** - lock phone and move around
8. **Check battery usage** - Settings → Battery → Battery usage

---

## 📱 Compatible Devices

The app works on:
- **Android 7.0 (API 24)** and higher
- Devices with **Google Play Services**
- Devices with **GPS capability**

Tested on:
- **Emulator**: API 36 (Android 16) ✅
- **Physical devices**: Pending your test

---

## 💡 Pro Tips

1. **Keep USB debugging enabled** for faster future installs
2. **Use wireless debugging** once paired - no cable needed
3. **Monitor battery** - location services use power
4. **Test in airplane mode** - GPS still works, useful for isolated testing
5. **Use "Use my current location"** feature for quick reminder setup
6. **Start with your home address** - easy to test while staying in one place

---

Need help with installation? Let me know what issue you're facing!

