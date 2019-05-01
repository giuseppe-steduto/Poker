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
                        messaggio("Impossibile chiudere la connessione!");
                    }
                }
            });
           //setResizable(false);
           
           //Styling dei componenti
           try {
               font = Font.createFont(Font.TRUETYPE_FONT, new File("JosefinSans-Regular.ttf")).deriveFont(Font.TRUETYPE_FONT, 20);
           } catch (IOException|FontFormatException e) {
                messaggio("Non è stato possibile caricare il font. Verrà usato uno predefinito.");
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
           Etichetta saldoValueLabel = new Etichetta("25€", font);
           titoloTop.add(saldoValueLabel);
           JLabel chipsPoker = new JLabel();
           chipsPoker.setIcon(new ImageIcon("chipsPoker.png"));
           titoloTop.add(chipsPoker);
           Etichetta piattoValueLabel = new Etichetta("320€", font);
           titoloTop.add(piattoValueLabel);
           Etichetta piattoLabel = new Etichetta("Piatto", font);
           titoloTop.add(piattoLabel);
           
           //Comandi per pannello titolo inferiore
           titoloPot.setLayout(new FlowLayout());
           titoloPot.setOpaque(true); //Necessario per far vedere il colore
           titoloPot.setBackground(new Color(71, 113, 72)); //Colore "verde tavolo poker"
           Etichetta puntataLabel = new Etichetta("Puntata minima", font.deriveFont(15));
           titoloPot.add(puntataLabel);
           Etichetta puntataValueLabel = new Etichetta("8€", font);
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
           Bottone parola = new Bottone("   Parola   ", "Parola", font);
           parola.addActionListener(this);
           bottoni.add(parola);
           //Bottone per cambiare le carte ↓
           Bottone cambia = new Bottone("Cambia carte", "Cambia", font);
           cambia.addActionListener(this);
           bottoni.add(cambia);
           //Bottone per puntare ↓
           Bottone punta = new Bottone("   Punta    ", "Punta", font);
           punta.addActionListener(this);
           bottoni.add(punta);
           
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

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getActionCommand().equals("Cambia")) {
                cambiaCarte();
            } else if (e.getActionCommand().equals("Cambia")) {
            
            } else {
            
            }
        }
        
        
        //Richiamato per il cambio delle carte
        public void cambiaCarte() {
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
