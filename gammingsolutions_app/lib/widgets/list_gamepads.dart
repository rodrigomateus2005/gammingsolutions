
import 'dart:collection';

import 'package:flutter/material.dart';
import 'package:gamepads/gamepads.dart';

typedef MenuEntry = DropdownMenuEntry<String>;

class ListGamepads extends StatefulWidget {
  final ValueChanged<String>? onSelected;

  const ListGamepads({super.key, this.onSelected});
  
  @override
  State<StatefulWidget> createState() => _ListGamepads();
}

class _ListGamepads extends State<ListGamepads> {

  String dropdownValue = '';

  Future<List<GamepadController>> loadGamepads() async {
    return await Gamepads.list();
  }

  @override
  Widget build(context) {
    return FutureBuilder<List<GamepadController>>(
      future: loadGamepads(),
      builder: (context, AsyncSnapshot<List<GamepadController>> snapshot) {
        if (snapshot.hasData) {
          final List<MenuEntry> menuEntries = UnmodifiableListView<MenuEntry>(
            snapshot.data!.map<MenuEntry>((GamepadController controller) => MenuEntry(value: controller.id, label: '${controller.name} (${controller.id})')),
          );

          return DropdownMenu<String>(
            onSelected: (String? value) {
              setState(() {
                dropdownValue = value!;
              });

              widget.onSelected!(value!);
            },
            dropdownMenuEntries: menuEntries,
          );
        } else {
          return const CircularProgressIndicator();
        }
      }
    );
  }
}