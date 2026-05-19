# Pi Secure Eye (Kotlin + Java)

An Android application for accessing MotionEye camera feeds, recordings, and footage verification via a Raspberry Pi server with self-signed SSL certificates. Built using **Kotlin and Java** to demonstrate modern Android development with interoperability.

## 🚀 Features

- **Camera Access**: Direct access to MotionEye live camera feed
- **Footage Access**: Browse and view recorded footage
- **🔍 Video Footage Verification**: Dedicated verification page at `/verify/`
- **🔐 Hash Generator**: Generate SHA-256 hash of video files for integrity verification
- **📥 Storage Access**: Download and verify footage with file hash checking
- **⚙️ Settings Screen**: Configure server URLs without editing code
- **Self-Signed SSL Support**: Automatically trusts self-signed certificates from rpi.local
- **HTTP Authentication**: Built-in login dialog for MotionEye credentials
- **In-App WebView**: No external browser redirection
- **Kotlin + Java**: Modern architecture with language interoperability
- **Works on both phones and tablets**

## 🛠️ Technology Stack

### Languages
- **Kotlin** - Main UI logic (Activities)
- **Java** - Utility classes (SSL Certificate Handler)
- Demonstrates seamless Kotlin-Java interoperability

### Architecture
```
PiSecureEye/
├── MainActivity.kt              # Kotlin - Button interface (3 buttons + Settings)
├── WebViewActivity.kt           # Kotlin - WebView with SSL handling
├── SettingsActivity.kt          # Kotlin - URL configuration screen
├── PreferencesManager.kt        # Kotlin - Settings storage (3 URLs)
└── utils/
    ├── SSLCertificateHandler.java   # Java - SSL utilities
    └── NetworkManager.kt            # Kotlin - Network utilities (calls Java)
```

### Key Components
- **Kotlin Activities**: Modern, concise UI code with null safety
- **Java Utilities**: Robust SSL certificate handling
- **AndroidX**: Latest Android support libraries
- **Material Design**: Modern UI components
- **WebView**: Enhanced with WebKit library

## 📋 Requirements

- Android 5.0 (API 21) or higher
- Android Studio Hedgehog or later
- Kotlin plugin (included in modern Android Studio)
- MotionEye server running on Raspberry Pi at `https://rpi.local`

## 🔧 Setup Instructions

### 1. Import Project in Android Studio

1. Open Android Studio
2. Click **"Open"** or **"Open an Existing Project"**
3. Navigate to the `MotionEyeApp` folder
4. Wait for Gradle sync to complete
5. Let Android Studio download dependencies

### 2. Configure Server URL (Optional)

If your Raspberry Pi has a different hostname or IP address, edit `MainActivity.kt`:

```kotlin
companion object {
    const val CAMERA_URL = "https://YOUR_RPI_ADDRESS/"
    const val FOOTAGE_URL = "https://YOUR_RPI_ADDRESS/recordings"
}
```

Replace `YOUR_RPI_ADDRESS` with:
- `192.168.1.100` (IP address)
- `rpi.local` (hostname)
- Or your custom domain

### 3. Build and Run

#### For Physical Device:
1. Enable **Developer Options** on your Android device
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
2. Enable **USB Debugging** in Developer Options
3. Connect device via USB
4. Click **"Run"** ▶️ in Android Studio
5. Select your device

#### For Emulator:
1. Create an Android Virtual Device (AVD)
   - Tools → Device Manager → Create Device
2. Click **"Run"** ▶️ in Android Studio
3. Select the emulator

### 4. Test the App

1. Launch the app
2. **Four main buttons available:**
   - **Camera Access** - Live camera feed
   - **Footage Access** - Recorded videos
   - **Video Footage Verification** - Verification page
   - **Hash Generator** - Generate SHA-256 hash of video files
3. Click **Settings** (gear icon in top right or bottom button) to configure URLs
4. Enter your custom URLs in Settings
5. Test each button to ensure they open the correct pages

**Using Hash Generator:**
1. Click **"Hash Generator"** button
2. Grant storage permission when prompted
3. Click **"Select Video File"**
4. Choose a video file from your device
5. Wait for hash calculation (large files take longer)
6. Click **"Copy Hash to Clipboard"** to copy the hash
7. Use this hash to verify file integrity

