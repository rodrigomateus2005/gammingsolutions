import 'package:flutter/material.dart';
import 'package:gammingsolutions_app/screens/viewer.dart';
import 'package:gammingsolutions_app/widgets/list_gamepads.dart';

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  final GlobalKey<FormState> _formKey = GlobalKey<FormState>();

  String gamepadIdSelected = '';
  final hostController = TextEditingController();
  final passwordController = TextEditingController();

  void _startViewer() {
    _formKey.currentState?.save();

    Navigator.push(
      context,
      MaterialPageRoute(
          builder: (context) => ViewerPage(
                hostName: hostController.text,
                password: passwordController.text,
                gamepadId: gamepadIdSelected,
              )),
    );
  }

  @override
  Widget build(BuildContext context) {
    // This method is rerun every time setState is called, for instance as done
    // by the _incrementCounter method above.
    //
    // The Flutter framework has been optimized to make rerunning build methods
    // fast, so that you can just rebuild anything that needs updating rather
    // than having to individually change instances of widgets.
    return Scaffold(
      appBar: AppBar(
        // TRY THIS: Try changing the color here to a specific color (to
        // Colors.amber, perhaps?) and trigger a hot reload to see the AppBar
        // change color while the other colors stay the same.
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        // Here we take the value from the MyHomePage object that was created by
        // the App.build method, and use it to set our appbar title.
        title: Text(widget.title),
      ),
      body: Form(
          key: _formKey,
          child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Center(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: <Widget>[
                    TextFormField(
                      controller: hostController,
                      decoration: const InputDecoration(
                        border: UnderlineInputBorder(),
                        labelText: 'Digite o host',
                      ),
                    ),
                    TextFormField(
                      controller: passwordController,
                      decoration: const InputDecoration(
                        border: UnderlineInputBorder(),
                        labelText: 'Digite a senha',
                      ),
                      autocorrect: false,
                      enableSuggestions: false,
                      obscureText: true,
                    ),
                    ListGamepads(
                      onSelected: (value) {
                        setState(() {
                          gamepadIdSelected = value;
                        });
                      },
                    ),
                  ],
                ),
              ))),
      floatingActionButton: FloatingActionButton(
        onPressed: _startViewer,
        tooltip: 'Play',
        child: const Icon(Icons.play_arrow),
      ),
    );
  }
}
