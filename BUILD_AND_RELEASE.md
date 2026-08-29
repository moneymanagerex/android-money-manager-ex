# Build and Release Guide

This document provides instructions on how to build, sign, and release the Android application locally and via GitHub Actions.

---

## 1. Local Environment Setup

To compile and sign release builds locally (from Android Studio or the command line), you need to configure your local signing credentials.

### Step 1: Create `local.properties`
In the root directory of the project, create a file named `local.properties` (this file is ignored by Git and should never be committed).

### Step 2: Configure Signing Properties
Add your keystore path and passwords to `local.properties` using the following template:
```properties
signing.store.file=path/to/your/release.keystore
signing.store.password=YOUR_STORE_PASSWORD
signing.key.alias=androiddebugkey
signing.key.password=YOUR_KEY_PASSWORD

```

*(Note: On Windows, use double backslashes `\\` for file paths or forward slashes `/`).*

You can copy [example.local.properties](example.local.properties)

---

## 2. Building Locally

Once `local.properties` is configured, you can build signed release bundles and APKs directly from your terminal using Gradle:

```bash
./gradlew bundleRelease assembleRelease

```

The generated files will be located in:

* **Bundles (AAB):** `app/build/outputs/bundle/`
* **APKs:** `app/build/outputs/apk/release/`

You can also use Android Studio's standard graphical interface: **Build -> Generate Signed Bundle / APK**.

---

## 3. CI/CD: GitHub Actions Workflow

The repository includes an automated workflow located at `.github/workflows/release.yml`. It triggers automatically whenever a push is made to any branch matching `release/v*`.

The workflow performs the following steps:

1. Checks out the repository (with full history for release notes).
2. Sets up JDK 17.
3. Decodes the secure keystore from GitHub Secrets.
4. Compiles all product flavors (`fdroid`, `gplay`, `sync`) in both AAB and APK formats.
5. Automatically generates standard GitHub release notes containing merged Pull Requests and contributors.
6. Publishes all built assets directly to the GitHub Releases page
7. Publishea gplay asset on gplay under [wolfsolver](https://play.google.com/store/apps/dev?id=7247021252165625264) account in open test channel.

---

## 4. Required GitHub Secrets

To allow the GitHub Action to sign the app and interact with services, the following secrets must be configured in your GitHub Repository under **Settings > Secrets and variables > Actions**:

| Secret Name | Description |
| --- | --- |
| `RELEASE_KEYSTORE_BASE64` | Your release keystore file encoded in Base64 format. |
| `SIGNING_STORE_PASSWORD` | The password for your keystore. |
| `SIGNING_KEY_PASSWORD` | The password for the specific signing key alias. |
| `PLAY_CONSOLE_JSON` | Service account JSON key for Google Play Console integration (optional/future use). |

### How to generate the Base64 Keystore Secret:

Run the following command in your terminal where your keystore file is located:

* **Linux / macOS:** `base64 -i your-keystore-file.keystore`
* **Windows (PowerShell):** `[Convert]::ToBase64String([IO.File]::ReadAllBytes("your-keystore-file.keystore"))`

Copy the resulting text string and paste it into the `RELEASE_KEYSTORE_BASE64` GitHub Secret.
