import javax.swing.*;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Font;
import javax.swing.plaf.basic.BasicBorders;
/**
 * Questa classe serve solo per lo styling dei bottoni.
 */
public class Bottone extends JButton
{
    public Bottone(String label, String actionComm, Font f) {
        super(label);
        this.setActionCommand(actionComm);
        setSize(80, 50);
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                  BorderFactory.createLineBorder(Color.BLACK, 2),
                  BorderFactory.createLineBorder(Color.WHITE, 20)));
        setFont(f);
    }
}
