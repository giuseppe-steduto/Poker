import javax.swing.*;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Font;
import javax.swing.plaf.basic.BasicBorders;
/**
 * Questa classe serve solo per lo styling dei bottoni.
 */
public class Etichetta extends JLabel
{
    public Etichetta(String label, Font f) {
        super(label);
        setForeground(Color.WHITE);
        setFont(f);
    }
}
