import java.util.*;
public class Mazzo {
  private Vector<Carta> deck;

  public Mazzo() {
    Carta[] mazzo = {
      new Carta('A', 'C'), new Carta('2', 'C'), new Carta('3', 'C'),
      new Carta('4', 'C'), new Carta('5', 'C'), new Carta('6', 'C'),
      new Carta('7', 'C'), new Carta('8', 'C'), new Carta('9', 'C'),
      new Carta('X', 'C'), new Carta('J', 'C'), new Carta('Q', 'C'),
      new Carta('K', 'C'),
      new Carta('A', 'Q'), new Carta('2', 'Q'), new Carta('3', 'Q'),
      new Carta('4', 'Q'), new Carta('5', 'Q'), new Carta('6', 'Q'),
      new Carta('7', 'Q'), new Carta('8', 'Q'), new Carta('9', 'Q'),
      new Carta('X', 'Q'), new Carta('J', 'Q'), new Carta('Q', 'Q'),
      new Carta('K', 'Q'),
      new Carta('A', 'F'), new Carta('2', 'F'), new Carta('3', 'F'),
      new Carta('4', 'F'), new Carta('5', 'F'), new Carta('6', 'F'),
      new Carta('7', 'F'), new Carta('8', 'F'), new Carta('9', 'F'),
      new Carta('X', 'F'), new Carta('J', 'F'), new Carta('Q', 'F'),
      new Carta('K', 'F'),
      new Carta('A', 'P'), new Carta('2', 'P'), new Carta('3', 'P'),
      new Carta('4', 'P'), new Carta('5', 'P'), new Carta('6', 'P'),
      new Carta('7', 'P'), new Carta('8', 'P'), new Carta('9', 'P'),
      new Carta('X', 'P'), new Carta('J', 'P'), new Carta('Q', 'P'),
      new Carta('K', 'P')
    };
    List<Carta> tmp = new ArrayList(mazzo.length);
    tmp.addAll(Arrays.asList(mazzo));
    deck = new Vector<Carta>(tmp);
  }

  public Carta estraiCarta() {
    int random = (int) (Math.random() * deck.size());
    Carta c = deck.get(random);
    deck.removeElementAt(random);
    return c;
  }
  
  public void add(Carta c) {deck.add(c);}
}