# DualApp MediaStore APIs Compatibility Test
[![CircleCI](https://circleci.com/gh/kaedea/dulapp-mediastore-compatibility.svg?style=shield&circle-token=0408c3e1410b8176b0e9ff44e6ecd0cb2ed45c7b)](#)

Tuning of Android MediaStore & File IO compatibility issues between HostProfile app and
WorkProfile app (DualApp/ParallelApp), especially for OEM Android Devices.

## Enable WorkProfile for DualApp

1. Create Profile
> adb shell pm create-user --profileOf 0 --managed Island
> adb -d shell pm list users
> adb -d shell settings --user <user_id> put secure install_non_market_apps 1

2. Install apk to Profile
> adb push <apk_path> /data/local/tmp/android.apk
> adb shell pm install -r -t --user <user_id> /data/local/tmp/android.apk

## Test Cases

1. Read & Write file by MediaStore within DualApp.
2. File Uri/FilePath Convert within DualApp.
3. Handle sharing file  within DualApp.

## Known Problems

1. IO works perfectly within Google Pixel of Android 10/11/12. FileProvider is needed when interacting
between HostProfile app and WorkProfile app.
2. WorkProfile app can not access files saved by MediaStore itself with OEM devices (Huawei, OPPO confirmed).
3. MediaStore query return missing within WorkProfile app when receiving sharing files from HostProfile app.
4. It seems that those OEM WorkProfile app save MediaStore IO data to HostProfile's MediaStore db, who knows.

## Demo Apk

[See releases](https://github.com/kaedea/dulapp-mediastore-compatibility/releases)

