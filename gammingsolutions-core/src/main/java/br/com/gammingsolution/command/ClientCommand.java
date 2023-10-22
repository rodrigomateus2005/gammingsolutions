package br.com.gammingsolution.command;

import br.com.gammingsolution.controller.ClientController;
import br.com.gammingsolution.controller.ServerController;
import lombok.AllArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@AllArgsConstructor
@ShellComponent
public class ClientCommand {

    private final ClientController clientController;

    @ShellMethod(key = "connect")
    public void serve(
            @ShellOption(defaultValue = "spring") String arg
    ) {
        clientController.connect();
    }

}
