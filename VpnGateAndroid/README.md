# VPN Gate Android Client

A professional Android VPN client for connecting to VPN Gate public servers, built with Java and modern Android development practices.

## Features

### 🔐 VPN Connectivity
- **OpenVPN Protocol Support**: Secure connections using industry-standard OpenVPN
- **Auto Server Selection**: Intelligent selection of best available servers
- **Manual Server Selection**: Browse and choose from hundreds of public servers
- **Connection Statistics**: Real-time monitoring of data usage, speed, and connection time
- **Favorites System**: Save frequently used servers for quick access

### 🛡️ Security Features
- **Kill Switch**: Blocks internet traffic if VPN connection drops
- **DNS Protection**: Uses secure DNS servers to prevent DNS leaks
- **Custom DNS**: Configure your own DNS servers
- **LAN Bypass**: Allow local network access while VPN is active
- **Ad & Malware Blocking**: Optional blocking of advertising and malicious domains

### 🎨 Modern UI/UX
- **Material Design 3**: Beautiful, modern interface following Google's design guidelines
- **Dark Theme Support**: Automatic dark/light theme switching
- **Responsive Design**: Optimized for all screen sizes
- **Intuitive Navigation**: Easy-to-use interface for all skill levels
- **Real-time Updates**: Live connection status and statistics

### ⚙️ Advanced Settings
- **Auto-Connect**: Automatically connect to VPN on app start
- **Start on Boot**: Launch VPN when device boots
- **Auto-Reconnect**: Automatic reconnection if connection drops
- **Connection Timeout**: Configurable timeout settings
- **Server Filtering**: Filter servers by country, ping, speed
- **Multiple Protocols**: Support for OpenVPN, IKEv2, and WireGuard (planned)

## Technical Architecture

### Core Components
- **VpnGateService**: Main VPN service extending Android's VpnService
- **VpnServerRepository**: Manages server data and API integration
- **OpenVpnManager**: Handles OpenVPN protocol implementation
- **ConnectionMonitorService**: Monitors connection health and statistics
- **PreferenceManager**: Manages app settings and user preferences

### API Integration
- **VPN Gate API**: Fetches real-time server list from VPN Gate project
- **CSV Parsing**: Efficient parsing of server data
- **Caching System**: Smart caching for improved performance
- **Error Handling**: Robust error handling and retry mechanisms

### Security Implementation
- **VPN Service**: Uses Android's native VPN framework
- **Permission Management**: Proper VPN permission handling
- **Network Monitoring**: Real-time network state monitoring
- **Kill Switch**: Traffic blocking when VPN is disconnected

## Project Structure

```
src/main/java/com/vpngate/android/
├── VpnGateApplication.java          # Main application class
├── api/                             # API and networking
│   ├── ApiClient.java
│   └── VpnGateApi.java
├── model/                           # Data models
│   ├── VpnServer.java
│   ├── ConnectionStats.java
│   └── VpnConfig.java
├── repository/                      # Data repositories
│   └── VpnServerRepository.java
├── service/                         # Background services
│   ├── VpnGateService.java
│   └── ConnectionMonitorService.java
├── ui/                             # User interface
│   ├── MainActivity.java
│   ├── ServerListActivity.java
│   └── SettingsActivity.java
├── adapter/                        # RecyclerView adapters
│   └── ServerListAdapter.java
├── util/                           # Utility classes
│   ├── OpenVpnManager.java
│   └── PreferenceManager.java
└── receiver/                       # Broadcast receivers
    ├── BootReceiver.java
    └── NetworkStateReceiver.java
```

## Dependencies

### Core Android Libraries
- AndroidX Core, AppCompat, Activity, Fragment
- Material Design Components
- ConstraintLayout, RecyclerView, SwipeRefreshLayout
- Lifecycle Components, Navigation Components
- Preferences Library

### Networking & Data
- Retrofit 2 with RxJava3 adapter
- OkHttp3 with logging interceptor
- Gson for JSON parsing
- RxJava3 for reactive programming

### UI & UX
- Glide for image loading
- MPAndroidChart for statistics visualization
- WorldCountryData for country flags
- Material Design 3 theming

### Utilities
- Timber for logging
- Dexter for permissions
- LocalBroadcastManager for internal communication

## Build Requirements

- **Android Studio**: Arctic Fox (2020.3.1) or later
- **Compile SDK**: 34 (Android 14)
- **Min SDK**: 21 (Android 5.0)
- **Target SDK**: 34 (Android 14)
- **Java Version**: 8 or later

## Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/vpn-gate-android.git
   cd vpn-gate-android
   ```

2. **Open in Android Studio**:
   - Open Android Studio
   - Select "Open an existing Android Studio project"
   - Navigate to the cloned directory and select it

3. **Build the project**:
   ```bash
   ./gradlew build
   ```

4. **Run on device/emulator**:
   ```bash
   ./gradlew installDebug
   ```

## Configuration

### VPN Gate API
The app automatically fetches server lists from the VPN Gate project API. No additional configuration is required.

### Custom DNS Servers
Default DNS servers are set to Google DNS (8.8.8.8, 8.8.4.4). Users can configure custom DNS servers in the app settings.

### OpenVPN Configuration
OpenVPN configurations are automatically downloaded and processed from the VPN Gate servers. The app modifies these configurations for Android compatibility.

## Usage

### First Launch
1. Grant VPN permission when prompted
2. Allow the app to create VPN connections
3. Choose "Auto Select" for best server or browse server list
4. Tap "Connect" to establish VPN connection

### Server Selection
- **Auto Select**: Automatically chooses the best available server
- **Server List**: Browse all available servers with filtering options
- **Favorites**: Save frequently used servers for quick access
- **Search**: Find servers by country or IP address

### Connection Management
- **Connect/Disconnect**: Simple one-tap connection control
- **Statistics**: View real-time connection statistics
- **Notification**: Persistent notification shows connection status
- **Kill Switch**: Automatically blocks traffic if VPN disconnects

## Security Considerations

### VPN Protocol
- Uses industry-standard OpenVPN protocol
- Supports strong encryption (AES-256, RSA-2048)
- Perfect Forward Secrecy (PFS) support

### DNS Protection
- Prevents DNS leaks through custom DNS servers
- Optional ad and malware blocking
- DNS-over-HTTPS support (planned)

### Traffic Protection
- Kill switch prevents IP leaks
- IPv6 leak protection
- WebRTC leak protection (planned)

## About VPN Gate

VPN Gate is an academic experiment project at the University of Tsukuba, Japan. It provides free VPN servers operated by volunteers around the world. This app is an independent client implementation and is not officially affiliated with the VPN Gate project.

### Important Notes
- VPN Gate servers are operated by volunteers
- Connection quality and availability may vary
- Use responsibly and respect server operators
- Some servers may have usage restrictions

## Contributing

Contributions are welcome! Please feel free to submit pull requests, report bugs, or suggest new features.

### Development Guidelines
- Follow Android development best practices
- Use Material Design guidelines
- Write clean, documented code
- Add unit tests for new features
- Follow the existing code style

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Disclaimer

This app is provided as-is for educational and research purposes. Users are responsible for complying with local laws and regulations regarding VPN usage. The developers are not responsible for any misuse of this software.

## Support

For support, bug reports, or feature requests, please open an issue on the GitHub repository.

---

**Built with ❤️ for the Android community**