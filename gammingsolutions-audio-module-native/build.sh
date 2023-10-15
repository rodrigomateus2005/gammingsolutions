export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

javac -h . PipewireAudioBridge.java

javap -s PipewireAudioBridge

gcc -g \
    -fPIC \
    -shared \
    -o libpipewire_bridge.so pipewire_bridge.c \
    -I"$JAVA_HOME/include" \
    -I"$JAVA_HOME/include/linux/" \
    -I"." \
    -I'/usr/include/pipewire-0.3' \
    -I'/usr/include/spa-0.2' \
    -D_REENTRANT \
    -lpipewire-0.3 \
    -lm