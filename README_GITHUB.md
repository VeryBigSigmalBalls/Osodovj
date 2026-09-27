# Arc Injector - GitHub Build

This repository is designed so the APK can be built by GitHub Actions instead of AndroidIDE.

## Build

1. Create a GitHub repository.
2. Upload all files from this project.
3. Push to `main` or `master`, or open **Actions → Build Arc Injector APK → Run workflow**.
4. Wait for the workflow to finish.
5. Open the completed workflow run.
6. Download the `ArcInjector-debug` artifact.
7. Extract it and install `app-debug.apk`.

The workflow installs JDK 17, Android SDK packages, and Gradle 8.1 on the GitHub runner.

## Configure Arc.json

Edit:

`app/src/main/assets/arc.json`

Use your own hero/skin data and HTTPS package URLs.

Example mapping:

`"default->skin_01": "https://your-domain.example/package.zip"`

The Inject button only enables when a valid mapping exists and Shizuku is ready.

## Local AndroidIDE

This project does not contain a phone-specific `org.gradle.java.home`. AndroidIDE can still open/edit the project, but GitHub Actions is the intended build environment.
