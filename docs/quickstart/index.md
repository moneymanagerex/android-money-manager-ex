---
layout: single
author_profile: true
---

[<img alt="Get it on F-Droid" src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" width="240">](https://f-droid.org/packages/com.money.manager.ex)
[<img alt="Get it on GitHub" src="https://raw.githubusercontent.com/Kunzisoft/Github-badge/main/get-it-on-github.png" width="240">](https://github.com/moneymanagerex/android-money-manager-ex/releases/latest)
[<img alt="Get it on Google Play" src="http://steverichey.github.io/google-play-badge-svg/img/en_get.svg" width="240">](https://play.google.com/store/apps/details?id=com.money.manager.ex.android)

## Usage Types
You can use this app in standalone mode, by simply creating a DB from your mobile device, or use it in companion mode with a desktop environment.
Almost all features can be used directly on a mobile device. Features can be:
* Available as a standalone function. This means that no internet and no desktop version is necessary. ![available-standalone]( {{"/assets/badges/available-standalone.svg" | relative_url }} )
* Usable directly on your mobile but requires internet connection ![require-connection]( {{"/assets/badges/require-connection.svg" | relative_url }} )
* Requires desktop version ![require-desktop]( {{"/assets/badges/require-desktop.svg" | relative_url }} )
* Features that do not work properly on mobile are marked with ![available-standalone-red]( {{"/assets/badges/available-standalone-red.svg" | relative_url }} )

### Quickstart as standalone version
* Create a new DB on mobile. Follow the wizard and create your DB in your preferred folder on mobile
* Create an initial account, set it as default if you would like to have it as default when entering transactions
* Start recording your expenses

Detailed instructions [here](start_standalone.md)

### Quickstart as companion app for desktop
* Use your own cloud provider to synchronize files created on desktop with your mobile device. Basically any cloud provider that supports [SAF (Storage Access Framework)](https://developer.android.com/guide/topics/providers/document-provider) will work. Known working cloud providers:
  * Google Drive
  * OneDrive
  * NextCloud
  * OwnCloud
  * ...
* On desktop, save your file into your own cloud provider.
* On Android, open the file from your cloud provider.

**Notice!** If your Remote Provider supports offline files, please be sure to set up Offline availability in your cloud app. See details [here](start_companion/#troubleshooting)
{: .notice--warning}

Detailed instructions [here](start_companion.md)
