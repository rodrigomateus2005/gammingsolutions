package br.com.gammingsolution.command;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class ServeCommand {

    @ShellMethod(key = "serve")
    public String serve(
            @ShellOption(defaultValue = "spring") String arg
    ) {
        return "Hello world " + arg;
    }

}