Alarmio is a simple alarm clock that implements many useful features while following regular design standards to ensure that it is quick and intuitive to use.

[![Build Status](https://travis-ci.com/fennifith/Alarmio.svg?branch=master)](https://travis-ci.com/fennifith/Alarmio)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/e214b14f27464ce39a24539fc0ca27a5)](https://www.codacy.com/app/fennifith/Alarmio?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=fennifith/Alarmio&amp;utm_campaign=Badge_Grade)
[![Discord](https://img.shields.io/discord/514625116706177035.svg?logo=discord&colorB=7289da)](https://discord.gg/kgqJ5hM)
[![Liberapay](https://img.shields.io/badge/liberapay-donate-yellow.svg?logo=liberapay)](https://liberapay.com/fennifith/donate)


## Screenshots

| Home | Alarms | Timers | Themes | Ringing |
|------|--------|--------|--------|---------|
| ![img](./.github/images/home.png?raw=true) | ![img](./.github/images/alarms.png?raw=true) | ![img](./.github/images/timers.png?raw=true) | ![img](./.github/images/themes.png?raw=true) | ![img](./.github/images/alert.gif?raw=true) |

## Installation

The app is published on Google Play:

[<img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png"
    alt="Get it on Google Play"
    height="80">](https://play.google.com/store/apps/details?id=me.jfenn.alarmio)

Alternatively, you can download the latest APK from [the GitHub releases](../../releases/).

### Pricing

Alarmio is, of course, completely free to download and use from either the GitHub Releases, F-Droid, or testing versions in the Discord server. However, it is approximately US$1.99 to install it through Google Play. Why is this?

Essentially, when you pay the $1.99 to download the app from the Play Store, you are not paying for the right to use the app. That model [has been broken for a long time](https://updato.com/android-apps/cracked-android-apps-avoid-get-paid-apps-free) - even properly obfuscated applications are often trivial to "crack" through many tools available today, and it is no longer viable to ensure that every customer downloads your app through the Play Store and nothing else. Rather, what you are paying for is in "support" - replies to your reviews, quicker feedback when you send me an email, and so on.

Of course, this means that the inverse goes for users that choose not to pay for the app: I have absolutely no obligation to help you or fix any of your problems. Of course, this shouldn't stop you from filing an issue when you come across one, just keep in mind that I might not fix them for free.

## Permissions

- `SET_ALARM`, `VIBRATE`, `WAKE_LOCK`: uh, this should be obvious
- `ACCESS_COARSE_LOCATION`: determining automatic sunrise/sunset times for "scheduled" light/dark themes
- `INTERNET`: obtaining a set of sunrise/sunset times (location data is not shared outside of the device), as well as fetching graphical assets and some of the information in the about page
- `RECEIVE_BOOT_COMPLETED`: re-schedule alarms on startup
- `READ_EXTERNAL_STORAGE`: used to set custom background / header images in the settings
- `FOREGROUND_SERVICE`: used to notify you to sleep - see the "Sleep Reminder" option in the settings
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`: see [dontkillmyapp.com](https://dontkillmyapp.com/)

## Contributing & Build Instructions

Instructions for contributing to this project and building it locally can be found [here](./.github/CONTRIBUTING.md).
