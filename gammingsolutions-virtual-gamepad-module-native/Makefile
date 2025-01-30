JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
CC= gcc
OPTIONS=-g -fPIC -shared

BIN_DIR= ./bin
BIN=${BIN_DIR}/libvirtualgamepad_bridge.so

SRC_DIRS= ./src
SRCS= ${SRC_DIRS}/device_bridge.c

INCLUDES= -I"${JAVA_HOME}/include" \
    -I"${JAVA_HOME}/include/linux/" \
    -I"${SRC_DIRS}/." \
	-I"${SRC_DIRS}/include" \
    $(shell pkgconf --cflags --libs libevdev)

all: prebuild build

prebuild:
	${shell rm -rf ${BIN_DIR}}
	${shell mkdir bin}

build:
	${CC} ${OPTIONS} ${SRCS} -o ${BIN} ${INCLUDES}
