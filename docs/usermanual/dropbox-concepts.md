**obsolete**: new remote file management with SAF

# Dropbox Concepts

Here we describe how MMEX for Android works with Dropbox to synchronize the database with MMEX running on other platforms or other devices.

After the [initial setup](Dropbox-synchronization), you should end up with one or more devices connected to Dropbox service.

There is a basic difference in how the Dropbox client works on Android in comparison to how it works on the desktop platforms. These details are described in the separate sections
below.

## Basic Scenario

When you make a change on a mobile device, the database is uploaded to Dropbox after about 30 seconds. It is then available for other devices to download. The change will be picked
up by the desktop Dropbox in a matter of seconds. Once the database is downloaded by a desktop application, you can open MoneyManagerEx on the desktop and see the data just updated
from the mobile device.
The opposite direction is similar. As soon as you make a change on the desktop, it will be recognised by Dropbox and uploaded to the cloud. Opening the app on your phone will
notify you of a newer version being available on Dropbox. If you have an app already open, you can manually synchronise via side menu.

## Android

The characteristics of Dropbox synchronization on Android are described in this section.

The Dropbox synchronization on Android requires an explicit notification from an application (in this case MMEX) requesting the notification. MoneyManagerEx for Android will
initiate these update notifications after data modifications, based on your preferences in Settings -> Dropbox settings.

## Desktop

The characteristics of Dropbox synchronization on a desktop platform are described in this section.

Dropbox synchronization on desktop will do all the work on its own, compared to the Android version.

When using the desktop MoneyManagerEx application, it is enough to simply open a database from the Dropbox folder and any changes will be automatically synchronized by Dropbox
application.
You can see the activity of Dropbox application in the Dropbox icon in your operating system tray.

## Tips

One tip would be to close the desktop application while synchronising to the PC as in some cases the database on the PC is locked by the system and can result in conflicting
updates.

### Dropbox Client

Be mindful of the desktop dropbox client. Users on Windows operating system have two versions available to them. The correct one is available from the Dropbox web site while there
is also a version in the Windows Marketplace.
The first is the full Dropbox synchronization client which runs on your PC when you are logged in and does synchronization in the background.
The second one is an equivalent of a Windows Phone app that has no synchronization capabilities and offers only limited capabilities compared to the full desktop version.

In all the scenarios, we assume the users are using the full desktop version of the Dropbox application.
