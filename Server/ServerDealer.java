package Server;

import java.net.*;
import java.util.*;
import java.io.*;

/*
Classe ServerDealer per il progetto "poker".
È il server che si occupa di gestire il mazzo e la partita.
Gestisce massimo 8 giocatori, assegnando e cambiando carte da un
mazzo di 52 carte da poker (Classe "mazzo").
Il protocollo di comunicazione usato è specificato nella cartella
del progetto.
*/
public class ServerDealer {
  private static int porta = 2000;
  private static Mazzo mazzo = new Mazzo();
  private static int giocatori = 0;
  private static boolean parolaDone = false;  //variabile booleana che segnala se è stata fatta parola da almeno un giocatore in questo turno o meno
  private static boolean cehapDone = false;  //variabile booleana che segnala se è stata fatta l'azione di cheap da almeno un giocatore in questo turno o meno
  private static Database log = new Database(); //Database in cui si memorizzano gli ID e i dati di ogni sessione
  private static int piatto; //Variabile che rappresenta il valore del piatto della sessione di gioco del poker
  private static int puntataMinima = 0; //Variabile che rappresenta il valore della puntata minima che si deve effettuare in ogni momento della partita
  private static boolean isPartitaFinita = false; //Variabile booleana che determina se la partita è o non è terminata (FALSE se è ancora in corso, TRUE se è terminata)
  private static String idPrimoGiocatore = ""; //Variabile contenente l'ID del giocatore che è "a capo" di ogni turno

    //Metodo main: l'apertura dei socket e la gestione delle richieste avviene qui
  public static void main(String[] args) {
        ServerSocket ss;
        try{
            //Apertura server sulla porta 2000
            ss = new ServerSocket(2000);

            while(true)
            {
                try{
                    Socket client = ss.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

                    // Lettura richiesta dal client
                    String str=in.readLine();
                    String risposta = elabora(str);

                    //trasmissione risposta del server
                    out.write(risposta);
                    out.flush();

                    // chiusura connessione
                    client.close();
                    in.close();
                    out.close();
                }
                catch(Exception e) {
                    System.out.println("COMUNICAZIONE FALLITA!\nErrore: " + e.getMessage());
                }
            }

        }
        catch(Exception e) {
            System.out.println("APERTURA ServerSocket FALLITA!\nErrore: " + e.getMessage());
        }
  }

  /*
   * Funzione che restituisce la risposta a una richiesta qualunque del client.
   * Parametri:
   * String r - La richiesta del client (vedi dettagli da protocollo)
   *
   * Valore restituito:
   * Stringa rappresentante la risposta del server (vedi dettagli da protocollo)
   */
  private static String elabora(String r) {
    if(isPartitaFinita)
        puntataMinima = 0;
    String s = "";
    //Controlla il tipo di richiesta (vedi protocollo)
    switch(r.charAt(0)) {
        case 'O':
            return apriConnessione(r);
        case 'R':
            return distribuisciCarte(r);
        case 'H':
            return cambiaCarte(r);
        case 'T':
            return isMioTurno(r);
        case 'W':
            return richiestaParola(r);
        case '$':
            return chiudiConnessione(r);
        case '?':
            return "M" + puntataMinima + "P" + piatto;
        case 'P':
            return operazionePuntata(r);
        default:
            return "E";
    }
  }

  /*
   * Funzione che restituisce la risposta alla richiesta del client "voglio puntare".
   * Parametri:
   * String r - La richiesta del client (di tipo P[V]S[ID])
   *
   * Valore restituito:
   * Stringa "OK" se l'operazione di puntata è adnato a buon fine, Stringa "E" se c'è stato un errore generico
   *
   * Note: questa funzione prevede anche l'aggiornamento delle seguenti vriabili:
   *       -idPrimoGiocatore --> asssume il valore dell'ID in questione se la sua puntata è maggiore a quella della puntataMinima o se la puntataMinima attualòe è pari a 0
   *       -puntatMinima --> assume il valore della puntata in questione se quest'ultima è maggiore alla puntatMinima stessa o se l'ID del giocatore in questione è uguale all'idPrimoGiocatore
  */
  private static String operazionePuntata(String r) {
        String tmp = "";
        int i = 0; //Variabile che, alla fine, determinerà la posizione della prima cifra dell'ID nella stringa r
        //Il ciclo do-while che va a mettere nella variabile tmp il valore della puntata che il client vuole effettuare
        do{
            tmp += r.charAt(i);
            i ++;
        }
        while(r.charAt(i) != 'S');
        int puntata = Integer.parseInt(tmp);
        String id = r.substring(i, 6);
        if(puntataMinima == 0)
            idPrimoGiocatore = id;
        if(id.equals(idPrimoGiocatore))
            puntataMinima = puntata;
        if(puntata == puntataMinima) {
            piatto += puntata;
            return "OK";
        }
        else if(puntata > puntataMinima) {
            idPrimoGiocatore = id;
            puntataMinima = puntata;
            piatto += puntata;
            return "OK";
        }
        return "E";
  }

