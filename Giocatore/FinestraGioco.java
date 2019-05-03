import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.imageio.*;
import java.io.*;
public class FinestraGioco extends JFrame implements ActionListener
{
        private int saldo = 500;
        private int pMinima = 50;
        private int piatto = 50;

        private int pixelInPiu = 0;
        Giocatore g;
        JPanel titoloTop = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(71, 113, 72));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor( Color.WHITE );
                // X Start, Y Start, X End, Y End
                // X = <---------->
                g.drawLine (230, 55, 550, 55);
            }
        };
        JPanel titoloPot = new JPanel();
        JPanel carte = new JPanel();
        JPanel bottoni = new JPanel();
        JPanel titolo = new JPanel();
        Bottone parola, cambia, punta;
        Etichetta saldoValueLabel, piattoValueLabel, puntataValueLabel;
        GruppoBottoni carteBottoni = new GruppoBottoni();
        Font font; //Font di default
        public FinestraGioco() {
           //Comandi per finestra
           super("Le tue carte da poker");
           this.setLocation(200, 200);
           this.setSize(800, 500);
           this.setLayout(new BorderLayout());
           this.getContentPane().setBackground(new Color(71, 113, 72)); //Colore "verde tavolo poker"
           g = new Giocatore(this);
           //Chiudi la sessione di gioco quando chiudi la finestra
           this.addWindowListener(new WindowAdapter(){
                public void windowClosing(WindowEvent e){
                    try {
                        String tmp = "";
                        for(JToggleButton b : carteBottoni) {
                            Carta c = (Carta) b;
                            tmp += "C" + c.getValore() + c.getSeme();
                        }
                        //Invia il codice di chiusura specificando le carte della mano
                        g.chiudi(tmp.toUpperCase());
                        System.exit(0);
                    } catch (Exception exc) {
                        messaggioErr("Impossibile chiudere la connessione!");
                    }
                }
            });
           //setResizable(false);

           //Import del font "Josefin Sans" da file. Se succede qualcosa, viene caricato il Century Gothic
           try {
               font = Font.createFont(Font.TRUETYPE_FONT, new File("JosefinSans-Regular.ttf")).deriveFont(Font.TRUETYPE_FONT, 20);
           } catch (IOException|FontFormatException e) {
                messaggioErr("Non è stato possibile caricare il font. Verrà usato uno predefinito.");
                font = new Font("Century Gothic", Font.PLAIN, 25);
           }

           //Comandi per pannello titolo
           titolo.setLayout(new BorderLayout());
           titolo.add(titoloTop, BorderLayout.NORTH);
           titolo.add(titoloPot, BorderLayout.SOUTH);

           //Comandi per pannello titolo superiore, saldo
           titoloTop.setLayout(new FlowLayout());
           titoloTop.setOpaque(true); //Necessario per far vedere il colore
           titoloTop.setBackground(new Color(71, 113, 72)); //Colore "verde tavolo poker"
           //Etichette per i soldi
           Etichetta saldoLabel = new Etichetta("Saldo", font);
           titoloTop.add(saldoLabel);
           saldoValueLabel = new Etichetta("500$", font);
           titoloTop.add(saldoValueLabel);
           JLabel chipsPoker = new JLabel();
           chipsPoker.setIcon(new ImageIcon("chipsPoker.png"));
           titoloTop.add(chipsPoker);
           piattoValueLabel = new Etichetta("50$", font);
           titoloTop.add(piattoValueLabel);
           Etichetta piattoLabel = new Etichetta("Piatto", font);
           titoloTop.add(piattoLabel);

           //Comandi per pannello titolo inferiore
           titoloPot.setLayout(new FlowLayout());
           titoloPot.setOpaque(true); //Necessario per far vedere il colore
           titoloPot.setBackground(new Color(71, 113, 72)); //Colore "verde tavolo poker"
           Etichetta puntataLabel = new Etichetta("Puntata minima", font.deriveFont(15));
           titoloPot.add(puntataLabel);
           puntataValueLabel = new Etichetta("50$", font);
           titoloPot.add(puntataValueLabel);

           this.add(titolo, BorderLayout.NORTH);

           //Comandi per pannello carte
           this.add(carte, BorderLayout.CENTER);
           carte.setLayout(new FlowLayout());
           carte.setOpaque(true);
           carte.setBackground(new Color(71, 113, 72)); //Colore "verde tavolo poker"

           //Comandi per pannello bottoni
           bottoni.setLayout(new FlowLayout());
           bottoni.setOpaque(true);
           bottoni.setBackground(new Color(71, 113, 72)); //Colore "verde tavolo poker"

           //Bottone per fare parola ↓
           parola = new Bottone("   Parola   ", "Parola", font);
           parola.addActionListener(this);
           parola.setEnabled(false);
           bottoni.add(parola);
           //Bottone per cambiare le carte ↓
           cambia = new Bottone("Cambia carte", "Cambia", font);
           cambia.addActionListener(this);
           bottoni.add(cambia);
           //Bottone per puntare ↓
           punta = new Bottone("   Punta    ", "Punta", font);
           punta.addActionListener(this);
           bottoni.add(punta);
           //Per adesso disabilita tutti i bottoni.
           cambia.setEnabled(false);
           punta.setEnabled(false);
           parola.setEnabled(false);
           this.add(bottoni, BorderLayout.SOUTH);

           //Delay per la richiesta isMioTurno da parte del client in questione
           javax.swing.Timer timer1 = new javax.swing.Timer(2500, this);
           timer1.setActionCommand("t1");
           timer1.start();
           SwingUtilities.updateComponentTreeUI(this);
           carteBottoni.aggiornaStile();
        }

        //Metodo per visualizzare un prompt di messaggioErr di errore
        public void messaggioErr(String s) {
            JOptionPane.showMessageDialog(this, s, "Errore!",
                JOptionPane.ERROR_MESSAGE);
        }

        public void messaggio(String s) {
            JOptionPane.showMessageDialog(this, s, "Errore!",
                JOptionPane.INFORMATION_MESSAGE);
        }

        public String messaggioDomanda(String s) {
            return JOptionPane.showInputDialog(s);
        }

        //Aggiunge le carte alla finestra, prendendole da un vettore di stringhe,
        //dove sono specificate secondo il solito standard.
        public void aggiungiCarte(Vector<String> mano) throws Exception {
            String nomeFile = "";
            for(String carta: mano) {
                nomeFile = carta.toLowerCase() + ".png";
                Carta c = new Carta(nomeFile);
                carte.add(c);
                carteBottoni.add(c);
                carte.repaint();
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getActionCommand().equals("Cambia")) {
                cambiaCarte();
            } else if(e.getActionCommand().equals("Punta")) {
                try {
                    punta();
                } catch (Exception exc) {
                    messaggioErr("Il dealer ha restituito info sulla puntata minima Leggieri è frocio");
                }
            } else if(e.getActionCommand().equals("Parola")) {
                parola();
            } else if (e.getActionCommand().equals("t1")) {
                try {
                    if(g.isMioTurno()) {
                        cambia.setEnabled(true);
                        punta.setEnabled(true);
                        parola.setEnabled(true);
                    }
                    else {
                        cambia.setEnabled(false);
                        punta.setEnabled(false);
                        parola.setEnabled(false);
                    }
                } catch (Exception exc) {
                    messaggioErr("Non posso stabilire se è il tuo turno o meno, oh, no! :( ");
                }
                try {
                    aggiornaValoriLabelPuntateMinimaEValorePiattoTitoloTopETitoloPotESaldo(Integer.parseInt(g.richiediPuntata()), Integer.parseInt(g.richiediPiatto()));
                } catch (Exception exc) {
                    messaggioErr("Non è stato possibile aggiornare i valori delle etichette");
                }
            }
        }


        //Richiamato per il cambio delle carte
        public void cambiaCarte() {
            boolean possoEliminare = true;
            //Cosa succede se premo il bottone "Cambia carte" ↓
            //Controllo di aver selezionato da 1 a 4 carte
            if(carteBottoni.contaSelezionati() == 0)
                messaggioErr("Clicca sulle carte da cambiare e poi ripremi questo bottone");
            else if(carteBottoni.contaSelezionati() == 5)
                messaggioErr("Puoi cambiare solo da 1 a 4 carte. Deselezionane almeno una.");
            else {
                String carteVecchie = "";
                Vector<Carta> tmp = new Vector<Carta>();
                //Segno nella variabile carteVecchie le carte selezionate e quindi
                //da cambiare
                for(JToggleButton b : carteBottoni) {
                    if(b.isSelected()) {
                        Carta c = (Carta) b;
                        carteVecchie += "C" + c.getValore() + c.getSeme();
                        tmp.add(c);
                    }
                }

                //Fai inviare dal server nuove carte
                try {
                    g.cambiaCarte(carteVecchie.toUpperCase());
                    //Nel metodo è prevista anche l'aggiunta delle nuove carte
                    //alla finestra
                } catch (Exception exc) {
                    messaggioErr("Niente carte nuove per te!\n" + exc.getMessage());
                    possoEliminare = false; //Variabile controllo: se qualcosa va
                                            //storto, non cancella le carte dalla finestra
                }
                //Rimozione delle carte vecchie
                if(possoEliminare) {
                    carteBottoni.removeAll(tmp); //Togli la carta dal gruppo di bottoni
                    for(Carta c: tmp) {
                        carte.remove(c); //Togli la carta dal JPanel
                    }
                    tmp.clear();
                    //Aggiorno la finestra, così inserisce le nuove carte
                    SwingUtilities.updateComponentTreeUI(this);
                    carteBottoni.aggiornaStile();
                }
            }
        }

        public void punta() throws Exception {
            int puntataMinima = Integer.parseInt(g.richiediPuntata());
            int p = 0;
            try {
                p = Integer.parseInt(messaggioDomanda("Quanto vuoi puntare? Puntata minima: " + puntataMinima));
            } catch (Exception e) {
                messaggioErr("Ehi amico, puoi inserire solo numeri!!!");
            }
            if(p < puntataMinima) {
                messaggioErr("La puntata non pareggia quella minima!");
                return;
            }
            if(p > saldo) {
                messaggioErr("Sei un povero di merda!");
                return;
            }
            String puntata = g.punta(p);
            if(puntata.equals("OK")) {
                messaggio("Puntata effettuata correttamente.");
                aggiornaValoriLabelPuntateMinimaEValorePiattoTitoloTopETitoloPotESaldo(p);
            }
            else
                messaggioErr("Qualcosa è andato storto! Forse non è il momento di puntare.");
        }

        public void parola() {
            try {
                if(g.parola().equals("WN")) {
                    messaggioErr("Mi spiace, ma qualcuno è più ricco di te :( ");
                }
                else {
                    messaggio("Okay, siete tutti dei poveri di merda, sei riuscito a fare parola!");
                }
            } catch (Exception exc) {
                messaggioErr("Il server mi ha dato una risposta che non ho capito! " + exc.getMessage());
            }
        }

        public void aggiornaValoriLabelPuntateMinimaEValorePiattoTitoloTopETitoloPotESaldo(int puntata) {
            saldo -= puntata;
            pMinima = puntata;
            piatto += puntata;
            puntataValueLabel.setText("" + pMinima + "$");
            piattoValueLabel.setText("" + piatto + "$");
            saldoValueLabel.setText("" + saldo + "$");
        }
        
        public void aggiornaValoriLabelPuntateMinimaEValorePiattoTitoloTopETitoloPotESaldo(int puntata, int piatto) {
            puntataValueLabel.setText("" + pMinima + "$");
            piattoValueLabel.setText("" + piatto + "$");
        }
}
