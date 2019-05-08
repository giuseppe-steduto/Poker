import java.net.*;
import java.util.*;
import java.io.*;
//Scala Reale – Scala Colore – Poker – Full – Colore – Scala – Tris – Doppia Coppia – Coppia – Carta Alta
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
  private static int numeroMosse = 0;  //numero delle mosse in un giro
  private static int numeroGiri = 0;  //numero di volte in cui viene azzerato numero mosse
  private static boolean parolaDone = false;  //variabile booleana che segnala se è stata fatta parola da almeno un giocatore in questo turno o meno
  private static boolean cheapDone = false;  //variabile booleana che segnala se è stata fatta l'azione di cheap da almeno un giocatore in questo turno o meno
  private static Database log = new Database(); //Database in cui si memorizzano gli ID e i dati di ogni sessione
  private static int piatto = 50; //Variabile che rappresenta il valore del piatto della sessione di gioco del poker
  private static int puntataMinima = 50; //Variabile che rappresenta il valore della puntata minima che si deve effettuare in ogni momento della partita
  private static boolean isPartitaFinita = false; //Variabile booleana che determina se la partita è o non è terminata (FALSE se è ancora in corso, TRUE se è terminata)
  private static String idPrimoGiocatore = ""; //Variabile contenente l'ID del giocatore che è "a capo" di ogni turno
  private static Vector<String> punteggi = new Vector();  //vector contente il punteggio ottenuto da un giocatore più l'id di questu'ultimo nella seguente struttura "P[p]S[id]"

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
                    String str = in.readLine();
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
        case 'F':
            return hoVinto(r);
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
        if(numeroGiri == 1) //Se siamo durante il giro di cambio carte, restituisci errore
            return "E";
        String tmp = "";
        int i = 1; //Variabile che, alla fine, determinerà la posizione della prima cifra dell'ID nella stringa r
        //Il ciclo do-while che va a mettere nella variabile tmp il valore della puntata che il client vuole effettuare
        do {
            tmp += r.charAt(i);
            i ++;
        } while(r.charAt(i) != 'S');
        int puntata = Integer.parseInt(tmp);
        String id = r.substring(++i);
        if(puntataMinima == 0)
            idPrimoGiocatore = id;
        if(id.equals(idPrimoGiocatore))
            puntataMinima = puntata;
        if(puntata == puntataMinima) {
            piatto += puntata;
            avantiTurno(id);
            aumentaMossa();
            parolaDone = false;
            return "OK";
        }
        else if(puntata > puntataMinima) {
            idPrimoGiocatore = id;
            puntataMinima = puntata;
            piatto += puntata;
            numeroMosse = 0;
            /*aumentaMossa();
            * per ora lo commento perché teoricamente non dovrebbe mai servire in questo caso
            */
            avantiTurno(id);
            aumentaMossa();
            parolaDone = false;
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
      String id = r.substring(1);
      if(idPrimoGiocatore.equals(""))
        idPrimoGiocatore = id;
      if(id.equals(idPrimoGiocatore) && numeroGiri == 2) {
        parolaDone = true;
      }
      if(parolaDone) {  //controllo del valore del piatto col valore della puntataMinima poiché se essi coincidono
          s = "WS";
          avantiTurno(id);
          aumentaMossa();
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
      if(isPartitaFinita) {
          return "TF";
      }
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
      Record record = new Record(r.substring(1, 7));
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
      aumentaMossa();
      avantiTurno(record.getId());
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
      Record record = new Record(r.substring(r.length() - 6));
      if(!log.contains(record) || numeroGiri != 1) { //Se id sessione non presente o se non è il momento di cambiare carte
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
      avantiTurno(record.getId());
      aumentaMossa();
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
      if(log.size() == 1) {
          record.setTurno(true);
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
      if(isPartitaFinita)
            return;
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
    for(int i = 0; i < 6; i++) {
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

  /*
   * Funzione che restituisce una stringa contenente il punteggio delle carte di un giocatore e memorizza il punteggio ottenuto più l'id del giocatore
   * che l'ha accumulato in un vector
   * Parametro: 5 carte di tipo Carta
   * Valore restituito: una Stringa punteggio del tipo "[P1][P2][P3]"
   * Note: - P1 è un valore numerico che va da 0 a 8 e identifica il tipo di punteggio massimo ottenibile dalle carte
           - P2 è un valore numerico che identifica il valore della carta più alta appartenente al tipo di punteggio migliore ottenibile dalle carte
           - P3 è un carattere che identifica il seme della carta più alta appartenente al tipo di punteggio migliore ottenibile dalle carte
   * Altre note: Specifica valori P1
               - 0 -> Carta più alta
               - 1 -> Coppia (2 carte dello stesso valore)
               - 2 -> Doppia Coppia (2 carte dello stesso valore + altre 2 carte dello stesso valore)
               - 3 -> Tris (3 carte dello stesso valore)
               - 4 -> Scala (5 carte consecutive con semi differenti)
               - 5 -> Full (3 carte dello stesso valore + 2 carte dello stesso valore)
               - 6 -> Colore (5 carte con lo stesso seme)
               - 7 -> Poker (4 carte dello stesso valore)
               - 8 -> Scala reale (5 carte consecutive dello stesso seme)
  */
  public static String ottieniPunteggioCarte(Carta c1, Carta c2, Carta c3, Carta c4, Carta c5) {
      String punteggio = "";
      if(!isScalaReale(c1, c2, c3, c4, c5).equals(""))
          punteggio = isScalaReale(c1, c2, c3, c4, c5);
      else if(!isPoker(c1, c2, c3, c4, c5).equals(""))
          punteggio = isPoker(c1, c2, c3, c4, c5);
      else if(!isColore(c1, c2, c3, c4, c5).equals(""))
          punteggio = isColore(c1, c2, c3, c4, c5);
      else if(!isFull(c1, c2, c3, c4, c5).equals(""))
          punteggio = isFull(c1, c2, c3, c4, c5);
      else if(!isScala(c1, c2, c3, c4, c5).equals(""))
          punteggio = isScala(c1, c2, c3, c4, c5);
      else if(!isTris(c1, c2, c3, c4, c5).equals(""))
          punteggio = isTris(c1, c2, c3, c4, c5);
      else if(!isDoppiaCoppia(c1, c2, c3, c4, c5).equals(""))
          punteggio = isDoppiaCoppia(c1, c2, c3, c4, c5);
      else if(!isCoppia(c1, c2, c3, c4, c5).equals(""))
          punteggio = isCoppia(c1, c2, c3, c4, c5);
      else
          punteggio = cartaPiuAlta(c1, c2, c3, c4, c5);
      String pFinale = "";  //stringa contenente il punteggio del giocatore più il relativo id
      pFinale += "P" + punteggio;
      //punteggi.add(pFinale);
      return punteggio;
  }

  public static String ottieniPunteggioCarte(String c) {
    //C1qC1qC1qC1qC1q
    //0123456789ABCDE
    Carta c1 = new Carta(c.substring(1, 3));
    Carta c2 = new Carta(c.substring(4, 6));
    Carta c3 = new Carta(c.substring(7, 9));
    Carta c4 = new Carta(c.substring(10, 12));
    Carta c5 = new Carta(c.substring(13));
    return ottieniPunteggioCarte(c1, c2, c3, c4, c5);
  }

  /*
    Questa funzione confronta due stringhe rappresentanti i punteggi di due diversi giocatori.

    Parametri:
    p1 -> Punteggio ("P...") di un giocatore
    p2 -> Punteggio ("P...") di un altro giocatore

    Valore restituito:
    true se p1 > p2
    false se p1 < p2
  */
  public static boolean confrontaPunteggi(String p1, String p2) throws Exception {
      String pParte2 = ""; //Il problema è che Leggieri ha restituito il punteggio facendo
                           //[P1][P2][P3], senza separatori, e P2 potrebbe essere a 2 cifre.
      pParte2 = "" + p1.charAt(1);
      if(p1.length() == 4)
          pParte2 = p1.substring(1, 2);
      int punteggio1 = Character.getNumericValue(p1.charAt(0)) * 1000 +
                       Integer.parseInt(pParte2) * 10 +
                       Character.getNumericValue(p1.charAt(2));
      pParte2 = "" + p2.charAt(1);
      if(p2.length() == 4)
          pParte2 = p2.substring(1, 2);
      int punteggio2 = Character.getNumericValue(p2.charAt(0)) * 1000 +
                       Integer.parseInt(pParte2) * 10 +
                       Character.getNumericValue(p2.charAt(2));
      if(punteggio1 > punteggio2)
          return true;
      else
          return false;
  }

  public static String isCoppia(Carta c1, Carta c2, Carta c3, Carta c4, Carta c5) {
      Vector<Carta> mano = new Vector<Carta>();
      mano.add(c1);
      mano.add(c2);
      mano.add(c3);
      mano.add(c4);
      mano.add(c5);
      for(int i = 0; i < 4; i++) {
          for(int j = i + 1; j < 5; j++) {
              if(mano.get(i).getValoreCarta() == mano.get(j).getValoreCarta()) {
                  String str = "1" + mano.get(i).getValoreCarta();
                  char seme = semePiuAlto(mano.get(i).getSeme(), mano.get(j).getSeme());
                  return str + seme;
              }
          }
      }
      return "";
  }

  public static char semePiuAlto(char s1, char s2) {
      if(s1 == 'C')
        return s1;
      else if(s1 == 'P')
        return s2;
      else if(s1 == 'F') {
          if(s2 == 'C' || s2 == 'Q')
            return s2;
          else
            return s1;
      }
      else {
          if(s2 == 'C')
            return s2;
          else
            return s1;
      }
  }

  public static String isDoppiaCoppia(Carta c1, Carta c2, Carta c3, Carta c4, Carta c5) {
      int nCoppie = 0;
      Vector<Carta> coppia1 = new Vector<Carta>(); //Prima coppia
      Vector<Carta> coppia2 = new Vector<Carta>(); //Seconda coppia
      Vector<Carta> mano = new Vector<Carta>();
      mano.add(c1);
      mano.add(c2);
      mano.add(c3);
      mano.add(c4);
      mano.add(c5);
      for(int i = 0; i < 4; i++) {
          for(int j = i + 1; j < 5; j++) {
              if(mano.get(i).getValoreCarta() == mano.get(j).getValoreCarta()) {
                  if(nCoppie == 0) {
                      coppia1.add(mano.get(i));
                      coppia1.add(mano.get(j));
                  } else {
                      coppia2.add(mano.get(i));
                      coppia2.add(mano.get(j));
                  }
                  nCoppie++;
              }
          }
      }

      if(nCoppie < 2) {return "";}
      if(coppia1.get(0).getValoreCarta() > coppia2.get(0).getValoreCarta()) {
          return "2" + coppia1.get(0).getValoreCarta() + semePiuAlto(coppia1.get(0).getSeme(), coppia1.get(1).getSeme());
      }
      return "2" + coppia2.get(0).getValoreCarta() + semePiuAlto(coppia2.get(0).getSeme(), coppia2.get(1).getSeme());
  }

  public static String isScalaReale(Carta c1, Carta c2, Carta c3, Carta c4, Carta c5) {
      String str = "";
      int v1 = getValoreCarta(c1);
      int v2 = getValoreCarta(c2);
      int v3 = getValoreCarta(c3);
      int v4 = getValoreCarta(c4);
      int v5 = getValoreCarta(c5);
      int[] arrV = {v1, v2, v3, v4, v5};
      Arrays.sort(arrV);
      char s1 = c1.getSeme();
      char s2 = c2.getSeme();
      char s3 = c3.getSeme();
      char s4 = c4.getSeme();
      char s5 = c5.getSeme();
      if(s1 == s2 && s1 == s3 && s1 == s4 && s1 == s5) {
          if(arrV[0] == arrV[1] - 1 && arrV[1] == arrV[2] - 1 && arrV[2] == arrV[3] - 1 && arrV[4] == arrV[4] - 1)
              str += "8" + arrV[4] + "" + s1;
      }
      return str;
  }

  public static String isPoker(Carta c1, Carta c2, Carta c3, Carta c4, Carta c5) {
      String str = "";
      int v1 = getValoreCarta(c1);
      int v2 = getValoreCarta(c2);
      int v3 = getValoreCarta(c3);
      int v4 = getValoreCarta(c4);
      int v5 = getValoreCarta(c5);
      if((v1 == v2 && v1 == v3 && v1 == v4) || (v1 == v2 && v1 == v3 && v1 == v5) || (v1 == v2 && v1 == v4 && v1 == v5) || (v1 == v3 && v1 == v4 && v1 == v5))
          str += "7" + v1 + "" + c1.getSeme();
      else if(v2 == v3 && v2 == v4 && v2 == v5)
          str += "7" + v2 + "" + c2.getSeme();
      return str;
  }

  public static String isColore(Carta c1, Carta c2, Carta c3, Carta c4, Carta c5) {
      String str = "";
      char s1 = c1.getSeme();
      char s2 = c2.getSeme();
      char s3 = c3.getSeme();
      char s4 = c4.getSeme();
      char s5 = c5.getSeme();
      int v1 = getValoreCarta(c1);
      int v2 = getValoreCarta(c2);
      int v3 = getValoreCarta(c3);
      int v4 = getValoreCarta(c4);
      int v5 = getValoreCarta(c5);
      int[] arrV = {v1, v2, v3, v4, v5};
      Arrays.sort(arrV);
      if(s1 == s2 && s1 == s3 && s1 == s4 && s1 == s5)
          str = "6" + arrV[4] + "" + s1;
      return str;
  }

  public static String isFull(Carta c1, Carta c2, Carta c3, Carta c4, Carta c5) {
      String str = "";
      int v1 = getValoreCarta(c1);
      int v2 = getValoreCarta(c2);
      int v3 = getValoreCarta(c3);
      int v4 = getValoreCarta(c4);
      int v5 = getValoreCarta(c5);
      int[] arrV = {v1, v2, v3, v4, v5};
      Arrays.sort(arrV);
      //Per questo tipo di punteggio non è necessario inserire anche P3 nella stringa di return
      if(arrV[0] == arrV[1] && arrV[2] == arrV[3] && arrV[2] == arrV[4])
          str = "5" + arrV[2] + " ";
      else if(arrV[0] == arrV[1] && arrV[0] == arrV[2] && arrV[3] == arrV[4])
          str = "5" + arrV[0] + " ";
      return str;
  }

  public static String isScala(Carta c1, Carta c2, Carta c3, Carta c4, Carta c5) {
      String str = "";
      char s1 = c1.getSeme();
      char s2 = c2.getSeme();
      char s3 = c3.getSeme();
      char s4 = c4.getSeme();
      char s5 = c5.getSeme();
      int v1 = getValoreCarta(c1);
      int v2 = getValoreCarta(c2);
      int v3 = getValoreCarta(c3);
      int v4 = getValoreCarta(c4);
      int v5 = getValoreCarta(c5);
      int[] arrV = {v1, v2, v3, v4, v5};
      Arrays.sort(arrV);
      char tmp = ' ';
      if(arrV[4] == v1)
          tmp = s1;
      else if(arrV[4] == v2)
          tmp = s2;
      else if(arrV[4] == v3)
          tmp = s3;
      else if(arrV[4] == v4)
          tmp = s4;
      else if(arrV[4] == v5)
          tmp = s5;
      if(arrV[0] == arrV[1] - 1 && arrV[1] == arrV[2] - 1 && arrV[2] == arrV[3] - 1 && arrV[3] == arrV[4] - 1)
          if(s1 != s2 || s1 != s3 || s1 != s4 || s1 != s5 || s2 != s3 || s2 != s4 || s2 != s5 || s3 != s4 || s3 != s5 || s4 != s5)
              str = "4" + arrV[4] + "" + tmp;
      return str;
  }

  public static String isTris(Carta c1, Carta c2, Carta c3, Carta c4, Carta c5) {
      String str = "";
      int v1 = getValoreCarta(c1);
      int v2 = getValoreCarta(c2);
      int v3 = getValoreCarta(c3);
      int v4 = getValoreCarta(c4);
      int v5 = getValoreCarta(c5);
      int[] arrV = {v1, v2, v3, v4, v5};
      Arrays.sort(arrV);
      //Per questo tipo di punteggio non è necessario inserire anche P3 nella stringa di return
      if(arrV[0] == arrV[1] && arrV[0] == arrV[2])
          str = "3" + arrV[2] + " ";
      else if(arrV[1] == arrV[2] && arrV[1] == arrV[3])
          str = "3" + arrV[3] + " ";
      else if(arrV[2] == arrV[3] && arrV[2] == arrV[4])
          str = "3" + arrV[4] + " ";
      return str;
  }

  /*public static isCoppia(Carta c1, Carta c2, Carta c3, Carta c4, Carta c5) {
      String str = "";
      int v1 = getValoreCarta(c1);
      int v2 = getValoreCarta(c2);
      int v3 = getValoreCarta(c3);
      int v4 = getValoreCarta(c4);
      int v5 = getValoreCarta(c5);
  }*/

  public static String cartaPiuAlta(Carta c1, Carta c2, Carta c3, Carta c4, Carta c5) {
      char s1 = c1.getSeme();
      char s2 = c2.getSeme();
      char s3 = c3.getSeme();
      char s4 = c4.getSeme();
      char s5 = c5.getSeme();
      int v1 = getValoreCarta(c1);
      int v2 = getValoreCarta(c2);
      int v3 = getValoreCarta(c3);
      int v4 = getValoreCarta(c4);
      int v5 = getValoreCarta(c5);
      int[] arrV = {v1, v2, v3, v4, v5};
      Arrays.sort(arrV);
      char tmp = ' ';
      if(arrV[4] == v1)
          tmp = s1;
      else if(arrV[4] == v2)
          tmp = s2;
      else if(arrV[4] == v3)
          tmp = s3;
      else if(arrV[4] == v4)
          tmp = s4;
      else if(arrV[4] == v5)
          tmp = s5;
      return "0" + arrV[4] + "" + tmp;
  }

  public static int getValoreCarta(Carta c) {
      char v = c.getValore();
      switch(v) {
          case 'A':
              return 1;
          case '2':
              return 2;
          case '3':
              return 3;
          case '4':
              return 4;
          case '5':
              return 5;
          case '6':
              return 6;
          case '7':
              return 7;
          case '8':
              return 8;
          case '9':
              return 9;
          case 'X':
              return 10;
          case 'J':
              return 11;
          case 'Q':
              return 12;
          case 'K':
              return 13;
      }
      return 0;
  }

  private static boolean aumentaMossa() {
      numeroMosse++;
      if(numeroMosse == giocatori) {
        numeroGiri++;
        if(numeroGiri == 3)
          terminaPartita();
        numeroMosse = 0;
        return true;
      }
      return false;
  }

  private static void terminaPartita() {
      for(Record r: log) {
          r.setTurno(false);
      }
      Record max = log.get(0);
      try {
        for(Record r: log) {
          if(confrontaPunteggi(ottieniPunteggioCarte(max.getCarte()), ottieniPunteggioCarte(r.getCarte()))) {
            max = r;
          }
        }
      } catch (Exception exc) {
        System.out.println("Errore nel confronto dei punteggi!\n" + exc.getMessage());
      }
      idPrimoGiocatore = max.getId();
      isPartitaFinita = true;
  }

  /*
      Funzione che risponde alla richiesta del client di sapere se ha vinto o meno.
      Parametri:
      String r - S[ID] - L'id del client richiedente
  */
  private static String hoVinto(String r) {
      if(!isPartitaFinita)
          return "E";
      if(idPrimoGiocatore.equals(r.substring(1)))
        return "S"; //A partita finita, in "idPrimoGiocatore" c'è l'id del vincitore.
      return "N";
  }
}
