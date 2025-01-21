export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

gcc -g \
    -fPIC \
    -shared \
    -o virtual_gamepad.so device_bridge.c \
    -I"$JAVA_HOME/include" \
    -I"$JAVA_HOME/include/linux/" \
    -I"." \
    -I"./include" \
    -I'/usr/include/libevdev-1.0' \
    -D_REENTRANT \
    -levdev