package task2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StringGenerator {
    private final Random random = new Random();

    public List<String> generateRandomString(){
        List<String> stringPair = new ArrayList<>();
            String generatedString = random.ints(97, 123)
                    .limit(25)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();

            stringPair.add(new StringBuilder().append(random.nextInt(100)).append(".").append(generatedString).toString());
            stringPair.add(new StringBuilder().append(random.nextInt(100)).append(".").append(generatedString).toString());

        return stringPair;
    }
}
