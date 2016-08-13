import java.awt.*;

public class Bezier3D {
  private double error;
  private double[ ][ ] curva = new double[4][3];
  private int ox;
  private int oy;

// e = error de trazado, normalmente e = 1
  public Bezier3D (double e) {
    for (int i=0; i<4; ++i)
      for (int j=0; j<3; ++j)
        curva[i][j] = 0;
    error = e;
    ox = 0;
    oy = 0;
  }

// Distancia del punto u a la recta v
  private double distancia (double[ ] u, double[ ] v) {
    double u2 = 0;
    for (int i=0; i<3; ++i) u2 += u[i]*u[i];
    double v2 = 0;
    for (int i=0; i<3; ++i) v2 += v[i]*v[i];
    double prodPunto = 0;
    for (int i=0; i<3; ++i) prodPunto += u[i]*v[i];
    return Math.sqrt (u2 - (prodPunto*prodPunto/v2));
  }

// Puede trazarse como una linea recta?
  private boolean recto (double[ ][ ] curva) {
    double[ ] v0 = new double[3];
    double[ ] v1 = new double[3];
    double[ ] v2 = new double[3];

    for (int i=0; i<3; ++i) {
      v0[i] = curva[3][i] - curva[0][i];
      v1[i] = curva[1][i] - curva[0][i];
      v2[i] = curva[3][i] - curva[2][i];
    }

    double d1 = distancia (v1, v0);
    double d2 = distancia (v2, v0);

    double d = (d2>d1)? d2: d1;
    return (d<=error);
  }

  private void dividir (double[ ][ ] curva, double[ ][ ] izq, double[ ][ ] der)
  {
    for (int i=0; i<3; ++i) {
      izq[0][i] = (8*curva[0][i]                                          )/8;
      izq[1][i] = (4*curva[0][i]+4*curva[1][i]                            )/8;
      izq[2][i] = (2*curva[0][i]+4*curva[1][i]+2*curva[2][i]              )/8;
      izq[3][i] = (  curva[0][i]+3*curva[1][i]+3*curva[2][i]+  curva[3][i])/8;
      der[0][i] = (  curva[0][i]+3*curva[1][i]+3*curva[2][i]+  curva[3][i])/8;
      der[1][i] = (              2*curva[1][i]+4*curva[2][i]+2*curva[3][i])/8;
      der[2][i] = (                            4*curva[2][i]+4*curva[3][i])/8;
      der[3][i] = (                                          8*curva[3][i])/8;
    }
  }

  private void trazarCurva (double[ ][ ] curva, Graphics screen) {
    if (recto (curva))
      screen.drawLine (
        ox + (int) Math.round (curva[0][0]),
        oy - (int) Math.round (curva[0][1]),
        ox + (int) Math.round (curva[3][0]),
        oy - (int) Math.round (curva[3][1])
      );
    else {
      double[ ][ ] izq = new double[4][3];
      double[ ][ ] der = new double[4][3];
      dividir (curva, izq, der);
      trazarCurva (izq, screen);
      trazarCurva (der, screen);
    }
  }

// Calcula la mitad de la curva
  public double[ ] mitad ( ) {
    double[ ] m = new double[3];
    for (int i=0; i<3; ++i)
      m[i] = (curva[0][i]+3*curva[1][i]+3*curva[2][i]+curva[3][i])/8;
    return m;
  }

// Cambiar puntos de control, matriz 4 x 3
  public void puntos (double[ ][ ] nueva) {
    for (int i=0; i<4; ++i)
      for (int j=0; j<3; ++j)
        curva[i][j] = nueva[i][j];
  }

// Trazar curva de Bezier
  public void trazar (int origenX, int origenY, Color c, Graphics g) {
    ox = origenX;
    oy = origenY;
    g.setColor (c);
    trazarCurva (curva, g);
  }
}