  /*
   * Funzione che restituisce la risposta alla richiesta del client di fare "parola".
   * Parametri:
   * String r - La richiesta del client (di tipo W)
   *
   * Valore restituito:
   * Stringa del tipo W[R], dove R = S se è possibile fare parola
   * Stringa del tipo W[R], dove R = N se NON è possibile fare parola
  */
  private static String richiestaParola(String r) {
      String s = "";
      if((puntataMinima == piatto) && !cheapDone) {  //controllo del valore del piatto col valore della puntataMinima poiché se essi coincidono
          parolaDone = true;                         //significa che nessuno ha puntato o fatto cheap quindi si può far parola
          s = "WS";                  
          return s;
      }
      else {
          s = "WN";
          return s;
      }

  }

  /*
   * Funzione che restituisce la risposta alla richiesta del client di fare "cheap".
   * Parametri:
   * String r - La richiesta del client (di tipo A)
   *
   * Valore restituito:
   * Stringa del tipo A[R], dove R = S se è possibile fare cheap
   * Stringa del tipo A[R], dove R = N se NON è possibile fare cheap
  */
  private static String richiestaCheap(String r) {
    String s = "";
    if((puntataMinima == piatto) && !parolaDone) {  //controllo del valore del piatto col valore della puntataMinima poiché se essi coincidono
        cheapDone = true;                           //significa che nessuno ha puntato o fatto parola quindi si può fare cheap
        s = "AS";
        return s;
    }
    else {
        s = "AN";
        return s;
    }

  } 

  /*
   * Funzione che restituisce la risposta alla richiesta del client "è il mio turno?".
   * Parametri:
   * String r - La richiesta del client (di tipo T[ID])
   *
   * Valore restituito:
   * Stringa del tipo T[R], dove R = S se è il turno del giocatore con id = [ID], altrimenti R = N
   */
 private static String isMioTurno(String r) {
      String id = r.substring(1);
      Record rec = new Record(id);
      if(!log.contains(rec)) { //Controlla se esiste una sessione con quell'id
          return "TN";
      }
      for(Record record: log) { //Controlla tutti i record del database
          if(record.equals(rec)) { //Se ho trovato un record con lo stesso id
              if(record.isTurno())
                  return "TS";
          }
      }
      return "TN";
 }

  /*
   * Funzione che termina la connessione con il client eliminando la sua sessione.
     Parametri:
     String r - La richiesta del client (di tipo $[ID])

     Valore restituito:
     Stringa "ok", ma praticamente non serve a nulla
   */
  private static String chiudiConnessione(String r) {
      Record record = new Record(r.substring(1, 6));
      if(!log.contains(record)) {
          return "E";
      }
      //Se il giocatore che ha appena abbandonato la partita è colui che era "a capo" di ogni turno, allora bisognerà aggiornare la variabile idPrimoGiocatore
      if(idPrimoGiocatore.equals(record))
          idPrimoGiocatore = "";
      //Rimette le carte nel mazzo
      String car = log.get(log.indexOf(record)).getCarte(); //Le carte del tipo con l'id sessione specificato
      for(int i = 0; i < 5; i++) {
          Carta c = new Carta(car.charAt(3 * i + 1), car.charAt(3 * i + 2));
          mazzo.add(c);
      }
      log.remove(record);
      giocatori--;
      return "ok";
  }

