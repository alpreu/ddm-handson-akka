package ddm.handson.akka;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ProblemEntry implements Serializable {

    private byte id;
    private String name;
    private String password;
    private String gene;

    public ProblemEntry(byte id, String name, String password, String gene)
    {
        this.id = id;
        this.name = name;
        this.password = password;
        this.gene = gene;
    }

    public byte getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getGene() {
        return gene;
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
}
