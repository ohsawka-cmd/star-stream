# Project Star Stream — Android Prototype 0.1

An Android-first prototype for a real, floating, ORV-inspired holographic scenario interface.

## What is implemented

- Native Android app written in Kotlin (no Android Studio project wizard required).
- System overlay permission flow.
- Foreground service with a persistent notification during an active overlay session.
- Centered holographic blue panel, curved double-glow frame, drop shadow, scanlines, short glitch entrance.
- Functional **Accept**, **Decline**, **Complete**, and **Close** actions.
- A *demonstration* countdown for a scenario after acceptance.
- Automated GitHub Actions debug APK build.

**Not implemented yet:** permanent event storage, actual coin accounting, AI, constellation registry, schedules, or Mac synchronization. Rewards shown in this prototype are demonstration text only. The countdown is not persisted across service restarts yet.

## No Android Studio needed

This repository builds in GitHub Actions. You can review/change code using ChatGPT and install the generated debug APK on your Galaxy S23 Ultra.

1. Create an empty **private** GitHub repository called `star-stream` using https://github.com/new (no README or .gitignore needed).
2. Upload/push the contents of this project to that repository. If you have connected GitHub to ChatGPT, ChatGPT can write the project files to the empty repo after you create it; this avoids manual Git commands.
3. Open the repository's **Actions** tab, then select **Build Android APK**. The workflow runs on pushes to `main`, or you can trigger it manually.
4. Once the workflow succeeds, open its run and download the **star-stream-debug-apk** artifact ZIP.
5. Unzip and transfer `app-debug.apk` to your S23 Ultra. Open it on your phone and approve Android's install-unknown-apps prompt for the application used to open it. Only install APKs you built yourself or trust.
6. Start the app, tap **Allow floating windows**, enable "Appear on top" / "Display over other apps", return to the app, then tap **Show system scenario**.
7. Press Home or open another app to verify the floating window. Tap **Accept** and **Complete** to test the interaction.

If overlay permission is denied, the app won't attempt to display a window.

## Technical choices

- AGP 8.13.2 (a conservative supported toolchain), Gradle 8.13, JDK 17, Kotlin 2.2.20.
- Android SDK 35 for compiling, minimum Android API 29.
- Native `WindowManager.TYPE_APPLICATION_OVERLAY`, no accessibility service, no screen reading.
- Foreground service type `specialUse`; Android's persistent service notification gives you a way to find/stop an active overlay.
- No Internet permission and no external backend.

## Next development tasks

- Move scenario definitions/timer data into a Room database.
- Add durable event-sourced records before implementing rewards.
- Introduce a notification queue and configurable quiet/work hours.
- Add a second overlay style for brief constellation messages.
- Use a secure sync layer and AI narrative service only once the local foundation works.

## Known restrictions

Android/Samsung can restrict overlays on certain system or protected screens and may impose background-service constraints. This proof of concept intentionally starts the overlay from a visible activity. Motion and rendering on the S23 Ultra must be tested on the device. No claim of successful compilation is made until the GitHub Actions workflow completes.
