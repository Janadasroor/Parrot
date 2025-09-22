# 🦜 Parrot – Modern Android App Template

![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)
[![Made with Jetpack Compose](https://img.shields.io/badge/Made%20with-Jetpack%20Compose-4285F4?style=flat&logo=android)](https://developer.android.com/jetpack/compose)
[![Dagger Hilt](https://img.shields.io/badge/DI-Dagger%20Hilt-5C2D91)](https://dagger.dev/hilt/)

Parrot is a **modern, extendable Android app template** built with **Jetpack Compose** and **Dagger-Hilt**.  
It's designed to **accelerate the development of production-ready apps** by providing a secure, scalable, and modular foundation.

---

## ✨ Features

- 🔒 **Secure HTTPS networking** with **dual-token authentication**
- 🔌 **Modular endpoint system** – easily add new HTTPS functions following the same structure
- 🗜️ **Built-in media compression** to reduce upload sizes before sending to the server
- 📁 **HTTPS file uploads** – send images, videos securely to your server
- 📤 **Upload manager** with progress tracking
- 🌐 **Network connectivity monitor** to handle online/offline states gracefully
- 🎥 **Video player integration** ready to stream
- 🏗️ **Clean MVVM architecture** with **Dagger-Hilt dependency injection**
- 🎨 **Material 3 Compose UI** – fully themable and easy to customize

---

## 🛠️ Open in Android Studio

1. **Clone the Repository**  
   ```bash
   git clone https://github.com/Janadasroor/Parrot.git
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select File > Open…
   - Choose the Parrot folder you just cloned

---

## ⚙️ Configure Your Endpoints

Open the file:
```
app/src/main/java/com/janad/parrot/data/api/NetworkModule.kt
```

Add your API Base URL and endpoints following the included examples.

---

## 🎨 Theming & Settings

- Built-in **Theme Selector** – switch between light/dark/custom themes
- **SharedPreferences/DataStore** used to save user settings
- Easily extendable to add your own theme options

---

## 💡 Why Parrot?

This template saves weeks of boilerplate setup by giving you a **secure networking layer, scalable architecture, and ready-to-use components** out of the box. Simply plug in your endpoints, customize the UI, and ship your app faster.

---

## 📄 License

This project is licensed under the Apache License 2.0
