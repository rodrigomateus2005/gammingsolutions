package br.com.gammingsolution.command;

import br.com.gammingsolution.service.IAudioService;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.io.IOException;

@AllArgsConstructor
@ShellComponent
public class TesteCommand {

    private final IAudioService audioService;

    @ShellMethod(key = "teste")
    public void serve(
            @ShellOption(defaultValue = "127.0.0.1") String host
    ) throws IOException {
        var content = FileUtils.readFileToByteArray(new File("testeaudio.wav"));
        while (true) {
            audioService.playBytes(content);
        }
    }

}
