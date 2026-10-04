# 📱 WhatsApp Android Native Application

An official native Android WhatsApp clone engineered in Kotlin and Jetpack Compose with Material 3, supporting Android 9 (API 28), 10 (API 29), 11 (API 30), 12 (API 31/32), 13, 14, and 15.

---

## 🚀 Key Highlights & Architecture

### 1. 🎨 Native Android Sizing & Design
- **Pixel-Perfect Header & Bottom Navigation**: Utilizes `enableEdgeToEdge()` with Android `WindowInsets` (`statusBarsPadding()` and `navigationBarsPadding()`) so the top app bar and bottom tabs sit cleanly without overlap or awkward cutoffs on any Android screen.
- **Strictly Official Icons**: Zero emojis used as UI icons. Uses official Material & Vector icons (`Icons.Default.*` and `Icons.Outlined.*`).
- **Signature WhatsApp Dark Palette**: `#008069` (WhatsApp Green), `#128C7E` (Teal), `#25D366` (Light Green), `#0B141A` (Dark Background), and `#111B21` (Surface).

### 2. 🔐 Authentication & Onboarding
- **Zero Phone OTP**: Direct Email & Password authentication + Google Sign-In via Firebase Auth.
- **Onboarding Pipeline**:
  1. Splash Screen
  2. Step-by-Step Permissions Screen (Microphone, Camera/Storage, Notifications, Background Service)
  3. Welcome Screen: Strictly two buttons (`Create Account` & `Login Account`)
  4. Create Account: Email -> Password -> Username (`@xxxxxx`) with live availability verification (official green checkmark for available, red rejection if taken) + Circular Avatar preview
  5. Google Sign-In: Direct entry to Username & Avatar setup. Shows a recurring modal reminder on the main dashboard: *"Set your password now or skip"* until the user configures their account password.

### 3. 🛡️ Unique 3-Option Password Recovery System
Users configure ONE of the three recovery methods in **Settings > Account Security**:
1. **8-Digit Forget Code** (e.g. `84920158`)
2. **13-Digit CNIC Code** (e.g. `4210112345671`)
3. **Restore Phone Number** (e.g. `03001234567`)

On the **Login** screen, tapping **Forget Password?**:
- Prompts for Email or Username
- Presents the 3 recovery method choices
- User enters their secret information
- Verified against Firebase Realtime Database
- Upon match -> Reveals the **Set New Password** fields and updates Firebase Auth and DB.

### 4. ☁️ Real Firebase Realtime Database & Cloudinary Integrations
- **Firebase Realtime Database**: `https://whatsapp-app-437b9-default-rtdb.firebaseio.com` configured with lifetime running rules for instant messaging, status syncing, and presence.
- **Cloudinary Media Storage**: Unsigned upload preset `whatsapp_uploads` on cloud `dtpuqzq0e` handles high-speed uploads for:
  - Profile Avatars
  - Chat Photos & Videos
  - PDF & Documents
  - Voice Audio Recordings
  - Status Stories

### 5. 🎙️ Professional Voice Recording Studio & Audio Trimming
- Tapping the microphone button displays a sleek recording studio modal with:
  - Glowing animated recording indicator
  - Real-time timer (`00:07`)
  - Reactive animated waveform bars driven by live microphone amplitudes
  - Cancel / Trash button
  - **Edit & Crop Button**: Opens the **Audio Trimmer Sheet** where users can:
    - Adjust start & end sliders
    - Preview the cropped segment with audio playback
    - Discard or send the cropped HD voice note directly to chat!

### 6. 📞 Live Voice & Video Calling
- **Incoming Call Fullscreen UI**: Pops up over the lock screen with custom ringing and vibration, displaying caller avatar, caller name, and Accept / Decline actions.
- **Active In-Call Controls**:
  - Mute / Unmute microphone
  - Loudspeaker / Earpiece switch
  - Camera flip (Front / Back)
  - Video toggle (On / Off)
  - End call with duration timer.

### 7. 💬 Advanced Chat Features
- **User Discovery**: Search and add contacts by `@username`.
- **Delivery Ticks**:
  - Single grey tick (`SENT` - receiver offline)
  - Double grey tick (`DELIVERED`)
  - Double blue tick (`SEEN` / read)
- **Message Actions**:
  - Star / Unstar messages
  - Delete for Me
  - Delete for Everyone
  - View-Once ("1" badge media that erases upon viewing)
  - Copy message text to Android clipboard
- **Quick Profile Popup**: Tapping a user's circular avatar in the chat list opens a WhatsApp-style modal card with enlarged photo and 4 action buttons:
  1. Message
  2. Audio Call
  3. Video Call
  4. Info (View Full Profile)
  - Tapping photo opens Fullscreen Photo Viewer.
  - Tapping Info opens Profile Screen with Block / Unblock contact functionality.

### 8. 📸 24-Hour Status Stories
- Post photo status with captions.
- Granular Privacy Controls:
  - *My Contacts*
  - *My Contacts Except...*
  - *Only Share With / Private*
- View status with Instagram/WhatsApp-style progress bars, viewer counters, and delete options.

---

## 🛠️ Build & CI/CD Pipeline
- Automated GitHub Actions workflow at [`.github/workflows/build-apk.yml`](file:///.github/workflows/build-apk.yml) compiles `app-debug.apk` and generates GitHub Releases on every push.
