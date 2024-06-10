package task2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class CustomStringFileWriter {

    private Path path;
    private final StringGenerator stringGenerator;
    private int generatedFileSize;

    public CustomStringFileWriter(Path path, StringGenerator stringGenerator, int generatedFileSize) {
        this.path = path;
        this.stringGenerator = stringGenerator;
        this.generatedFileSize = generatedFileSize;
    }

    public long write() {
        long fileSize = 0;
        long stringCounter = 0;
        try (BufferedWriter fileWriter = Files.newBufferedWriter(path)) {
            while (fileSize < 1024 * 1024 * generatedFileSize) {
                List<String> pairForWrite = stringGenerator.generateRandomString();
                if (pairForWrite.get(0).length() < 20 || pairForWrite.get(1).length()<20){
                    System.out.println(pairForWrite.get(0).length());
                    System.out.println(pairForWrite.get(1).length());
                }

                fileWriter.write(pairForWrite.get(0));
                fileWriter.newLine();
                //Записываем вторую идентичную строку для выполнения условия задания - в файле должны быть дубли строк
                fileWriter.write(pairForWrite.get(1));
                fileWriter.newLine();
                fileSize+=(pairForWrite.get(0).getBytes().length + pairForWrite.get(1).getBytes().length + System.lineSeparator().length());
                stringCounter+=2;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringCounter;
    }
}
