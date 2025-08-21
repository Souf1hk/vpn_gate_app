# VPN Gate Android - Setup Guide

## Quick Start

### Prerequisites
- **Android Studio**: Arctic Fox (2020.3.1) or later
- **Java Development Kit**: JDK 8 or later
- **Android SDK**: API level 21 (Android 5.0) or higher

### Installation Steps

1. **Extract the Project**
   ```bash
   unzip VpnGateAndroid.zip
   cd VpnGateAndroid
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing Android Studio project"
   - Navigate to the extracted `VpnGateAndroid` folder
   - Click "OK" to open the project

3. **Sync Project**
   - Android Studio will automatically start syncing the project
   - Wait for the Gradle sync to complete
   - If prompted, accept any SDK updates

4. **Build the Project**
   ```bash
   ./gradlew build
   ```

5. **Run on Device/Emulator**
   - Connect an Android device or start an emulator
   - Click the "Run" button in Android Studio
   - Or use command line: `./gradlew installDebug`

### Project Structure

```
VpnGateAndroid/
├── app/
│   ├── src/main/
│   │   ├── java/com/vpngate/android/
│   │   │   ├── api/              # API and networking
│   │   │   ├── model/            # Data models
│   │   │   ├── repository/       # Data repositories
│   │   │   ├── service/          # Background services
│   │   │   ├── ui/               # User interface
│   │   │   ├── util/             # Utility classes
│   │   │   ├── adapter/          # RecyclerView adapters
│   │   │   └── receiver/         # Broadcast receivers
│   │   ├── res/                  # Android resources
│   │   └── AndroidManifest.xml   # App manifest
│   ├── build.gradle              # App-level Gradle
│   └── proguard-rules.pro        # ProGuard configuration
├── gradle/                       # Gradle wrapper
├── build.gradle                  # Project-level Gradle
├── settings.gradle               # Gradle settings
├── gradle.properties            # Gradle properties
├── README.md                    # Documentation
├── LICENSE                      # MIT License
└── .gitignore                   # Git ignore rules
```

### Key Features Implemented

✅ **VPN Service**: Complete VpnService implementation with OpenVPN support  
✅ **Server Management**: VPN Gate API integration with server list  
✅ **Modern UI**: Material Design 3 with dark theme support  
✅ **Security Features**: Kill switch, DNS protection, custom DNS  
✅ **Statistics**: Real-time connection monitoring and data usage  
✅ **Settings**: Comprehensive configuration options  
✅ **Auto-Connect**: Boot receiver and auto-connection features  

### Development Notes

- **Min SDK**: 21 (Android 5.0 Lollipop)
- **Target SDK**: 34 (Android 14)
- **Architecture**: Clean architecture with repository pattern
- **Libraries**: AndroidX, Material Design, Retrofit, RxJava3
- **Build System**: Gradle with ProGuard for release builds

### Testing

1. **Debug Build**: `./gradlew assembleDebug`
2. **Release Build**: `./gradlew assembleRelease`
3. **Install**: `./gradlew installDebug`
4. **Test**: `./gradlew test`

### Customization

- **App Name**: Modify `app_name` in `res/values/strings.xml`
- **Package Name**: Change in `build.gradle` and refactor packages
- **Colors**: Update `res/values/colors.xml`
- **Themes**: Customize `res/values/themes.xml`
- **Icons**: Replace files in `res/mipmap-*` directories

### Troubleshooting

**Gradle Sync Issues:**
- Ensure you have the latest Android Studio
- Check internet connection for dependency downloads
- Try "File → Invalidate Caches and Restart"

**Build Errors:**
- Verify Java version (JDK 8 or later)
- Check Android SDK installation
- Update Android Gradle Plugin if needed

**Runtime Issues:**
- Grant VPN permission when prompted
- Check device compatibility (API 21+)
- Verify network connectivity

### Next Steps

1. **Add App Icons**: Replace placeholder icons with actual PNG files
2. **Test on Devices**: Test on various Android versions and devices
3. **Customize UI**: Modify colors, themes, and layouts as needed
4. **Add Features**: Implement additional VPN protocols or features
5. **Publish**: Prepare for Google Play Store submission

### Support

For issues or questions:
- Check the main README.md for detailed documentation
- Review Android Studio's built-in documentation
- Consult Android developer guides

**Happy Coding! 🚀**