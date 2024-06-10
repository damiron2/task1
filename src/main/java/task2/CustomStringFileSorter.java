package task2;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomStringFileSorter {
    private List<String> list;
    private Path pathOfUnsorted;
    private Path pathOfSorted;
    private final CustomStringComparator stringComparator = new CustomStringComparator();

    public CustomStringFileSorter(Path pathOfUnsorted, Path pathOfSorted) {
        this.pathOfUnsorted = pathOfUnsorted;
        this.pathOfSorted = pathOfSorted;
    }

    public void inMemSort() {
        List<String> list = new ArrayList<>();
        try (
        BufferedReader reader =  Files.newBufferedReader(pathOfUnsorted);
        BufferedWriter writer = Files.newBufferedWriter(pathOfSorted)) {
            String line;
            while ((line = reader.readLine())!=null){
                list.add(line);
            }
            list.sort(stringComparator);
            for (int i = 0; i < list.size(); i++) {
                writer.write(list.get(i));
                writer.newLine();
            }
        } catch (IOException e) {
            e.getMessage();
        }

    }

    public List<Path> split() {
        List<Path> tmpFiles = new ArrayList<>();
        try {
            Path tempDir = Files.createTempDirectory(Paths.get(System.getProperty("user.dir")), "tmp");
            try (BufferedReader reader = Files.newBufferedReader(pathOfUnsorted)) {
                int fileIndex = 0;
                long maxFileSize = 50 * 1024 * 1024; // 10 MB in bytes
                long currentFileSize = 0;
                Path path = Files.createTempFile(tempDir, fileIndex + "_tmp","");
                BufferedWriter writer = Files.newBufferedWriter(path);
                String line;
                tmpFiles.add(path);
                while ((line = reader.readLine()) != null) {
                    long lineSize = line.getBytes().length + System.lineSeparator().getBytes().length;
                    if (currentFileSize + lineSize > maxFileSize) {
                        writer.close();
                        fileIndex++;
                        path = Files.createTempFile(tempDir, fileIndex + "_tmp", "");
                        writer = Files.newBufferedWriter(path);
                        tmpFiles.add(path);
                        currentFileSize = 0;
                    }
                    writer.write(line);
                    writer.newLine();
                    currentFileSize += lineSize;
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tmpFiles;
    }

    public List<Path> sort (List<Path> pathsOfUnsortedTmpFiles) throws IOException {
        List<Path> pathsOfSorted = new ArrayList<>();
        Path tempDir = Files.createTempDirectory(Paths.get(System.getProperty("user.dir")), "tmp_sorted");
        for (Path path : pathsOfUnsortedTmpFiles) {
            Path pathTmpSorted = Files.createTempFile(tempDir, path.getFileName() + "_sorted", ".txt");
            BufferedWriter writer = Files.newBufferedWriter(pathTmpSorted);
            pathsOfSorted.add(pathTmpSorted);
            list = Files.readAllLines(path);
            list.sort(stringComparator);
            for (String s : list) {
                writer.write(s);
                writer.newLine();
            }
            writer.close();
            Files.deleteIfExists(path);
        }
        Files.deleteIfExists(pathsOfUnsortedTmpFiles.get(0).getParent());
        return pathsOfSorted;
    }

    public void merge(List<Path> pathOfSortedTmp) throws IOException {
        List<BufferedReader> readers = new ArrayList<>();
        for (Path path : pathOfSortedTmp) {
            BufferedReader reader = Files.newBufferedReader(path);
            readers.add(reader);
        }

        PriorityQueue<String> pq = new PriorityQueue<>(stringComparator);
        for (BufferedReader reader : readers) {
            String line = reader.readLine();
            if (line != null) {
                pq.add(line);
            }
        }

        BufferedWriter bw = Files.newBufferedWriter(pathOfSorted);
        while (!pq.isEmpty()) {
            String line = pq.poll();
            bw.write(line);
            bw.newLine();

            for (int i = 0; i < readers.size(); i++) {
                BufferedReader reader = readers.get(i);
                String nextLine = reader.readLine();
                if (nextLine != null) {
                    pq.add(nextLine);
                } else {
                    reader.close();
                    readers.remove(i);
                    i--;
                }
            }
        }

        bw.close();
    }

}
