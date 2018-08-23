# Steam Link Bluetooth Controller
Steam link Bluetooth controller. Support for keyboard mouse and gamepad

## Introduction
The objective of this project is to create a support keyboard, mouse and gamepad to help with steamk link user interaction. The app is to be developed to android in a first initial release and the communication is featured via bluetooth. No need to buy additional mouse, keyboard or second gamepad. No need to always move the peripherals arround. The bluetooth data transfer was choosen because the steam link supports bluetooth. Given that, in some conditions useres might not have wireless connection due to distance reasons. However, bluetooth will always be close to the players becuase a proper close quarters interactions is needed with steam link.

## Proof of conecpt
In order to test if this project could be developed it required different proto-testing phases. These phases are:

- Test virtual user input via uinput module in ubuntu
- Test virtual user input via uinput module in steamlink
- Test bluetooth data transfer via ubunutu (C server) and ubuntu (C client)
- Test bluetooth data transfer via steamlink (C server) and ubuntu (C client)
- Test bluetooth data transfer via steamlink (C server) and android (Android client)

Each phase was successful proving that the Application can be planned, designed and developed.

## Setup 
The project setup and compilation proved to be quite the challenge. Multiple modules needed to be configured in order to be possible to compile the multiple prototypes used during the proof of concept phase. 

First of all the steamlink is differnt from common PC/laptop. While a common computer uses the `x86_64` architecture, Steam Link uses a `ARMv7` architecture. More detailed a `ARMv7a` architecture. Additional packages or development tools might need to be downloaded and installed manually under an architecture code `armhf`. I uses a Linux firmware called `3.8.13-mrvl` based on the `3.8` version. In order to compile, the gnu C library `2.19` is used. This library has a different folder structure from the current release. Given that, some headers in C code might need to be updated accordingly.

To develop steamlink terminal apps we need ssh acces to it. The SteamLink SDK github provides a tutorial to enable ssh connection in steamlink. This setup chapter assumes you have already enabled ssh connection to your steamlink.

To compile a simple C program we need an `armv7a` compiler. Fortunatly, Valve provides the tools needed for that. Clone the Github, Steam Link SDK, link in references, `cd steamlin-sdk && source setenv.sh` and we have the compiler binaries exported for the current terminal. Now, instead of `gcc` we just need to use `armv7a-cros-linux-gnueabi-gcc`. The glibc folder used by the compiler is located under `steamlink-sdk/toolchain/usr/armv7a-cros-linux-gnuebi/usr/include`. If there is a need to use other headers in the future, they should be stored in that folder. It is the case of the bluez module. But we will talk about that later.

