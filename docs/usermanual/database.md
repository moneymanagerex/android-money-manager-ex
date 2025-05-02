---
title: "Database"
layout: single
author_profile: true
---

The database that [MoneyManagerEx for Android](/) generates and uses, known as the **.mmb** file, becomes an important file for you to maintain.

On the first run, the app will create a database in the Shared Storage on the device. The purpose for this location is that:
- it is accessible to the user
- it is accessible to other apps and scripts, for creating an automated backup, for example.

The exact location is in the directory `MoneyManagerEx` in the Internal Storage.

## [Encrypted Database](#Encrypted_Database)

Depending on circumstances, security features such as encryption can be employed, which is recognized as a **.emb** file. This is where we can attach a password to the database, and will require a password every time [MoneyManagerEx for Android](Home) is opened. The encryption feature for the database file (.emb) is not available for the Android version ![Static Badge](https://img.shields.io/badge/from%20release-5.X-gree).

