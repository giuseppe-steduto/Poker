public class Carta {
  private Character valore;
  private Character seme;
  public Carta(Character v, Character s) {
    valore = v;
    seme = s;
  }
  public Character getValore() {return valore;}
  public Character getSeme() {return seme;}
  public int getValoreCarta() {
      char v = this.getValore();
      int val = 0;
      switch(v) {
          case 'A':
              val = 1;
              break;
          case '2':
              val = 2;
              break;
          case '3':
              val = 3;
              break;
          case '4':
              val = 4;
              break;
          case '5':
              val = 5;
              break;
          case '6':
              val = 6;
              break;
          case '7':
              val = 7;
              break;
          case '8':
              val = 8;
              break;
          case 'X':
              val = 10;
              break;
          case 'J':
              val = 11;
              break;
          case 'Q':
              val = 12;
              break;
          case 'K':
              val = 13;
              break;
      }
      return val;
  }
}
