import 'dart:async';
import 'dart:convert';
import 'dart:developer';
import 'dart:io';

import 'package:async/async.dart';
import 'package:flutter/material.dart';
import 'package:flutter_rfb/flutter_rfb.dart';
import 'package:gamepads/gamepads.dart';
import 'package:mp_audio_stream/mp_audio_stream.dart';

class ViewerPage extends StatefulWidget {
  const ViewerPage(
      {super.key,
      required this.hostName,
      required this.password,
      required this.gamepadId});

  final String hostName;
  final String password;
  final String gamepadId;

  @override
  State<ViewerPage> createState() => _ViewerPagePageState();
}

class _ViewerPagePageState extends State<ViewerPage> {
  late Socket socketAudio;
  late Socket socketJoypad;
  late AudioStream audioStream;

  @protected
  @mustCallSuper
  @override
  void initState() {
    Gamepads.eventsByGamepad(widget.gamepadId).listen((event) {
      socketJoypad.writeln(jsonEncode(
          {'key': event.key, 'value': event.value, 'type': event.type.index}));
      log(event.key);
      log(event.value.toString());
      log(event.type.name);
    });

    Socket.connect(widget.hostName, 9001).then((Socket sock) {
      socketAudio = sock;
      audioDataHandler(ChunkedStreamReader(socketAudio));
    }).catchError((e) {
      log("Unable to connect: $e");
    });

    Socket.connect(widget.hostName, 9001).then((Socket sock) {
      socketJoypad = sock;
      socketJoypad.listen(joypadDataHandler,
          onError: joypadErrorHandler,
          onDone: joypadDoneHandler,
          cancelOnError: false);
    }).catchError((e) {
      log("Unable to connect: $e");
    });

    startAudioPlayer();

    super.initState();
  }

  Future<void> startAudioPlayer() async {
    audioStream = getAudioStream();
    audioStream.init(channels: 2, sampleRate: 48000);
  }

  Future<void> audioDataHandler(ChunkedStreamReader<int> reader) async {
    while (true) {
      var data = await reader.readBytes(1024 * 50);
      audioStream.push(data.buffer.asFloat32List());
    }
  }

  void joypadDataHandler(data) {
    log(String.fromCharCodes(data).trim());
  }

  void audioErrorHandler(error, StackTrace trace) {
    log(error);
  }

  void joypadErrorHandler(error, StackTrace trace) {
    log(error);
  }

  void audioDoneHandler() {
    socketAudio.destroy();
  }

  void joypadDoneHandler() {
    socketJoypad.destroy();
  }

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          RemoteFrameBufferWidget(
            hostName: widget.hostName,
            port: 5902,
            onError: (final Object error) {
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(
                  content: Text('Error: $error'),
                ),
              );
            },
            password: widget.password,
          ),
        ],
      ),
    );
  }
}
