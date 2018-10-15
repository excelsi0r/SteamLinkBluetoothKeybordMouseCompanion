#! /bin/bash

wget https://github.com/excelsi0r/SteamLinkBluetoothKeybordMouseCompanion/releases/download/v0.2-beta/bluetoothcontroller-x80_86.o -O bluetoothcontroller.o &&
sudo cp bluetoothcontroller.o /usr/local/bin/bluetoothcontroller.o &&
sudo chmod +x /usr/local/bin/bluetoothcontroller.o &&
rm bluetoothcontroller.o &&
sudo sed -i 's;ExecStart=/usr/lib/bluetooth/bluetoothd;ExecStart=/usr/lib/bluetooth/bluetoothd --compat/g' /etc/systemd/system/dbus-org.bluez.service &&
sudo echo "#! /bin/sh
# /etc/init.d/noip
### BEGIN INIT INFO
# Provides:          noip
# Required-Start:    $remote_fs $syslog
# Required-Stop:     $remote_fs $syslog
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Simple script to start a program at boot
# Description:       A simple script from www.stuffaboutcode.com which will sta$
### END INIT INFO
# If you want a command to always run, put it here
# Carry out specific functions when asked to by the system

case $1 in
  start)
    echo \"Starting BluetoothController\"
    /usr/local/bin/bluetoothcontroller.o &
    echo \"Please Run\"
  ;;
  stop)
    echo \"Stopping BluetoothController\"
    pkill bluetoothcontro
  ;;
  restart)
    echo \"Restarting BluetoothController\"
    pkill bluetoothcontro
    /usr/local/bin/bluetoothcontroller.o &
  ;;
  *)
	echo \"Usage: /etc/init.d/bluetoothcontroller { start \| stop \|  restart }\"
    exit 1
  ;;
esac

exit 0

# vim:noet
" &> /etc/init.d/bluetoothcontroller &&
sudo chmod +x /etc/init.d/bluetoothcontroller &&
sudo ln -s /etc/init.d/bluetoothcontroller /etc/rc2.d/S02bluetoothcontroller