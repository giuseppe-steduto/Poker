import java.awt.image.*;
import java.awt.Image;
import java.awt.Graphics2D;
/**
 * Write a description of class Immagine here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Immagine extends BufferedImage
{

    public Immagine(Image img) {
        super(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = this.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
    }

}
