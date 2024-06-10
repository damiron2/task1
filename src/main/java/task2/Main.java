package task2;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {

        System.out.println("Specify the file size in MB");
        Scanner scanner = new Scanner(System.in);
        int generatedFileSize = scanner.nextInt();

        StringGenerator stringGenerator = new StringGenerator();
        Path pathForUnsorted = Path.of(System.getProperty("user.dir") + "\\unsorted.txt");
        Path pathForSorted = Path.of(System.getProperty("user.dir") + "\\sorted.txt");
        CustomStringFileWriter customStringFileWriter = new CustomStringFileWriter(pathForUnsorted,stringGenerator,generatedFileSize);
        System.out.println("Start generate strings");
        long stringCounter = customStringFileWriter.write();
        System.out.println("Finish generate, was produced " + (stringCounter) + " strings, saved at " + pathForUnsorted);

        System.out.println("Do you wish to continue and sort file? y/n:  ");

        String next = scanner.next();

        while (!next.equalsIgnoreCase("n")){
            if (next.equalsIgnoreCase("y")){
                if (generatedFileSize<=200){
                    System.out.println("Generated file size <=200 Mb, start sort in memory");
                    CustomStringFileSorter customStringFileSorter = new CustomStringFileSorter(pathForUnsorted,pathForSorted);
                    customStringFileSorter.inMemSort(pathForUnsorted, pathForSorted);
                    System.out.println("Sorted, saved at " + pathForSorted);
                    System.exit(0);
                } else {
                    System.out.println("Generated file size >=200 Mb, start sort with tmp files");
                    CustomStringFileSorter customStringFileSorter = new CustomStringFileSorter(pathForUnsorted,pathForSorted);
                    customStringFileSorter.split();
                    System.out.println("Sorted, saved at " + pathForSorted);
                    System.exit(0);
                }
            }
            System.out.println("Do you wish to continue and sort file? y/n:  ");
            next = scanner.next();
        }
        System.exit(0);
    }
}