There is no need to compile the C program has a kernel module. However, I will describe how to recompile the kernel which might be needed in the future if someone wishes to replace the Steam Link device current module. This explanation is also featured in the Steam Link SDK Repository. Firstly, extract binaries, `cd steamlin-sdk && source setenv.sh`, this will be needed because the kernel must be compiled for the arm architecture. So when running `make` in the following steps, the armv7a compiler will be used. Second, `export ARCH=arm; export LOCALVERSION="-mrvl"`. Third, `make bg2cd_penguin_mlc_defconfig`. Fourth, `make menuconfig`, the terminal will enter in a configuration graphical interface, you dont need to change anything just save and exit. Fifth, create a empty `Makefile` in all of the following folders (create them if they don't exist): `./kernel/arch/arm/mach-berlin/modules/wlan_sd8787`, `./kernel/arch/arm/mach-berlin/modules/wlan_sd8797`, `./kernel/arch/arm/mach-berlin/modules/wlan_sd8801` and `./kernel/arch/arm/mach-berlin/modules/wlan_sd8897`. These files need to exist because they are missing and the build fails if there are no files there. You might need to install other tools to help build the kernel, `sudo apt-get install git fakeroot build-essential ncurses-dev xz-utils libssl-dev bc`. Sixth and last, hit `make` and wait until the build is successful.

For bluetooth development we are going to use the offical Linux BlueZ library for steamlink. BlueZ is installed in steamlink but not the terminal tools. However, there are some bluetooth tools natively installed in steamlink that are more than enough to support bluetooth for this Application. First of all, the compiler we need to use does not have the `libbluetooth-dev` installed which is mandatory to have in order to develop bluetooth applications in C for steamlink. For that we need to manually download and install the `libbluetoot-dev` package. Given that we have a `armv7a` cpu and the BlueZ version is `5.43`, we need to download that exact package. Link in the references. After we have successfully downloaded it, we need to extract the debian package properly. So, `dpkg -x libbluetooth-dev_5.43-2+deb9u1_armhf.deb .` Copy the folder `bluetooth`, located in `libbluetooth/usr/include/`, to the toolchain glibc folder: `steamlink-sdk/toolchain/usr/armv7a-cros-linux-gnuebi/usr/include`. Now the `armv7a` compiler will be able to locate the bluetooth headers referenced. Finally when compiling don't forget to include the libbluetooth static library, which is the file `libbluetooth/usr/lib/arm-linux-gnueabihf/libbluetooth.a`. You can place this file anoy place you want as long as it is referenced when compiling the C program. And that is it, that should do it for C bluetooth apps compiltation for steamlink. A simple C bluetooth server in steamlink should run, connect and transfer data with a simple C client in Ubuntu. 

Unfortunatly, in order to transfer data between steamlink C server and Android java client we need to configure a SDP (Service Discovery Protocol) in steamlink. The C code is simple to write, however BlueZ no longer supports SDP by default. So, in order to make it work we need to configure steamlink to run BlueZ in compatibility mode. For that, inside steamlink terminal, we will lock for the file `/etc/init.d/bluetooth.sh`, open the file in vi, and change the line `/usr/libexec/bluetooth/bluetoothd -E -n $DEBUG_OPTIONS` to `/usr/libexec/bluetooth/bluetoothd -E --compat -n $DEBUG_OPTIONS`. You might need some experience in order to use vi. Once you save and exit the steamlink should restart by itself, if not then power cycle the device. Once de device is up, hop into the terminal again and execute `chmod 777 /var/run/sdp`. This will change the file permissions in order to register the sdp. This setup is necessary because if you tried to register the sdp in your C application you would get `segmetation fault (core dumped)` in the `sdp_record_register()` line.

When Android 4.2 came out, the Bluetooth stack was completely revamped. The casual methods to create a RFCOMM connection no longer work and workarrounds are needed. So instead of calling a simple `createRfcommSocketToServiceRecord()` call we need to create a fallback socket using: `btSocket =(BluetoothSocket) btDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(btDevice,channel);` where `btSocket` represent a BluetoothSocket class, `btDevice` represents the device we want to connect with and `channel` represents the channel which the SDP protocol is running. This channel needs to be same number also registered in the C program server in steamlink. Do not mistake socket descriptor for channel. Socket descriptor is arbitrarily choosen, while the rfcomm sdp channel is not. I found that using the channel `11` works very well to register the sdp. In the description there is a link explaining the problem with the change of the Bluetooth stack in Android.

In order for steamlink and android exchange data via bluetooth, they need to be paired. It is a requirement of steamlink to only exchange data between know devices. Steamlink provides some tools that can help pair the yout device. 

## Steam link MAC used to test information
- Bluetooth Adapter address: E0:31:9E:07:07:66

## References
- Linux Kernel, uinput module: https://www.kernel.org/doc/html/v4.16/input/uinput.html
- Github, Steamlink SDK: https://github.com/ValveSoftware/steamlink-sdk
- Building Linux Kernel: https://medium.freecodecamp.org/building-and-installing-the-latest-linux-kernel-from-source-6d8df5345980
- ARM architecture differences: https://en.wikipedia.org/wiki/ARM_architecture
- BlueZ 5.43, libbluetooth-dev for armhf: https://packages.debian.org/stretch/armhf/libbluetooth-dev/download
- Extract Debian package: https://linux-tips.com/t/how-to-extract-deb-package/169
- Bluetooth programming in C: https://people.csail.mit.edu/albert/bluez-intro/c404.html
- Bluetooth RFCOMM sockets in C: https://people.csail.mit.edu/albert/bluez-intro/x502.html
- SDP register core dumped issue: https://gist.github.com/yan12125/61c13eeb3a8a8472705b
- SDP register core dumped solution: https://raspberrypi.stackexchange.com/questions/41776/failed-to-connect-to-sdp-server-on-ffffff000000-no-such-file-or-directory
- Bluetooth SDP in C: https://people.csail.mit.edu/albert/bluez-intro/x604.html
- Vi tutorial: https://www.tutorialspoint.com/unix/unix-vi-editor.htm
- Bluetooth Android Overview: https://developer.android.com/guide/topics/connectivity/bluetooth
- Bluetooth Android stack workarround: https://stackoverflow.com/questions/18657427/ioexception-read-failed-socket-might-closed-bluetooth-on-android-4-3
