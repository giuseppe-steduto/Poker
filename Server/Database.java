import java.util.*;;

public class Database extends Vector<Record> {
    public Database() {super(8);}
    public void inserisci(Record r) {
        add(r);
    }

    public void aggiorna(Record r) {
        int i = 0;
        for(Record record : this) {
            if(r.equals(record)) { //Se hanno lo stesso id sessione
                this.removeElementAt(i);
                this.add(r);
                return;
            }
            i++;
        }
    }
}
