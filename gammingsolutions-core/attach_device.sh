HOST=$1
DEV=$2

modprobe vhci-hcd

usbip attach -r "$HOST" -b "$DEV"