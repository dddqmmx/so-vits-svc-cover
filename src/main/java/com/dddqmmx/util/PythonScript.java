package com.dddqmmx.util;

import java.io.*;

public class PythonScript {
    private Process process;
    private OutputHandler outputHandler;
    private CompletionHandler completionHandler;

    public interface OutputHandler {
        void onOutput(PythonScript sender, String output);
    }

    public interface CompletionHandler {
        void onCompletion(PythonScript sender, int exitCode);
    }

    public PythonScript(File workingDirectory,String pythonPath,String pythonScriptPath) {
        try {
            //String pythonScriptPath = "infer_uvr5.py";
            String command = pythonPath + " " + pythonScriptPath;
            ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", command);
            processBuilder.directory(workingDirectory);
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();

            Thread outputThread = new Thread(() -> {
                BufferedReader reader;
                try {
                    reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            outputThread.start();
            // 等待进程执行完成
            int exitCode = process.waitFor();
            System.out.println("Python脚本执行完毕，退出码：" + exitCode);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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