**Default URLs:**
- Camera: `https://rpi.local/`
- Footage: `https://rpi.local/recordings`
- Verification: `https://rpi.local/verify/`

## 🔐 Security Features

### SSL Certificate Handling

The app automatically trusts self-signed certificates through multiple layers:

#### 1. Network Security Configuration (`network_security_config.xml`)
```xml
<domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="true">rpi.local</domain>
    <trust-anchors>
        <certificates src="system" />
        <certificates src="user" />
    </trust-anchors>
</domain-config>
```

#### 2. WebView SSL Error Handler (Kotlin)
```kotlin
override fun onReceivedSslError(
    view: WebView?,
    handler: SslErrorHandler?,
    error: SslError?
) {
    if (currentUrl?.contains("rpi.local") == true) {
        handler?.proceed() // Auto-accept for rpi.local
    }
}
```

#### 3. Java SSL Utilities
The `SSLCertificateHandler.java` class provides:
- Trust managers for all certificates
- Hostname verification
- Local host detection

### HTTP Authentication

- Built-in authentication dialog (Kotlin)
- Credentials sent securely to server
- No credentials stored in the app
- Auto-dismisses on successful login

## 🎨 Customization

### Change Server URLs (Easy Way - No Code Editing!)
1. Open the app
2. Click **"Settings"** button
3. Enter your custom URLs
4. Click **"Save"**

### Change Server URLs (Alternative - Edit Code)
If you prefer to set default URLs in code, edit `PreferencesManager.kt`:
```kotlin
companion object {
    const val DEFAULT_CAMERA_URL = "https://your-server.com/"
    const val DEFAULT_FOOTAGE_URL = "https://your-server.com/recordings"
}
```

### Add More Trusted Domains
Edit `network_security_config.xml`:
```xml
<domain includeSubdomains="true">192.168.1.x</domain>
<domain includeSubdomains="true">your-domain.com</domain>
```

## 🔍 Kotlin + Java Interoperability

This project demonstrates how Kotlin and Java work together:

### Kotlin Calling Java
```kotlin
// NetworkManager.kt (Kotlin)
fun isLocalUrl(url: String): Boolean {
    val hostname = extractHostname(url)
    // Calling Java static method from Kotlin
    return SSLCertificateHandler.isLocalHost(hostname)
}
```

### Java Utility Class
```java
// SSLCertificateHandler.java
public static boolean isLocalHost(String hostname) {
    return hostname.equals("localhost") || 
           hostname.equals("rpi.local");
}
```

### Benefits
- **Kotlin**: Concise, null-safe, modern syntax for UI
- **Java**: Proven, stable code for critical utilities
- **Seamless**: No barriers between languages
- **Gradual Migration**: Can convert Java to Kotlin over time

## 🐛 Troubleshooting

### SSL Certificate Errors
**Problem**: "Your connection is not private" or SSL warnings

**Solutions**:
1. Verify URL in `MainActivity.kt` matches your Raspberry Pi
2. Check `network_security_config.xml` includes your domain/IP
3. Ensure Raspberry Pi has SSL certificate installed
4. Try using IP address instead of hostname

### Cannot Connect to Server
**Problem**: App shows "Error loading page"

**Solutions**:
1. Ensure Android device is on same network as Raspberry Pi
2. Test connection: `ping rpi.local` from computer
3. Verify MotionEye is running: open URL in browser
4. Check firewall settings on Raspberry Pi
5. Try direct IP address instead of `rpi.local`

### Authentication Dialog Not Appearing
**Problem**: No login prompt when accessing MotionEye

**Solutions**:
1. Clear app data:
   - Settings → Apps → MotionEyeApp → Storage → Clear Data
2. Restart the app
3. Verify MotionEye has authentication enabled
4. Check MotionEye configuration file

### App Crashes on Launch
**Problem**: App immediately closes after opening

**Solutions**:
1. Check Android Studio Logcat for errors
2. Verify minimum SDK (API 21+)
3. Clean and rebuild:
   - Build → Clean Project
   - Build → Rebuild Project
4. Invalidate caches:
   - File → Invalidate Caches / Restart

### Gradle Sync Failed
**Problem**: "Gradle sync failed" error

**Solutions**:
1. Check internet connection
2. Update Gradle wrapper:
   - File → Project Structure → Project → Gradle Version
