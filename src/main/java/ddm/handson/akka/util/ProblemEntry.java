package ddm.handson.akka.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ProblemEntry implements Serializable {

    public final int id;
    public final String name;
    public final String password;
    public final String gene;

    public ProblemEntry(int id, String name, String password, String gene)
    {
        this.id = id;
        this.name = name;
        this.password = password;
        this.gene = gene;
    }

    public static ProblemEntry fromCSVString(String str)
    {
        final String[] values = str.split(";");
        return new ProblemEntry(Byte.parseByte(values[0]),
                values[1],
                values[2],
                values[3]);
    }

    public static List<ProblemEntry> parseFile(String file) throws IOException {
        List<ProblemEntry> problemEntries = new ArrayList<>(42);
        BufferedReader br = new BufferedReader(new FileReader(file));
        // skip first line
        br.readLine();
        String line = br.readLine();
        while (line != null) {
            if (line.length() > 10) {
                problemEntries.add(ProblemEntry.fromCSVString(line));
            }
            line = br.readLine();
        }
        return problemEntries;
    }

    public static int[] getIds(List<ProblemEntry> entries)
    {
        int[] ids = new int[entries.size()];
        int i = 0;
        for (ProblemEntry entry : entries)
            ids[i++] = entry.id;
        return ids;
    }

    public static String[] getPasswords(List<ProblemEntry> entries)
    {
        String[] passwords = new String[entries.size()];
        int i = 0;
        for (ProblemEntry entry : entries)
            passwords[i++] = entry.password;
        return passwords;
    }

    public static String[] getGeneSequences(List<ProblemEntry> entries)
    {
        String[] geneSequences = new String[entries.size()];
        int i = 0;
        for (ProblemEntry entry : entries)
            geneSequences[i++] = entry.gene;
        return geneSequences;
    }
}
