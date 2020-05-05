Alarmio
[![Build Status](https://github.com/fennifith/Alarmio/workflows/Gradle%20Build/badge.svg)](https://github.com/fennifith/Alarmio/actions)
[![Discord](https://img.shields.io/discord/514625116706177035.svg?logo=discord&colorB=7289da)](https://discord.jfenn.me/)
[![Liberapay](https://img.shields.io/badge/liberapay-donate-yellow.svg?logo=liberapay)](https://jfenn.me/links/liberapay)
=======

Alarmio is a simple alarm clock that implements many useful features while following regular design standards to ensure that it is quick and intuitive to use.

## Screenshots

| Home | Alarms | Timers | Themes | Ringing |
|------|--------|--------|--------|---------|
| ![img](./.github/images/home.png?raw=true) | ![img](./.github/images/alarms.png?raw=true) | ![img](./.github/images/timers.png?raw=true) | ![img](./.github/images/themes.png?raw=true) | ![img](./.github/images/alert.gif?raw=true) |

## Installation

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/me.jfenn.alarmio/)
[<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png"
     alt="Get it on Google Play"
     height="80">](https://play.google.com/store/apps/details?id=me.jfenn.alarmio)

Alternatively, you can download the latest APK from [the GitHub releases](../../releases/).

### Pricing

Alarmio is, of course, completely free to download and use from either the GitHub Releases, F-Droid, or my website. However, it is approximately US$1.99 to install it through Google Play. Why is this?

Essentially, when you pay the $1.99 to download the app from the Play Store, you are not paying for the right to use the app. That model [has been broken for a long time](https://updato.com/android-apps/cracked-android-apps-avoid-get-paid-apps-free) - even properly obfuscated applications are often trivial to "crack" through many tools available today, and it is no longer viable to ensure that every customer downloads your app through the Play Store and nothing else. Rather, what you are paying for is "support" - replies to your reviews, quicker responses when you send me an email, and the effort of dealing with Google Play as a method of distribution ([it's a bit of a pain sometimes](https://www.reddit.com/r/androiddev/comments/9n88wv/the_future_of_android_development/)).

### Permissions

- `SET_ALARM`, `VIBRATE`, `WAKE_LOCK`: uh, this should be obvious
- `ACCESS_COARSE_LOCATION`: determining automatic sunrise/sunset times for "scheduled" light/dark themes
- `INTERNET`: obtaining a set of sunrise/sunset times (location data is not shared outside of the device), as well as fetching graphical assets and some of the information in the about page
- `RECEIVE_BOOT_COMPLETED`: re-schedule alarms on startup
- `READ_EXTERNAL_STORAGE`: used to set custom background / header images in the settings
- `FOREGROUND_SERVICE`: used to notify you to sleep - see the "Sleep Reminder" option in the settings
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`: see [dontkillmyapp.com](https://dontkillmyapp.com/)

## How to Contribute

Alarmio is written by users - everyone working on this project (including myself) is motivated entirely by their own needs and ideals. There is no professional support line to handle complaints or paid development team to fix bugs. Anyone that volunteers their time and expertise to improve this app does so without any obligation or commitment to the users of this software.

As a contributor, you are highly valued - any contribution, no matter how small, misled, or incorrectly formatted, is welcome. We'd much prefer to work together and resolve an issue than turn any genuine effort away. **If you need help with this process, have any questions or confusion, or want to get feedback before a contribution, please don't hesitate to get in touch.** (either [discord](https://discord.jfenn.me/) or [email](mailto:dev@jfenn.me) work fine)

### Example contributions

- **Development:** Developers can help Alarmio by [fixing bugs](https://github.com/fennifith/Alarmio/issues), implementing features, or helping to debug & research new issues. I'm hoping to write a complete guide to this process in the future - for now, please refer to [CONTRIBUTING.md](./.github/CONTRIBUTING.md).
- **Design:** Alarmio should be intuitive and accessible to a wide variety of users - suggestions to improve certain interfaces are always welcome. This includes compatibility with screen readers, problems with contrast / color blindness, and the sizing/positioning of touch targets in the app - many of which are shamefully untested in Alarmio's present state.
- **Helping users:** Often times, issues are created that go untouched for a couple days due to various factors, when they could be resolved immediately. Responding to obvious questions and identifying duplicate issues can be immensely helpful in reducing the workload on other maintainers and developers.
- **Filing issues:** Accurately reporting bugs and edge cases you experience can drastically reduce the amount of work we need to do to identify a problem. Providing relevant info - the name & manufacturer of your device, its Android version, the version of Alarmio the bug was encountered in, and a thorough description of how to reproduce it - helps others to understand what the issue is and how it could be solved. If you want to go the extra mile, screen recordings and logcat info are often immensely helpful. Please be sure to check that there isn't already an open issue describing your problem, though!
- **Localization:** If Alarmio doesn't have support for your fluent language(s), please consider translating it! Most in-app text is stored in [strings.xml](./app/src/main/res/values/strings.xml) - this file should be copied to `../values-{lang}/strings.xml` when translated. (this is an absurdly concise explanation - if this isn't clear, simply sending us translations in a new issue or email is perfectly fine!)
- **Documentation:** Writing guides and explanations of how Alarmio works, how to use it, and how to contribute to it, can go a long way to ensuring its usefulness and stability in the future. Whether this involves an update to the README, a tutorial for users and contributors, or adding Javadocs & comments to undocumented functions in the app - anything is valid!

### Other ways to support Alarmio

Not everyone has the time or technical knowledge to help out with many of the above - we understand that. With this in mind, here are a few other ways to help us out that don't require as much time or dedication.

- **Advertising:** Spread the word! If you like what we're doing here, getting more people involved is the best way to help improve it. Suggestions include: posting on social media... writing a blog post... yeah, right, you get the idea.
- **Donations:** I've invested a lot of time into this app - as have many of its contributors. Many of us have paying jobs, difficult classes, or otherwise important life occurrences that prevent us from putting all our time into software. Supporting us helps to fund the time we spend building an open source alarm clock when we could be doing... literally anything else :)
- **Politics:** Yep, you're reading this right. Free software is, in fact, a very political thing - and I'd like to think we're taking the right approach to it. If you're in a position to do so, I recommend supporting FOSS applications and services over their proprietary counterparts. In addition, similar movements that support user freedom - many supported by the [SFC](https://sfconservancy.org/) or [EFF](https://www.eff.org/), as well as [Right to Repair](https://repair.org) - would benefit from increased awareness and advocacy.

## Acknowledgements

I'd like to give a huuuuuge thanks to all of Alarmio's [contributors](https://github.com/fennifith/Alarmio/graphs/contributors), the developers that write the software we depend on, and the users that support our goal. Also, props to the [F-Droid team](https://f-droid.org/en/about/) for maintaining the free software repository that distributes our app and many others like it.

I've received a lot of thanks from various people for the time I've put into this, and that thought helps me get up in the morning. If someone fixes a bug you encountered, helps you out in an issue, or implements a feature you enjoy, please consider sending them a tip or a thank-you note to let them know that you appreciate their time :)

_Contributors: feel free to create a PR to add/edit your listing here, as well as in the app ([attribouter.xml](./app/src/main/res/xml/attribouter.xml))._

### Contributors

- [@fennifith](https://github.com/fennifith) (me) - [liberapay](http://jfenn.me/links/liberapay), [paypal](http://jfenn.me/links/paypal)
- [@quipri](https://github.com/quipri)
- [@ajayyy](https://github.com/ajayyy) - [website](https://ajay.app/)
- [@raboof](https://github.com/raboof) - [website](https://arnout.engelen.eu/)

### Translators

- [@yzqzss](https://github.com/yzqzss) (Chinese)
- [@meskobalazs](https://github.com/meskobalazs) (Hungarian)
- [@ikanakova](https://github.com/ikanakova) (Czech, Portuguese)
- [@Larnicone](https://github.com/Larnicone) (French)
- [@FriederZi](https://github.com/FriederZi) (German)
- [@pablomeza10](https://github.com/pablomeza10) (Spanish)
- [@Suburbanno](https://github.com/Suburbanno) (Portuguese)

## Related Work

- [@ajayyy](https://github.com/ajayyy) is maintaining a fork of Alarmio that forces you to dismiss the alarm from another device: [AlarmioRemote](https://github.com/ajayyy/AlarmioRemote/)
