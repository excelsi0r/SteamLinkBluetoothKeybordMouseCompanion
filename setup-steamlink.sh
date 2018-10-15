#! /bin/bash

if [ "$1" ];
then
	sudo apt update &&
	sudo apt install sshpass -y &&

	wget https://github.com/excelsi0r/SteamLinkBluetoothKeybordMouseCompanion/releases/download/v0.2-beta/bluetoothcontroller.o -O bluetoothcontroller.o &&
	sshpass -p 'steamlink123' scp bluetoothcontroller.o root@$1:/home/steam/bin/bluetoothcontroller.o &&
	rm bluetoothcontroller.o &&

	sshpass -p 'steamlink123' ssh root@$1 'echo "#!/bin/sh

	#DEBUG_OPTIONS=-d

	# Set SCO into HCI mode
	hcitool cmd 0x3f 0x1d 0x00

	while true; do
		    echo "Starting bluetoothd: $DEBUG_OPTIONS"
		    /usr/libexec/bluetooth/bluetoothd -E --compat -n $DEBUG_OPTIONS
		    sleep 1
	done
	" &> /etc/init.d/bluetooth.sh' &&

	sshpass -p 'steamlink123' ssh root@$1 'echo "#!/bin/sh

	while true; do
		    echo "Starting bluetoothd: $DEBUG_OPTIONS"
		    /home/steam/bin/bluetoothcontroller.o
		    sleep 1
	done
	" &> /etc/init.d/bluetoothcontroller.sh' &&

	sshpass -p 'steamlink123' ssh root@$1 'echo "#!/bin/sh
	/etc/init.d/bluetoothcontroller.sh &
	" &> /etc/init.d/startup/S14bluetoothcontroller.sh'
else
	echo "Please Specify an IP Address"
fi
