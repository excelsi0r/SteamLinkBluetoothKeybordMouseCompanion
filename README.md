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

To compile a simple C program we need an `armv7a` compiler. Fortunatly, Valve provides the tools needed for that. Clone the Github, Steam Link SDK, link in references, `cd steamlin-sdk && source setenv.sh` and we have the compiler binaries exported for the current terminal. Now, instead of `gcc` we just need to use `armv7a-cros-linux-gnueabi-gcc`. The glibc folder used by the compiler is located under `steamlink-sdk/toolchain/usr/armv7a-cros-linux-gnuebi/usr/include`. If there is a need to use other headers in the future, they should be stored in that folder. It is the case of the bluez module. But we will talk about that later.

There is no need to compile the C program has a kernel module. However, I will describe how to recompile the kernel which might be needed in the future if someone wishes to replace the Steam Link device current module. This explanation is also featured in the Steam Link SDK Repository. Firstly, extract binaries, `cd steamlin-sdk && source setenv.sh`, this will be needed because the kernel must be compiled for the arm architecture. So when running `make` in the following steps, the armv7a compiler will be used. Second, `export ARCH=arm; export LOCALVERSION="-mrvl"`. Third, `make bg2cd_penguin_mlc_defconfig`. Fourth, `make menuconfig`, the terminal will enter in a configuration graphical interface, you dont need to change anything just save and exit. Fifth, create a empty `Makefile` in all of the following folders (create them if they don't exist): `./kernel/arch/arm/mach-berlin/modules/wlan_sd8787`, `./kernel/arch/arm/mach-berlin/modules/wlan_sd8797`, `./kernel/arch/arm/mach-berlin/modules/wlan_sd8801` and `./kernel/arch/arm/mach-berlin/modules/wlan_sd8897`. These files need to exist because they are missing and the build fails if there are no files there. You might need to install other tools to help build the kernel, `sudo apt-get install git fakeroot build-essential ncurses-dev xz-utils libssl-dev bc`. Sixth and last, hit `make` and wait until the build is successful.

## Steam link used to test information
- Bluetooth Adapter address: E0:31:9E:07:07:66

## References
- Linux Kernel, uinput module: https://www.kernel.org/doc/html/v4.16/input/uinput.html
- Github, Steamlink SDK: https://github.com/ValveSoftware/steamlink-sdk
- Building Linux Kernel: https://medium.freecodecamp.org/building-and-installing-the-latest-linux-kernel-from-source-6d8df5345980