3. Sync manually: File → Sync Project with Gradle Files
4. Check `build.gradle` for dependency errors

### WebView Shows Blank Page
**Problem**: WebView opens but shows nothing

**Solutions**:
1. Enable JavaScript in `WebViewActivity.kt`
2. Check network connectivity
3. Verify URL is accessible in browser
4. Look for errors in Logcat

## 📁 Project Structure

```
MotionEyeApp/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/motioneyeapp/
│   │       │   ├── MainActivity.kt                    # Kotlin - Main screen
│   │       │   ├── WebViewActivity.kt                 # Kotlin - WebView
│   │       │   └── utils/
│   │       │       ├── SSLCertificateHandler.java     # Java - SSL utils
│   │       │       └── NetworkManager.kt              # Kotlin - Network utils
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   ├── activity_main.xml              # Main layout
│   │       │   │   └── activity_webview.xml           # WebView layout
│   │       │   ├── drawable/
│   │       │   │   ├── button_camera.xml              # Camera button style
│   │       │   │   ├── button_footage.xml             # Footage button style
│   │       │   │   ├── ic_camera.xml                  # Camera icon
│   │       │   │   ├── ic_videocam.xml                # Video icon
│   │       │   │   └── ic_folder.xml                  # Folder icon
│   │       │   ├── values/
│   │       │   │   ├── strings.xml                    # App strings
│   │       │   │   ├── colors.xml                     # Colors
│   │       │   │   └── themes.xml                     # Themes
│   │       │   └── xml/
│   │       │       └── network_security_config.xml    # SSL config
│   │       └── AndroidManifest.xml                    # App manifest
│   ├── build.gradle                                   # App build config
│   └── proguard-rules.pro                             # ProGuard rules
├── build.gradle                                       # Project build config
├── settings.gradle                                    # Project settings
├── gradle.properties                                  # Gradle properties
└── README.md                                          # This file
```

## 🌐 Network Configuration

The app is pre-configured to work with:
- `rpi.local` (default)
- `192.168.1.x` (common home networks)
- `192.168.0.x` (common home networks)
- `10.0.0.x` (common private networks)
- `localhost` and `127.0.0.1`

Add more IP ranges in `network_security_config.xml` as needed.

## 🔑 Permissions

The app requires:
- `INTERNET` - Access MotionEye server
- `ACCESS_NETWORK_STATE` - Check network connectivity
- `ACCESS_WIFI_STATE` - Determine WiFi status
- `READ_EXTERNAL_STORAGE` - Access video files for hash generation (Android 6-12)
- `READ_MEDIA_VIDEO` - Access video files (Android 13+)
- `WRITE_EXTERNAL_STORAGE` - Download footage (Android 6-9)

**No permissions needed for:**
- ❌ Location
- ❌ Camera
- ❌ Contacts

## 📦 Dependencies

```gradle
// Kotlin
implementation 'org.jetbrains.kotlin:kotlin-stdlib:1.9.21'
implementation 'androidx.core:core-ktx:1.12.0'

// UI
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'com.google.android.material:material:1.11.0'

// WebView
implementation 'androidx.webkit:webkit:1.10.0'

// Coroutines (for future use)
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
```

## 🚀 Building APK

### Debug APK
```bash
./gradlew assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

### Release APK
```bash
./gradlew assembleRelease
```
Output: `app/build/outputs/apk/release/app-release-unsigned.apk`

## 📝 Future Enhancements

Potential features to add:
- [ ] Multiple camera support
- [ ] Push notifications for motion detection
- [ ] Video playback controls
- [ ] Download recordings locally
- [ ] Dark mode support
- [ ] Camera PTZ controls
- [ ] Snapshot capture
- [ ] Settings screen

## 📄 License

This project is open source and available for modification and distribution.

## 💬 Support

For issues with:
- **MotionEye**: Check [MotionEye documentation](https://github.com/motioneye-project/motioneye)
- **Raspberry Pi**: Verify network and SSL configuration
- **Android App**: Check troubleshooting section above
- **Kotlin/Java**: Refer to official Android documentation

## 🙏 Credits

- Built with Kotlin + Java
- Uses Material Design components
- Powered by AndroidX libraries
- SSL handling inspired by Android security best practices

---

**Made with ❤️ for MotionEye users**
