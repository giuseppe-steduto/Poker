import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.imageio.*;
import java.io.*;
public class FinestraGioco extends JFrame implements ActionListener
{
        private int pixelInPiu = 0;
        Giocatore g;
        JPanel titoloTop = new JPanel();
        JPanel carte = new JPanel();
        JPanel bottoni = new JPanel();
        GruppoBottoni carteBottoni = new GruppoBottoni();
        public FinestraGioco() {
           //Comandi per finestra
           super("Le tue carte da poker");
           this.setLocation(200, 200);
           this.setSize(800, 250);
           this.setLayout(new BorderLayout());
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
                        messaggio("Impossibile chiudere la connessione!");
                    }
                }
            });
           //setResizable(false);

           //Comandi per pannello carte
           this.add(titoloTop, BorderLayout.NORTH);
           this.add(carte, BorderLayout.CENTER);
           carte.setLayout(new FlowLayout());

           //Comandi per pannello bottoni
           bottoni.setLayout(new FlowLayout());
           JButton cambia = new JButton("Cambia carte");
           cambia.addActionListener(this);
           bottoni.add(cambia);
           this.add(bottoni, BorderLayout.SOUTH);
        }

        //Metodo per visualizzare un prompt di messaggio di errore
        public void messaggio(String s) {
            JOptionPane.showMessageDialog(this, s, "Errore!",
                JOptionPane.ERROR_MESSAGE);
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

        //Richiamato per il cambio delle carte
        @Override
        public void actionPerformed(ActionEvent e) {
            pixelInPiu++;
            boolean possoEliminare = true;
            //Cosa succede se premo il bottone "Cambia carte" ↓
            //Controllo di aver selezionato da 1 a 4 carte
            if(carteBottoni.contaSelezionati() == 0)
                messaggio("Clicca sulle carte da cambiare e poi ripremi questo bottone");
            else if(carteBottoni.contaSelezionati() == 5)
                messaggio("Puoi cambiare solo da 1 a 4 carte. Deselezionane almeno una.");
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
                    messaggio("Niente carte nuove per te!\n" + exc.getMessage());
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
}
