# DualApp MediaStore APIs Compatibility Test
[![CircleCI](https://circleci.com/gh/kaedea/dualapp-mediastore-compatibility.svg?style=shield&circle-token=0408c3e1410b8176b0e9ff44e6ecd0cb2ed45c7b)](#)

Tuning of Android MediaStore & File IO compatibility issues between HostProfile app and
WorkProfile app (DualApp/ParallelApp), especially for OEM Android Devices.

Also run the smoking tests for Android's **Scoped Storage** Feature.

## Enable WorkProfile for DualApp

1. Create Profile

> adb shell pm create-user --profileOf 0 --managed Island  
> adb -d shell pm list users  
> adb -d shell settings --user <user_id> put secure install_non_market_apps 1  

2. Install apk to Profile

> adb push <apk_path> /data/local/tmp/android.apk  
> adb shell pm install -r -t --user <user_id> /data/local/tmp/android.apk  

## Test Cases

1. WorkProfile/DualApp/MutliApp Recognize.
2. Read & Write file by MediaStore within DualApp.
3. File Uri/FilePath Convert within DualApp.
4. Handle sharing file within DualApp.
5. Files manipulations smoking test.

## Known Problems

All IO tested above works perfectly within Google Pixel of Android 10/11/12. FileProvider is needed when interacting
between HostProfile app and WorkProfile app.

OEM Android Devices' DualApp(or MultiApp, mostly with a UserId `999`) works like WorkProfile(mostly with a UserId `10+`).
But unlike WorkProfile, DualApp MediaStore saves files into path of user `xxx-0` but not `999`. This
causes some problems:

1. WorkProfile App can not directly access files saved by MediaStore itself in OEM devices (Huawei, OPPO confirmed), when query by file path.
2. MediaStore query return missing within WorkProfile app when receiving sharing files from HostProfile app.
3. HostProfile App might get wrong file path, which is actually DualApp's, when query file by MediaStore.

## Demo Apk

[See releases](https://github.com/kaedea/dulapp-mediastore-compatibility/releases)

