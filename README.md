# MoneyManagerEx for Android

See our homepage at [android.moneymanagerex.org](http://android.moneymanagerex.org/) for more user-oriented information.

Dev [![Build Status](https://travis-ci.org/moneymanagerex/android-money-manager-ex.svg?branch=dev)](https://travis-ci.org/moneymanagerex/android-money-manager-ex)

Stable [![Build Status](https://travis-ci.org/moneymanagerex/android-money-manager-ex.svg?branch=master)](https://travis-ci.org/moneymanagerex/android-money-manager-ex)

#### Basic information

Money Manager Ex for Android is a mobile companion to PC/desktop personal finance applications currently available for Windows, Mac OSX and LINUX. It can be used with the same database used by MoneyManagerEx Desktop, or as an addition to GnuCash or other applications that import transactions via .qif files.

#### Links

[![MoneyManagerEx for Android on PlayStore](https://developer.android.com/images/brand/en_app_rgb_wo_60.png)](http://play.google.com/store/apps/details?id=com.money.manager.ex)

#### Contributing

There are several ways you can contribute to the project:

- code
- translation
- beta testing
- donations

#### Translate

If you want to join our translation team: [MoneyManagerEx for Android on Crowdin.net](https://crowdin.net/project/android-money-manager-ex)

#### Beta Testing

You can install the Beta version from [Google Play](https://play.google.com/store/apps/details?id=com.money.manager.ex.beta) in parallel to the stable version, and help us out testing the app before it reaches the Stable channel.

# Build

To build the project, the following settings are recommended:

- current stable version of Android Studio,
- use embedded JDK,
- use gradle wrapper.

You will need to generate a custom "fabric.properties" file in "app" directory, containing apiSecret and apiKey values.
More info at [Fabric API Keys](https://docs.fabric.io/android/fabric/settings/api-keys.html).

## Continuous Integration

Travis CI build is active for the project. It runs a build on every check-in and a pull request.

Useful content is available at the Fabric/Travis [demo](https://github.com/plastiv/CrashlyticsDemo). It explains how to generate fabric.properties file during the build, using environment variables.
The environment variables start with org_gradle_project, i.e. ORG_GRADLE_PROJECT_crashlyticsdemoApikey.

# License

    Copyright 2012-2017 The Android Money Manager Ex Project Team

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 3
    of the License, or (at your option) any later version.

    https://www.gnu.org/licenses/gpl-2.0.html

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
