# DualApp MediaStore APIs Compatibility Test

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
