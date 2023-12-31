package br.com.gammingsolution.command;

import br.com.gammingsolution.controller.ServerController;
import br.com.gammingsolution.service.IUsbService;
import lombok.AllArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.stream.Collectors;

@AllArgsConstructor
@ShellComponent
public class ServeCommand {

    private final ServerController serverController;

    @ShellMethod(key = "serve")
    public void serve(
            @ShellOption(defaultValue = "spring") String arg
    ) {
        serverController.start();
    }

}