  /*
    Funzione che dà carte nuove al client che le richiede, reinserendo le carte cambiate nel Mazzo
    Parametri:
    + String r - La richiesta del client (di tipo H[N_CARTE]C[VALORE][SEME]...S[ID])

    Valore restituito:
    - Stringa del tipo C[VALORE][SEME] ripetuto N_CARTE volte. Sarebbero le nuove carte
    - Stringa "E", errore generico (es. se non esiste un record con quell'id)
   */
  private static String cambiaCarte(String r) {
      String s = "";
      //Se non è presente nessun record con l'id inviato, restituisco errore
      Record record = new Record(r.substring(r.length() - 5));
      if(!log.contains(record)) {
          return "E";
      }
      String car = log.get(log.indexOf(record)).getCarte(); //Le vecchie carte del record, da aggiornare
      //Estrai n carte dal mazzo
      for(int i = 0; i < r.charAt(1) - 48; i++) {
          Carta cNuova = mazzo.estraiCarta();
          Carta cVecchia = new Carta(r.charAt(3 * i + 3), r.charAt(3 * i + 4));
          String tmp = "" + cNuova.getValore() + cNuova.getSeme();
          String tmp2 = "" + cVecchia.getValore() + cVecchia.getSeme();
          s += "C" + tmp;
          car = car.replace(tmp2, tmp);

      }
      //Rimetti nel mazzo le carte restituite
      for(int i = 0; i < r.charAt(1) - 48; i++) {
          Carta cVecchia = new Carta(r.charAt(3 * i + 3), r.charAt(3 * i + 4));
          mazzo.add(cVecchia);
      }
      record.cambiaCarte(r.charAt(1) - 48);
      record.setCarte(car);
      log.aggiorna(record);
      return s;
  }

  /*
    Funzione che dà carte (le prime 5) al client che le richiede
    Parametri:
    + String r - La richiesta del client (di tipo R[ID])

    Valore restituito:
    - Stringa del tipo C[VALORE][SEME] ripetuto 5 volte. Sarebbero le nuove carte
    - Stringa "E", errore generico (es. se non esiste un record con quell'id)
   */
  private static String distribuisciCarte(String r) {
      String s = "";
      //Se non è presente nessun record con l'id inviato, restituisco errore
      Record record = new Record(r.substring(1));
      if(!log.contains(record)) {
          return "E";
      }
      //Estrai 5 carte dal mazzo e inseriscile nella risposta del server
      for(int i = 0; i < 5; i++) {
        Carta c = mazzo.estraiCarta();
        s += "C" + c.getValore() + c.getSeme();
      }
      record.setCarte(s);
      log.aggiorna(record);
      return s;
  }

  /*
      Funzione che scorre il turno.
      Parametri:
      String id - L'id di chi ha appena finito (CON SUCCESSO) la sua mossa
  */
  private static void avantiTurno(String id) {
      Record record = new Record(id);
      for(int i = 0; i < log.size(); i++) {
          if(record.equals(log.get(i))) { //Se ho trovato il record con l'id del giocatore che ha fatto la mossa
              log.get(i).setTurno(false); //Non è più il suo turno
              if(i < log.size() - 1) //Se non siamo all'ultimo elemento del database
                log.get(i + 1).setTurno(true); //È il turno del giocatore successivo
              else                   //Se invece siamo all'ultimo elemento
                log.get(0).setTurno(true); //È il turno del primo giocatore
          }
      }
  }

  /*
    Funzione che dà carte (le prime 5) al client che le richiede
    Parametri:
    + String r - La richiesta del client (di tipo O)

    Valore restituito:
    - Stringa del tipo S[ID]. Significa "connessione creata, con id = [ID]"
    - Stringa "E", errore generico
    - Stringa "F", la partita è piena
   */
 private static String apriConnessione(String r) {
    String s = "";
    //Controllo sul numero di giocatori
    if(giocatori >= 8)
      return "F";
    giocatori++;

    //Genera un id di 5 interi casuali
    String id = "";
    Random rand = new Random();
    for(int i = 0; i < 5; i++) {
        id += rand.nextInt(10);
    }
    s = "S" + id;
    //Se la variabile idPrimoGiocatore non contine nessun ID vuol dire che l'ID appena generato rappresenterà il giocatore "a capo" di ogni turno
    if(!idPrimoGiocatore.equals(""))
        idPrimoGiocatore += id;
    //Salvo l'id appena creato nella tabella di log
    Record rec = new Record(id);
    log.inserisci(rec);
    return s;
  }
}
