package br.com.gammingsolution;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class App {
    public static void main(String[] args) throws InterruptedException {
        String pathJar = args.length > 0 ? args[0] : "~/games-scripts";
        String homeDir = System.getProperty("user.home");

        var isStartedXorg = ProcessHandle
                .allProcesses()
                .anyMatch(x -> x.info().commandLine().orElse("").trim().equals("/usr/lib/xorg/Xorg vt3 :2"));
        int argChvt = isStartedXorg ? 0 : 1;


        Thread threadServer = executeCmdThread("java -jar " + pathJar + "/peripherals-connector-1.0-SNAPSHOT.jar serve");
        Thread threadChangeAndStartX = null;
        threadChangeAndStartX = executeCmdThread("sudo " + pathJar + "/change-vt-game.sh " + argChvt);

        Thread.sleep(2000);
        Thread threadRetroArch = executeCmdThread("/opt/RetroArch/RetroArch-Linux-x86_64/RetroArch-Linux-x86_64.AppImage -fg", new String[]{"XDG_RUNTIME_DIR=/run/user/1000", "HOME=" + homeDir,"DISPLAY=:2"});
        // add -localhost no, to open on network. And --I-KNOW-THIS-IS-INSECURE if -SecurityTypes None
        Thread threadVnc = executeCmdThread("X0tigervnc -display :2 -AcceptSetDesktopSize=0 -localhost=1 -desktop Game -rfbport 5902 -SecurityTypes None");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    threadRetroArch.interrupt();
                } catch (Exception ignored) {
                }
            }
        });

        threadRetroArch.setName("RetroArch");
        threadRetroArch.join();
        threadServer.interrupt();
        threadVnc.interrupt();
        threadChangeAndStartX.interrupt();

    }

    private static Thread executeCmdThread(final String cmd) {
        return executeCmdThread(cmd, null);
    }

    private static Thread executeCmdThread(final String cmd, final String[] envp) {
        Thread t = new Thread() {
            private Process process;

            @Override
            public void run() {
                process = executeCmd(cmd, envp);
                try {
                    process.waitFor();
                } catch (Exception ignored) {
                }
            }

            @Override
            public void interrupt() {
                process.destroyForcibly();
                super.interrupt();
            }
        };
        t.start();
        return t;
    }

    private static Process executeCmd(String cmd, String[] envp) {
        try {
            return Runtime.getRuntime().exec(cmd, envp);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}