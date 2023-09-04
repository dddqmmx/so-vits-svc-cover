package com.dddqmmx.util;

import java.io.*;

public class CommandLine {
    private Process process;
    private OutputHandler outputHandler;
    private CompletionHandler completionHandler;

    public interface OutputHandler {
        void onOutput(CommandLine sender, String output);
    }

    public interface CompletionHandler {
        void onCompletion(CommandLine sender, int exitCode);
    }

    public CommandLine(File workingDirectory) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe");
            processBuilder.directory(workingDirectory);
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();

            outputHandler = null;
            completionHandler = null;

            Thread outputThread = new Thread(() -> {
                BufferedReader reader;
                try {
                    reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (outputHandler != null) {
                            outputHandler.onOutput(CommandLine.this, line);
                        }
                    }
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            outputThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setOutputHandler(OutputHandler handler) {
        outputHandler = handler;
    }

    public void setCompletionHandler(CompletionHandler handler) {
        completionHandler = handler;
    }

    public void write(String data) {
        try {
            OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream(), "GBK");
            writer.write(data + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }







    public void waitForCompletion() {
        try {
            process.waitFor();
            if (completionHandler != null) {
                completionHandler.onCompletion(this, process.exitValue());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupted status
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}