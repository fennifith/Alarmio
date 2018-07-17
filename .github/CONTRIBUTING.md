# Contributing

As long as your pull request doesn't break the app in any way or conflict with an ongoing change (check the open issues first or send me [an email](mailto:dev@jfenn.me) to make sure this doesn't happen), it will likely be accepted. I have a lot of projects and may not get around to reviewing pull requests right away. If you have contributed to this project before and would like to be added to this repository as a collaborator, feel free to send me [an email](mailto:dev@jfenn.me) and ask.

## Branches

This project is still in closed alpha testing only, so I'm currently just doing everything on the master branch. This may change in the future, and this section will be updated accordingly.

## Build Instructions

The source published on GitHub should build fine like any other Android project with no modifications. However, the theming library ([aesthetic](https://github.com/afollestad/aesthetic)) that I am using has [a certain issue](https://github.com/afollestad/aesthetic/issues/80) causing a crash upon launch on most devices. This issue has actually been fixed in the source of the library that is published on GitHub, but has not yet been released on jCenter for multiple reasons. As a workaround, you can download the source of the repository, import the 'aesthetic' module into the project, and replace aesthetic's dependency with `implementation project(':aesthetic')`.

You will also need to override certain dependencies from the aesthetic module in the app build.gradle (by adding the following to the 'dependencies' block):
```gradle
    implementation 'io.reactivex.rxjava2:rxjava:2.1.0'
    implementation 'io.reactivex.rxjava2:rxandroid:2.0.1'
    implementation 'com.f2prateek.rx.preferences2:rx-preferences:2.0.0-RC2'

    implementation 'me.zhanghai.android.materialprogressbar:library:1.4.1'
```

