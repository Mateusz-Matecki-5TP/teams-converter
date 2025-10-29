import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TeamsConverter {
    String inputFile;
    String outputFile;

    TeamsConverter(String inputFile, String outputFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;

        List<Participant> participantList = readParticipants();
        sortByLastName(participantList);
        saveToCSV(participantList);
    }

    private void sortByLastName(List<Participant> currentParticipants) {
        Collator collator = Collator.getInstance(new Locale("pl", "PL"));

        collator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        collator.setStrength(Collator.TERTIARY);

        currentParticipants.sort((p1, p2) -> collator.compare(p1.firstName, p2.lastName));
    }

    private void saveToCSV(List<Participant> currentParticipants) {
        try (
            BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream(this.outputFile),
                    StandardCharsets.UTF_8
                )
            )
        ) {
            writer.write("Nazwisko,Imię,Czas uczestnictwa,Status zaświadczenia\n");

            for (Participant p : currentParticipants) {
                writer.write(p.toCSV() + "\n");
            }

            System.out.println();
            System.out.println("Zapisano: " + this.outputFile);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public List<Participant> readParticipants() {
        String normalizedPath = Normalizer.normalize(this.inputFile, Normalizer.Form.NFC);

        try (
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    new FileInputStream(normalizedPath),
                    StandardCharsets.UTF_16LE
                )
            )
        ) {
            List<Participant> currentParticipants = new ArrayList<>();

            String line;
            boolean startReading = false;

            System.out.println();

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("2. Uczestnicy")) {
                    startReading = true;
                    continue;
                }

                if (startReading) {
                    if (line.isEmpty() || line.startsWith("3.")) {
                        break;
                    }

                    System.out.println(line);

                    Participant p = parse(line);
                    if (p != null) {
                        currentParticipants.add(p);
                    }
                }
            }

            return currentParticipants;
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return List.of();
    }

    public Participant parse(String line) {
        if (line.isEmpty()) {
            return null;
        }

        if (line.startsWith("Imię i nazwisko")) {
            return null;
        }

        String[] cols = line.split("\t");
        if (cols.length < 4) {
            return null;
        }


        String participant = cols[0].replaceAll("\\(.*?\\)", "").trim();
        String[] parts = participant.split("\\s+");
        if (parts.length < 2) {
            return null;
        }

        String lastName = parts[0];
        String firstName = parts[1];

        String time = cols[3].trim();
        if (time.isEmpty()) {
            time = "0 min";
        }

        return new Participant(firstName, lastName, time);
    }

    public static int convertToMinutes(String time) {
        int total = 0;

        Pattern pattern = Pattern.compile("(\\d+)\\s*godz\\.|(\\d+)\\s*min|(\\d+)\\s*s");
        Matcher matcher = pattern.matcher(time);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                total += Integer.parseInt(matcher.group(1)) * 60;
            }

            if (matcher.group(2) != null) {
                total += Integer.parseInt(matcher.group(2));
            }

            if (matcher.group(3) != null) {
                int seconds = Integer.parseInt(matcher.group(3));
                if (seconds >= 30) {
                    total += 1;
                }
            }
        }

        return total;
    }

}
