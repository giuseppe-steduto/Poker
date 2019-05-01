import java.net.*;
import java.util.Vector;
public class Giocatore
{
    private ConnessioneAPartita conn = new ConnessioneAPartita("localhost", 2000);
    private String id = "";
    private String richiesta = "C";
    private String tmp = "";
    private FinestraGioco f;
    private Vector<String> carte = new Vector<String>();
    public Giocatore(FinestraGioco f) {
        this.f = f;
        inizializza();
    }

    public void inizializza() {
        try {
            //Richiesta apertura sessione
            String risp = conn.risposta("O");
            if(risp.equals("F")) {
                f.messaggio("La partita è piena!");
                return;
            }
            else if(risp.equals("E")) {
                f.messaggio("Errore di comunicazione!");
                return;
            }
            id = risp.substring(1);

            //Richiesta delle 5 carte
            risp = conn.risposta("R" + id);
            if(risp.equals("E")) {
                f.messaggio("Errore di comunicazione!");
                return;
            }
            else {
                for(int i = 0; i < 5; i++) {
                    tmp = "";
                    tmp += risp.charAt((3 * i) + 1);
                    tmp += risp.charAt((3 * i) + 2);
                    carte.add(tmp);
                }
                f.aggiungiCarte(carte);
                f.setVisible(true);
            }
        } catch(Exception e) {
            f.messaggio("Errore nella connessione al server!\n" + e.getMessage());
        }
    }

    public void cambiaCarte(String s) throws Exception {
        int n = s.length() / 3; //Numero di carte da sostituire
        String risp = conn.risposta("H" + n + s + "S" + id);
        if(risp.equals("E")) {
            f.messaggio("Errore di comunicazione!");
            return;
        }
        carte.clear();
        for(int i = 0; i < risp.length() / 3; i++) {
            tmp = "";
            tmp += risp.charAt((3 * i) + 1);
            tmp += risp.charAt((3 * i) + 2);
            carte.add(tmp);
        }
        f.aggiungiCarte(carte);
    }
    
    public void chiudi(String s) throws Exception {
        String risp = conn.risposta("$" + id + s);
    }
}
