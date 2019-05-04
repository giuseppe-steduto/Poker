public class Record
{
    private String id;
    private String carte;
    private int carteCambiate = 0;
    private boolean isMioTurno = false; //Variabile che va aggiornata ad ogni mossa

    public Record() {id = null;}
    public Record(String id) {this.id = id;}
    public Record(String id, String carte) {this.id = id; this.carte = carte;}

    public String getId() {return id;}
    public void setId(String s) {id = s;}
    public String getCarte() {return carte;}
    public void setCarte(String s) {carte = s;}
    public boolean isTurno() {return isMioTurno;}
    public void setTurno(boolean t) {isMioTurno = t;}

    //Tiene traccia del numero di carte cambiate
    public int cambiaCarte(int n) {return carteCambiate += n;}

    @Override
    public int hashCode() {return Integer.parseInt(id);}

    @Override
    public boolean equals(Object o) {
        Record r = (Record) o;
        if(r.getId().equals(id))
            return true;
        return false;
    }

    @Override
    public String toString() {return id;}
}
