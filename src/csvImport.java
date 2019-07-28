import java.io.*;
import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.sql.Date;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;


public class csvImport {

    static Connection connection = null;
    public static final String DB_CONNECT_STRING = "jdbc:mysql://localhost:3306/COREIT";
    public static final String DB_USERNAME = "root";
    public static final String DB_PASSWORD = "1234";
    public static int insertedGlobal = 0;
    public static int duplicityGlobal = 0;
    public static int updatedGlobal = 0;
    public static int actualRow = 0;
    //zdrojový adresář
    public static String pathImport = "/CoreIt/Import";
    //cílový adresář
    public static String pathImported = "/CoreIt/Imported";


    public static void main(String args[]) {
        final File folder = new File(pathImport);
        try {
            connection = DriverManager.getConnection(DB_CONNECT_STRING, DB_USERNAME, DB_PASSWORD);
            System.out.println("Connected to database");
            listFilesFromFolder(folder);

        } catch (SQLException e) {
            e.printStackTrace();

        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    //nacteni souboru z adresare
    public static void listFilesFromFolder(File folder) {
        for (File executedFile : folder.listFiles()) {
            System.out.println(executedFile.getName());
            System.out.println("Start parsing file, see progress below:");
            try {
                executeLinesFromFile(executedFile.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            moveFile(executedFile.getPath());
        }
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println();
        System.out.println("Global statistics (table ZAMESTNANCI):");
        System.out.println("inserted lines: " + insertedGlobal);
        System.out.println("updated lines:  " + updatedGlobal);
        System.out.println("duplicity:      " + duplicityGlobal);
    }

    //čtení z CSV souboru, zápis do DB, počítání, presun do Imported
    static void executeLinesFromFile(String path) throws IOException, SQLException {
        int inserted = 0;
        int duplicity = 0;
        int updated = 0;

        FileReader filereader = null;
        try {
            filereader = new FileReader(path);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        BufferedReader br = new BufferedReader(filereader);
        String myLine;
        Firma firmaCsv = new Firma();
        Zamestnanec zamCSV = new Zamestnanec();
        while ((myLine = br.readLine()) != null) {
            firmaCsv = getFirmaFromLine(myLine);
            zamCSV = getZamFromLine(myLine);
            if (firmaNotPresentInDB(firmaCsv.getIcoFy())) {
                addFirmaToDB(firmaCsv);
            }
//řádek z csv není v databázi, nutno vložit
            if (zamNotPresentInDB(getZamFromLine(myLine).getEmailZam())) {
                addZamToDB(getZamFromLine(myLine), getFirmaFromLine(myLine));
                inserted++;
                insertedGlobal++;
            } else {
// datum aktualizace v csv je novější, nutno přepsat řádek
                if (zamCSV.getDatumAktualizaceZam().compareTo(dateInDB(zamCSV.getEmailZam())) > 0) {
                    String query = "update zamestnanci set  JMENO_ZAM = ?, PRIJMENI_ZAM = ?, DATUM_AKTUALIZACE_ZAM =?, " +
                            "ICO_FIRMY = ? where EMAIL_ZAM = ?";
                    PreparedStatement preparedStmt = null;
                    preparedStmt = connection.prepareStatement(query);
                    preparedStmt.setString(1, zamCSV.getJmenoZam());
                    preparedStmt.setString(2, zamCSV.getPrijmeniZam());
                    preparedStmt.setDate(3, zamCSV.getDatumAktualizaceZam());
                    preparedStmt.setString(4, firmaCsv.getIcoFy());
                    preparedStmt.setString(5, zamCSV.getEmailZam());
                    preparedStmt.executeUpdate();
                    updated++;
                    updatedGlobal++;
                    // datum aktualizace v csv je stejné nebo starší, ponecháme původní záznam, jde o duplicity
                } else if (zamCSV.getDatumAktualizaceZam().compareTo(dateInDB(zamCSV.getEmailZam())) <= 0) {
                    duplicity++;
                    duplicityGlobal++;
                }
            }
            if (rowsInFile(path) < 80) {
                for (int i = 0; i < (int) (80 / rowsInFile(path)); i++) {
                    System.out.print(">");
                }
            } else if ((actualRow % Math.round((rowsInFile(path) / 80))) == 0) {
                System.out.print(">");
            }
            actualRow++;
        }

        System.out.println(" ");
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("0%    10%     20%    30%    40%     50%     60%     70%     80%     90%    100%");
        System.out.println();
        System.out.println("File statistics (table ZAMESTNANCI):");
        System.out.println("inserted lines: " + inserted);
        System.out.println("updated lines:  " + updated);
        System.out.println("duplicity:      " + duplicity);

        br.close();
        filereader.close();
    }


    static Firma getFirmaFromLine(String myRow) {
        String[] myArray = myRow.split(";");
        Firma myFirma = new Firma(myArray[0], myArray[1], myArray[2]);
        return myFirma;
    }

    static Zamestnanec getZamFromLine(String myRow) {
        String[] myArray = myRow.split(";");
        Zamestnanec zamec = new Zamestnanec(myArray[3], myArray[4], myArray[5], (Date.valueOf(myArray[6])));
        return zamec;
    }

    // metoda zapis do databaze
    static void addZamToDB(Zamestnanec zam, Firma firma) {
        String query = "insert into zamestnanci (email_zam, jmeno_zam, prijmeni_zam, datum_aktualizace_zam, ico_firmy) values (?, ?, ?, ?, ?)";
        PreparedStatement preparedStmt = null;
        try {
            preparedStmt = connection.prepareStatement(query);
            preparedStmt.setString(1, zam.getEmailZam());
            preparedStmt.setString(2, zam.getJmenoZam());
            preparedStmt.setString(3, zam.getPrijmeniZam());
            preparedStmt.setDate(4, zam.getDatumAktualizaceZam());
            preparedStmt.setString(5, firma.getIcoFy());
            preparedStmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void addFirmaToDB(Firma firma) {
        String query = "insert into firma (ico_firmy, nazev_firmy, adresa_firmy)" +
                "values (?, ?, ?)";
        PreparedStatement preparedStmt = null;
        try {
            preparedStmt = connection.prepareStatement(query);
            preparedStmt.setString(1, firma.getIcoFy());
            preparedStmt.setString(2, firma.getNazevFy());
            preparedStmt.setString(3, firma.getAdresaFy());
            preparedStmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // metoda koukni jestli to tam uz je
    static boolean zamNotPresentInDB(String emailZam) {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("select * from zamestnanci where EMAIL_ZAM ='" + emailZam + "'");
            if (rs.next() == false) {
                return true;
            } else return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    static boolean firmaNotPresentInDB(String icoFirmy) {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("select * from firma where ICO_FIRMY ='" + icoFirmy + "'");
            if (rs.next() == false) {
                return true;
            } else return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //metoda najdi datum aktualizace v DB
    static Date dateInDB(String emailZam) {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("select * from zamestnanci where EMAIL_ZAM ='" + emailZam + "'");
            rs.next();
            Date dbDate = rs.getDate("datum_aktualizace_zam");
            return dbDate;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // počet řádků v souboru
    public static int rowsInFile(String path) throws IOException {
        int rows = 0;
        FileReader filereader = null;
        String myLine;
        try {
            filereader = new FileReader(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(filereader);
        while ((myLine = br.readLine()) != null) {
            rows++;
        }
        br.close();
        filereader.close();
        return rows;
    }

    public static void moveFile(String sourcePath) {
        Path source = Paths.get(sourcePath);
        String fullPathImported = pathImported + "/" + source.getFileName().toString();
        Path target = Paths.get(fullPathImported);
        try {
            Path temp = Files.move(source, target);
            if (temp != null) {
                System.out.println("File moved succesfully.");
            } else {
                System.out.println("File not moved.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println();
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        System.out.println();
    }
}
