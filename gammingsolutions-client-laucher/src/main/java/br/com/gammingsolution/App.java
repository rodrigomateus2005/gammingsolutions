package br.com.gammingsolution;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import javax.print.DocFlavor;
import javax.swing.*;
import java.io.*;
import java.util.concurrent.TimeUnit;

public class App {
    public static void main(String[] args) throws InterruptedException {
        String pathLocal = args[0];
        String host = args[1];
        String user = args[2];
        String password = JOptionPane.showInputDialog("Digite a senha");

        Thread threadSSH = startSSHSession(host, user, password, "~/games-scripts/start-game-server.sh", 5902);
        Thread.sleep(5000);
        Thread threadVnc = executeCmdThread("xtigervncviewer -CompressLevel 9 -QualityLevel 1 -AutoSelect 0 localhost:5902");
        Thread threadConnector = executeCmdThread("sudo " + pathLocal + "/execute-java-client.sh " + host);

        threadVnc.join();
        threadSSH.interrupt();
        threadConnector.interrupt();
    }


    private static Thread startSSHSession(String host, String user, String password, String command, int port) {
        Thread t = new Thread() {
            Session session = null;
            ChannelExec channel = null;

            @Override
            public void run() {
                try {
                    session = new JSch().getSession(user, host);
                    session.setPortForwardingL(port, "localhost", port);
                    session.setPassword(password);
                    session.setConfig("StrictHostKeyChecking", "no");
                    session.connect();

                    channel = (ChannelExec) session.openChannel("exec");
                    channel.setCommand(command);
                    channel.connect();

                    while (channel.isConnected()) {
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    disconnect();
                }
            }

            private void disconnect() {
                if (session != null && session.isConnected()) {
                    session.disconnect();
                }
                if (channel != null && channel.isConnected()) {
                    channel.disconnect();
                }
            }

            @Override
            public void interrupt() {
                disconnect();
                super.interrupt();
            }
        };
        t.start();
        return t;
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