import java.util.*;
import java.awt.*;
import javax.swing.*;
public class GruppoBottoni extends Vector<Carta>
{
    public GruppoBottoni() {super();}

    public int contaSelezionati() {
        int i = 0;
        for(JToggleButton b : this) {
            if(b.isSelected())
                i++;
            }
        return i;
    }
    
    public void aggiornaStile() {
        for(JToggleButton b : this) {
            b.setMargin(new Insets(0, 0, 0, 0));
            b.setBorder(null);
        }
    }
}
