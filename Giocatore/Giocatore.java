import java.net.*;
import java.util.Vector;
public class Giocatore
{
    private ConnessioneAPartita conn = new ConnessioneAPartita("localhost", 2000);
    private String id = "";
    private String richiesta;
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

    public int isMioTurno() throws Exception {
        String risp = conn.risposta("T" + id);
        if(risp.charAt(1) == 'S') //È il tuo turno
            return 0;
        else if(risp.charAt(1) == 'N') //Non è il tuo turno
            return 1;
        return 2; //Partita finita
    }

    public boolean cambiaCarte(String s) throws Exception {
        int n = s.length() / 3; //Numero di carte da sostituire
        String risp = conn.risposta("H" + n + s + "S" + id);
        carte.clear();
        if(risp.equals("E")) {
            f.messaggioErr("Non sei riuscito a cambiare carte! Forse non è il momento di cambiare");
            return false;
        }
        for(int i = 0; i < risp.length() / 3; i++) {
            tmp = "";
            tmp += risp.charAt((3 * i) + 1);
            tmp += risp.charAt((3 * i) + 2);
            carte.add(tmp);
        }
        f.aggiungiCarte(carte);
        return true;
    }

    public void chiudi(String s) throws Exception {
        String risp = conn.risposta("$" + id + s);
    }

    public String richiediPuntata() throws Exception {
        String risp = conn.risposta("?P");
        return risp.substring(1, risp.indexOf("P"));
    }

    public String punta(int valorePuntata) throws Exception {return conn.risposta("P" + valorePuntata + "S" + id);}

    public String richiediPiatto() throws Exception {
        String risp = conn.risposta("?P");
        return risp.substring(risp.indexOf("P") + 1);
    }

    public String parola() throws Exception {return conn.risposta("W" + id);}

    public String ottieniPunteggio() throws Exception {
        String str = conn.risposta("F" + carte.elementAt(0) + carte.elementAt(1) + carte.elementAt(2) +carte.elementAt(3) + carte.elementAt(4) + id);
        return str;
    }
    
    public String finisci() throws Exception {return conn.risposta("F" + id);}
}
