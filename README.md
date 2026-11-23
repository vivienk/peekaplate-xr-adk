# PeekaPlate · Night Market 2049

Explore a pixel-art night market and see your meal in 3D before you order. Walk through three districts, visit Noka Ramen or Farm House Thai, compare dishes, and select **View Portion** to inspect a featured dish in a native PICO spatial viewer.

**[Open Night Market 2049 in WebXR](https://peekaplate-xr.vercel.app/)**

## Download the submitted PICO build

**App version 0.1.0 · PICO Spatial SDK 6.2.8 · Android package `com.peekaplate.market`**

Sign in to GitHub, download the ZIP, and extract `app-debug.apk`. The APK includes the market and both featured dish models. [Download the submitted build](https://github.com/vivienk/peekaplate-xr-adk/actions/runs/45132484820/artifacts/14798252731) · [Build log](https://github.com/vivienk/peekaplate-xr-adk/actions/runs/45132484820) · [Source at submission](https://github.com/vivienk/peekaplate-xr-adk/tree/a3b00837943f17dac50f0abb336372e4709f3764)

To verify the extracted APK on macOS, run `shasum -a 256 app-debug.apk`. The expected digest is `9bca87c0bcb8561640d49244d7546c3a792ddd2f97ff173aef922cf29a7cb402`.

## Run on the Swan emulator

Start your configured Swan emulator in Android Studio’s **Tools → Device Manager**. Wait for the headset home screen. In Terminal, open the folder containing the extracted APK and run:

```sh
ADB="$HOME/Library/Android/sdk/platform-tools/adb"
"$ADB" devices -l
```

Once one emulator appears with status `device`, install the APK and launch the market:

```sh
"$ADB" -e install -r app-debug.apk &&
"$ADB" -e shell am start -W -n com.peekaplate.market/.GameActivity
```

`GameActivity` opens the full Night Market. Inside the game, **View Portion** opens the native `LaunchActivity` for the selected dish. If installation reports `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, use a clean Swan emulator to preserve data belonging to the differently signed installation.

## Walkthrough

1. Select **Start**, enter Rain Alley, and walk to **Noka Ramen**.
2. Compare two bowls, open **Flaming Steak Ramen**, and select **View Portion**. Check that the ramen appears in 3D and rotates, then return to the market.
3. Visit **Farm House Thai** and repeat with the **fried chicken bowl with sunny egg**. Check that the viewer now shows the chicken bowl and that returning restores the market.

## Verification

The submitted build passed the bundled game tests and APK build in GitHub Actions. The APK checksum and both native dish models were verified. The bundled market also opened in a desktop browser. An end-to-end Swan or headset test of portion rendering, rotation, and return navigation is still pending.
