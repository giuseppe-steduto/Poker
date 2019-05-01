import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.plaf.basic.BasicBorders.ToggleButtonBorder;
public class Carta extends JToggleButton implements ActionListener
{
    private Character valore = ' ';
    private Character seme = ' ';
    private String nomeFile = "";
    public Carta() {
        super();
    }

    public Character getValore() {return valore;}
    public Character getSeme() {return seme;}

    public Carta(String nomeFile) throws Exception {
        super();
        this.nomeFile = nomeFile;
        valore = nomeFile.charAt(0);
        seme = nomeFile.charAt(1);
        BufferedImage img = ImageIO.read(new File(nomeFile));
        ImageIcon icon = new ImageIcon(
                    new ImageIcon(nomeFile).getImage().getScaledInstance(
                        100, 145, Image.SCALE_DEFAULT));
        this.setMargin(new Insets(0, 0, 0, 0));
        // to add a different background
        // to remove the border
        this.setBorder(null);
        this.addActionListener(this);
        this.setIcon(icon);
        this.setSize(100, 400);
    }

    public void resizeImage(int x, int y) {
        ImageIcon icon = new ImageIcon(
                new ImageIcon(nomeFile).getImage().getScaledInstance(
                                x, y, Image.SCALE_DEFAULT));
        this.setIcon(icon);
    }

    public void actionPerformed(ActionEvent e) {
        if(this.isSelected()) {
            this.resizeImage(90, 130);
            this.setBorder(new BasicBorders.ToggleButtonBorder(
                            new Color(68, 138, 252),
                            new Color(42, 90, 168),
                            new Color(68, 138, 252),
                            new Color(42, 90, 168)));
        }
        else {
            this.resizeImage(100, 145);
            this.setBorder(null);
        }
    }

}
