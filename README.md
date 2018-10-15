# Steam Link Bluetooth Keyboard and Mouse Companion
Steam link Bluetooth Keyboard and Mouse Companion. Support for keyboard mouse.

Wireless support is currently in development.

## Introduction
The objective of this project is to create a support keyboard, mouse and gamepad to help with steamk link user interaction. The app is to be developed to android in a first initial release and the communication is featured via bluetooth. No need to buy additional mouse, keyboard or second gamepad. No need to always move the peripherals arround. The bluetooth data transfer was chosen because the steam link supports bluetooth. Given that, in some conditions useres might not have wireless connection due to distance reasons. However, bluetooth will always be close to the players becuase a proper close quarters interactions is needed with steam link.

## Setup
This project works also for other linux based distributions, as long as they support Bluetooth.
In the following chapter I will show how to install in SteamLink and Ubuntu.

## SteamLink
#### Requirments:
 * SteamLink Device
 * MacOs, Linux Machine or Windows with Linux terminal (example: windows 10 with ubuntu from Store)
 
#### 1st Step: Enable Steamlink SSH and retrieve IP address
The SteamLinkSDK repository explains very well how to enable ssh under SteamLink. 

[Enalbe SSH in Steamlink](https://github.com/ValveSoftware/steamlink-sdk#ssh-access)

Basically: Format a pen drive to FAT32 format, Inside the pen-drive create folder `steamlink`, inside create `config`, inside again create `system` and finally inside create the text file `enable_ssh.txt`. Open the file and write `SteamLink`, save and close it. Now power cycle the device, this means you need to unplug SteamLink from electricity and plug it again, Shutting down from the SteamLink itself it will only put it to sleep and it will not enable SSH. Do not change the password.

Now, boot up SteamLink and retrieve the `IP Address` by going down to Settings->Network. If you don't have an IP Address then it means you are not connected to your Home Network. The IP Adress should have the following format: `X.X.X.X` where `X` is a number from 1 to 255.

Write down the IP Adress, we will need it for the next Step.

#### 2nd Step: Pair SteamLink with your android device
First, Download the Client App from the Play Store, [SteamLink Keyboard Mouse Companion](), open and notice the underlined number by the format `XX:XX:XX:XX:XX:XX` that is your device MAC Address, note it down we are gonna need that later.

Since SteamLink does not provide a proper UI for the users to pair devices, we have to pair via terminal.
Open a SSH connection from termianl `ssh root@<STEAMLINK_IP_ADDRESS>`. Don't forget to change <STEAMLINK_IP_ADDRESS> for the real address that you retrieved last step.

Now, follow the Tutorial on the Wiki to pair the device, in the last step you will need the MAC address that you wrote down in the beginning of this step. [Bluetooth Pairing With SteamLink](https://github.com/excelsi0r/SteamLinkBluetoothKeybordMouseCompanion/wiki/Research#bluetooth-pairing-with-steamlink)

#### 3rd Step: Run Script by passing IP
Download the script [SteamLink Setup Script](https://github.com/excelsi0r/SteamLinkBluetoothKeybordMouseCompanion/releases/download/v0.2-beta/setup-steamlink.sh)

Run the script with your steamlink IP Address. Ex: `./setup-steamlink.sh 192.168.1.105`

After the script finishes running, power cycle the SteamLink, unplug from electricity and plug again.

#### 4th Step: Run Android App
Open android app, press Connect and select the steamlink in the list of paired devices.

## Raspbian
#### Requirments:
 * Raspbian Device (Raspberry Pi)
 * MacOs, Linux Machine or Windows with Linux terminal (example: windows 10 with ubuntu from Store)

#### 1st Step: Pair with Raspberry
Raspbian provides a easy user interaction in order to pair devices. The first thing you should do is pair Raspbian with your device. If you are unfamiliar just search in Google and tons of tutorials will show up.

#### 2nd Step: Run Script
Run the Provided script in order to install the daemon server in Raspbian. [Raspbian Setup Script](https://github.com/excelsi0r/SteamLinkBluetoothKeybordMouseCompanion/releases/download/v0.2-beta/setup-raspbian.sh)

I will assume you are inside Raspbian terminal. So, just run the script `sudo ./setup-rapbian.sh`

Oncd it finishes, reboot Raspbian.

#### 3rd: Run Android App
Open android app, press Connect and select the rasperrypi in the list of paired devices.
