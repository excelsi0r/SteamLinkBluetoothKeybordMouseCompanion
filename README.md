# Steam Link Bluetooth Controller
Steam link Bluetooth controller. Support for keyboard mouse and gamepad.

Gamepad and wireless support is currently in development.

## Introduction
The objective of this project is to create a support keyboard, mouse and gamepad to help with steamk link user interaction. The app is to be developed to android in a first initial release and the communication is featured via bluetooth. No need to buy additional mouse, keyboard or second gamepad. No need to always move the peripherals arround. The bluetooth data transfer was chosen because the steam link supports bluetooth. Given that, in some conditions useres might not have wireless connection due to distance reasons. However, bluetooth will always be close to the players becuase a proper close quarters interactions is needed with steam link.

## Setup
This project works also for other linux based distributions, as long as they support Bluetooth.
In the following chapter I will show how to install in SteamLink and Ubuntu.

### SteamLink
Requirments:
 * SteamLink Device
 * MacOs, Linux Machine or Windows with Linux terminal (exapmle: windows 10 with ubuntu from Store)
 
#### 1st Step: Enable Steamlink SSH and retrieve IP address
The SteamLinkSDK repository explains very well how to enable ssh under SteamLink. [Enalbe SSH in Steamlink](https://github.com/ValveSoftware/steamlink-sdk#ssh-access)
Basically: Format a pen drive to FAT32 format, Inside the pen-drive create folder `steamlink`, inside create `config`, inside again create `system` and finally inside create the text file `enable_ssh.txt`. Open the file and write `SteamLink`, save and close it. Now power cycle the device, this means you need to unplug SteamLink from electricity and plug it again, Shutting down from the SteamLink itself it will only put it to sleep and it will not enable SSH.

Now, boot up SteamLink and retrieve the `IP Address` by going down to Settings->Network. If you don't have an IP Address then it means you are not connected to your Home Network. The IP Adress should have the following format: `X.X.X.X` where `X` is a number from 1 to 255.

Write down the IP Adress, we will need it for the next Step.

#### 2nd Step: Run Script by passing IP



### Ubuntu
TODO

### Raspbian
TODO
