Float Back - floating Back button above all apps (Android 5+, tested design target: Android 8 head units)

BUILD (Windows)
1. Install Android Studio. Open this folder (FloatBack) and let Gradle sync.
2. Build > Build Bundle(s) / APK(s) > Build APK(s).
3. Result: app/build/outputs/apk/debug/app-debug.apk  (debug-signed, installable)

INSTALL
- adb install app-debug.apk   (or with your ADB helper app)

ENABLE (if the Accessibility screen exists: just turn on "Float Back" there)
If the head unit has no Accessibility screen, use adb:
  1) adb shell settings get secure enabled_accessibility_services
     -> write the result down (if it is "null", it is empty)
  2) If empty:
     adb shell settings put secure enabled_accessibility_services com.example.floatback/com.example.floatback.FloatBackService
     If NOT empty, append with a colon:
     adb shell settings put secure enabled_accessibility_services <existing>:com.example.floatback/com.example.floatback.FloatBackService
  3) adb shell settings put secure accessibility_enabled 1

REMOVE / UNDO
- Put the value from step 1 back using the same "settings put" command, or uninstall the app:
  adb uninstall com.example.floatback

USE
Tap = Back. Long-press (0.7 s) = Home. Drag = move the button (position is remembered).
Only test while the car is parked.
