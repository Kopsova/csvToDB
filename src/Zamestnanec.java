import java.sql.Date;

public class Zamestnanec {
    String emailZam;
    String jmenoZam;
    String prijmeniZam;
    Date datumAktualizaceZam;

    public Zamestnanec() {
    }

    public Zamestnanec(String emailZam, String jmenoZam, String prijmeniZam, Date datumAktualizaceZam) {
        this.emailZam = emailZam;
        this.jmenoZam = jmenoZam;
        this.prijmeniZam = prijmeniZam;
        this.datumAktualizaceZam = datumAktualizaceZam;
    }

    public String getEmailZam() {
        return emailZam;
    }

    public void setEmailZam(String emailZam) {
        this.emailZam = emailZam;
    }

    public String getJmenoZam() {
        return jmenoZam;
    }

    public void setJmenoZam(String jmenoZam) {
        this.jmenoZam = jmenoZam;
    }

    public String getPrijmeniZam() {
        return prijmeniZam;
    }

    public void setPrijmeniZam(String prijmeniZam) {
        this.prijmeniZam = prijmeniZam;
    }

    public Date getDatumAktualizaceZam() {
        return datumAktualizaceZam;
    }

    public void setDatumAktualizaceZam(Date datumAktualizaceZam) {
        this.datumAktualizaceZam = datumAktualizaceZam;
    }
}
