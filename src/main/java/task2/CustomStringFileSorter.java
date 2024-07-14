package task2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CustomStringFileSorter {
    private final Path pathOfUnsorted;
    private final Path pathOfSorted;
    private final CustomStringComparator stringComparator = new CustomStringComparator();

    public CustomStringFileSorter(Path pathOfUnsorted, Path pathOfSorted) {
        this.pathOfUnsorted = pathOfUnsorted;
        this.pathOfSorted = pathOfSorted;
    }


    public void split() throws IOException, InterruptedException {
        List<Path> tmpFiles = new ArrayList<>();
        try {
            Path tempDir = Files.createTempDirectory(Paths.get(System.getProperty("user.dir")), "tmp");
            try (BufferedReader reader = Files.newBufferedReader(pathOfUnsorted)) {
                int fileIndex = 0;
                long maxFileSize = 50 * 1024 * 1024; // 50 MB in bytes
                long currentFileSize = 0;
                Path path = Files.createTempFile(tempDir, fileIndex + "_tmp", "");
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
        //sort(tmpFiles);
        multithreadSort(tmpFiles);
    }

    public void sort(List<Path> pathsOfUnsortedTmpFiles) throws IOException {
        List<Path> pathsOfSorted = new ArrayList<>();
        Path tempDir = Files.createTempDirectory(Paths.get(System.getProperty("user.dir")), "tmp_sorted");
        for (Path path : pathsOfUnsortedTmpFiles) {
            Path pathTmpSorted = Files.createTempFile(tempDir, path.getFileName() + "_sorted", ".txt");
            pathsOfSorted.add(pathTmpSorted);
            inMemSort(path, pathTmpSorted);
            Files.deleteIfExists(path);
        }
        Files.deleteIfExists(pathsOfUnsortedTmpFiles.get(0).getParent());
        merge(pathsOfSorted);
    }

    public void multithreadSort(List<Path> pathsOfUnsortedTmpFiles) throws IOException, InterruptedException {
        List<Path> pathsOfSorted = new ArrayList<>();
        Path tempDir = Files.createTempDirectory(Paths.get(System.getProperty("user.dir")), "tmp_sorted");

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        for (Path path : pathsOfUnsortedTmpFiles) {
            Path pathOfSortedTmp = Files.createTempFile(tempDir, tempDir.getFileName() + "_sorted", ".txt");
            executorService.execute(new Thread(() -> {
                try {
                    inMemSort(path, pathOfSortedTmp);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
            pathsOfSorted.add(pathOfSortedTmp);
        }
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.DAYS);
    }

    public void inMemSort(Path pathOfUnsorted, Path pathOfSorted) throws IOException {
        List<String> list = new ArrayList<>();
        Files.lines(pathOfUnsorted).filter(string -> !string.isEmpty()).forEach(list::add);
        list.sort(stringComparator);
        Files.write(pathOfSorted, list);
    }

    public void merge(List<Path> pathOfSortedTmp) throws IOException {
        long start = System.currentTimeMillis();
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
        long finish = System.currentTimeMillis();
        System.out.println((finish - start) / 1000 + " sec merged");
    }
}
