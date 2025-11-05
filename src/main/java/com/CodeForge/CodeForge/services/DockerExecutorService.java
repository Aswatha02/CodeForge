package com.CodeForge.CodeForge.services;

import java.io.*;
import java.nio.file.*;
import java.util.stream.Collectors;

public class DockerExecutorService {

    public String runSubmission(String code, String language) {
        Path codeDir = null;
        try {
            // Create temporary folder in OS temp dir
            Path baseDir = Paths.get(System.getProperty("java.io.tmpdir"));
            codeDir = Files.createTempDirectory(baseDir, "code");

            // Write user code to appropriate file
            Path codeFile;
            switch (language.toLowerCase()) {
                case "java":
                    codeFile = codeDir.resolve("Main.java");
                    break;
                case "python":
                    codeFile = codeDir.resolve("main.py");
                    break;
                case "cpp":
                    codeFile = codeDir.resolve("main.cpp");
                    break;
                default:
                    return "Unsupported language: " + language;
            }
            Files.writeString(codeFile, code);

            // Execute inside Docker
            return executeInDocker(codeDir, language);

        } catch (Exception e) {
            e.printStackTrace();
            return "Execution failed: " + e.getMessage();
        } finally {
            // Cleanup after execution
            if (codeDir != null) {
                try {
                    Thread.sleep(200); // ensure Docker released the folder
                    Files.walk(codeDir)
                         .sorted((a, b) -> b.compareTo(a))
                         .map(Path::toFile)
                         .forEach(File::delete);
                } catch (Exception ignored) {}
            }
        }
    }

    private String executeInDocker(Path codeDir, String language) throws IOException, InterruptedException {
        // Fix Windows path -> Docker Desktop compatible path
        String dockerPath = codeDir.toAbsolutePath().toString()
                .replace("\\", "/")
                .replaceFirst("^(?i)([a-z]):", "/host_mnt/$1");

        String dockerCommand;
        switch (language.toLowerCase()) {
            case "java":
                dockerCommand = String.format(
                    "docker run --rm --memory 256m --cpus 0.5 --user root " +
                    "-v \"%s:/code\" openjdk:17-slim sh -c \"cd /code && javac *.java && java Main\"",
                    dockerPath
                );
                break;

            case "python":
                dockerCommand = String.format(
                    "docker run --rm --memory 256m --cpus 0.5 --user root " +
                    "-v \"%s:/code\" python:3.9-slim sh -c \"cd /code && python3 main.py\"",
                    dockerPath
                );
                break;

            case "cpp":
                dockerCommand = String.format(
                    "docker run --rm --memory 256m --cpus 0.5 --user root " +
                    "-v \"%s:/code\" gcc:latest sh -c \"cd /code && g++ main.cpp -o main && ./main\"",
                    dockerPath
                );
                break;

            default:
                throw new IllegalArgumentException("Unsupported language: " + language);
        }

        // Use CMD on Windows, Bash on Linux/Mac
        ProcessBuilder builder;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            builder = new ProcessBuilder("cmd.exe", "/c", dockerCommand);
        } else {
            builder = new ProcessBuilder("bash", "-c", dockerCommand);
        }

        builder.redirectErrorStream(true);
        Process process = builder.start();

        String output;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            output = reader.lines().collect(Collectors.joining("\n"));
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) output += "\n[Process exited with code " + exitCode + "]";
        return output;
    }
}
