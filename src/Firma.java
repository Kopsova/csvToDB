public class Firma {

    String icoFy;
    String nazevFy;
    String adresaFy;

    public Firma() {
    }

    public Firma(String icoFy, String nazevFy, String adresaFy) {
        this.icoFy = icoFy;
        this.nazevFy = nazevFy;
        this.adresaFy = adresaFy;
    }

    public String getIcoFy() {
        return icoFy;
    }

    public void setIcoFy(String icoFy) {
        this.icoFy = icoFy;
    }

    public String getNazevFy() {
        return nazevFy;
    }

    public void setNazevFy(String nazevFy) {
        this.nazevFy = nazevFy;
    }

    public String getAdresaFy() {
        return adresaFy;
    }

    public void setAdresaFy(String adresaFy) {
        this.adresaFy = adresaFy;
    }
}

