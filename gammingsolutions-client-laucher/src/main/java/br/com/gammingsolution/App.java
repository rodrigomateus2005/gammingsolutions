package br.com.gammingsolution;

import javax.print.DocFlavor;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class App {
    public static void main(String[] args) throws InterruptedException {

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