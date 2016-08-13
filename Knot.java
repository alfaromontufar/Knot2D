import java.util.LinkedList;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;



public class Knot extends JPanel
    implements ActionListener, MouseListener, MouseMotionListener {
    
    class Bezier3D {
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


    class Nodo {

	public int x;
	public int y;
	public int z;

	public Nodo(){ }

	public boolean equals(Nodo Aux){
	    if(x == Aux.x && y == Aux.y && z == Aux.z)
		return true;	
	    return false;
	}

	public void equalize(Nodo Aux){
	    x=Aux.x;
	    y=Aux.y;
	    z=Aux.z;
	}

	public void rotate90(){
	    int aux=x;
	    x=-y;
	    y=aux;
	}

	public void rotateh(){
	    y=-y;
	    //z=-z;
	}

	public void moveX(int auxInt){	
	    x += auxInt;
	}

	public void imprime(){
	    System.out.println( x + " " + y + " " + z );
	}
    
    }

    JButton button = new JButton("draw");
    JTextField text = new JTextField("2,3,1",20);
    private int N = 0;   // Number of nodes, N>=3
    private int N1 = 0;   // Number of nodes, N>=3
    private double R= 50;   // Radio
    private double R1= 50;   // Radio
    private double oldR;
    private double oldR1;
    private final int C = 2;   // Mouse node size
    private final int CC = 4;   // Mouse control size
    private final double E = 1;   // Bezier draw error
    private final double B = 0.25;   // Bezier tangent relative length
    private int[ ][ ] control = new int[7][2];
    private int[ ][ ] node;
    private int[ ][ ] node1;
    private int[ ][ ] oldNode;
    private int[ ][ ] oldNode1;
    private Bezier3D[ ] bezier;
    private Bezier3D[ ] bezier1;
    private int cx, cy, selection, selection1, conSel;
    private boolean start = true;
    private String cadena;
    
    private void FuntionNodes (){
	
	//// Calculating number of Nodes
	
	int nNumbers = 1;
	
	for(int i=0; i<cadena.length(); ++i)
	    if(cadena.charAt(i)== ',')
		nNumbers++;
	
	int[ ] number = new int [nNumbers];	
	
	int index=0;
	
	for( int i=0; cadena.indexOf(',',index) != -1; ++i){	    
	    number[i] = Integer.parseInt(cadena.substring(index,cadena.indexOf(',',index)));	    	    	   
	    index = cadena.indexOf(',',index) + 1;	    
	}
	
	number[nNumbers-1] = Integer.parseInt(cadena.substring(index,cadena.length()));	
	
	//// Filling Nodes
	
	int sum = 0;
	
	Nodo nE = new Nodo();
	Nodo nW = new Nodo();
	Nodo sE = new Nodo();
	Nodo sW = new Nodo();
	
	LinkedList listA = new LinkedList();
	LinkedList listB = new LinkedList();
	
	for(int iNumber = 0 ; iNumber < nNumbers ; ++iNumber){


	    Nodo nEaux = new Nodo();
	    Nodo nWaux = new Nodo();
	    Nodo sEaux = new Nodo();
	    Nodo sWaux = new Nodo();

	    LinkedList listAaux = new LinkedList();
	    LinkedList listBaux = new LinkedList();

	    if(number[iNumber]>0){		
 	    		
		for (int i=0; i<2*number[iNumber]+1; ++i) {
		    Nodo Aux = new Nodo();
		    Aux.x = (int)( (R/Math.sqrt(2)) * (i-number[iNumber]-1/2) );
		    Aux.y = (int)( (R/Math.sqrt(2)) * Math.cos(Math.PI*i/2) );
		    Aux.z = (int)( -(R/Math.sqrt(2)) * Math.sin(Math.PI*i/2) );
		    listAaux.addLast(Aux);
		    if( i == 0 )
			nWaux.equalize(Aux);		       
		    if( i == 2*number[iNumber] && number[iNumber]%2!=0)
			sEaux.equalize(Aux);
		    if( i == 2*number[iNumber] && number[iNumber]%2==0)
			nEaux.equalize(Aux);
		}
		
		for (int i=0; i<2*number[iNumber]+1; ++i) {
		    Nodo Aux = new Nodo();
		    Aux.x = (int)(  (R1/Math.sqrt(2)) * (i-number[iNumber]-1/2) );
		    Aux.y = (int)( -(R1/Math.sqrt(2)) * Math.cos(Math.PI*i/2) );
		    Aux.z = (int)( (R1/Math.sqrt(2)) * Math.sin(Math.PI*i/2) );
		    listBaux.addLast(Aux);
		    if( i == 0 )
			sWaux.equalize(Aux);
		    if( i == 2*number[iNumber] && number[iNumber]%2!=0)
			nEaux.equalize(Aux);
		    if( i == 2*number[iNumber] && number[iNumber]%2==0)
			sEaux.equalize(Aux);
		}
	    }	    
	    else if(number[iNumber]<0){

		for (int i=0; i<2*Math.abs(number[iNumber])+1; ++i) {
		    Nodo Aux = new Nodo();
		    Aux.x = (int)(  (R/Math.sqrt(2)) * (i+number[iNumber]-1/2) );
		    Aux.y = (int)(  (R/Math.sqrt(2)) * Math.cos(Math.PI*i/2) );
		    Aux.z = (int)( (R/Math.sqrt(2)) * Math.sin(Math.PI*i/2) );
		    listAaux.addLast(Aux);
		    if( i == 0 )
			nWaux.equalize(Aux);
		    if( i == -2*number[iNumber] && number[iNumber]%2!=0)
			sEaux.equalize(Aux);
		    if( i == -2*number[iNumber] && number[iNumber]%2==0)
			nEaux.equalize(Aux);
		}
		
		for (int i=0; i<2*Math.abs(number[iNumber])+1; ++i) {
		    Nodo Aux = new Nodo();
		    Aux.x = (int)(  (R1/Math.sqrt(2)) * (i+number[iNumber]-1/2) );
		    Aux.y = (int)(-(R1/Math.sqrt(2)) * Math.cos(Math.PI*i/2) );
		    Aux.z = (int)( -(R1/Math.sqrt(2)) * Math.sin(Math.PI*i/2) );
		    listBaux.addLast(Aux);
		    if( i == 0 )
			sWaux.equalize(Aux);
		    if( i == -2*number[iNumber] && number[iNumber]%2!=0)
			nEaux.equalize(Aux);
		    if( i == -2*number[iNumber] && number[iNumber]%2==0)
			sEaux.equalize(Aux);
		}
	    }
	    else{ //// i.e. number[iNumber] == 0

		for (int i=0; i< 3 ; ++i){
		    Nodo Aux = new Nodo();
		    Aux.x = (int)(R/Math.sqrt(2))*(i-3/2);
		    Aux.y = (int)( (R/(Math.sqrt(2)*3)) * (2+Math.pow(-1,i)) );
		    Aux.z = 0;
		    listAaux.addLast(Aux);
		    if( i == 0 )
			nWaux.equalize(Aux);
		    if( i == 2)
			nEaux.equalize(Aux);
		}
		
		for (int i=0; i < 3 ; ++i){
		    Nodo Aux = new Nodo();
		    Aux.x = (int)(R/Math.sqrt(2))*(i-3/2);
		    Aux.y = (int)( -(R/(Math.sqrt(2)*3)) * (2+Math.pow(-1,i)) );
		    Aux.z = 0;
		    listBaux.addLast(Aux);
		    if( i == 0 )
			sWaux.equalize(Aux);
		    if( i == 2)
			sEaux.equalize(Aux);
		}		
	    }	   	   

	    if(iNumber == 0){
		
		nW.equalize(nWaux);
		nE.equalize(nEaux);
		sW.equalize(sWaux);
		sE.equalize(sEaux);
		
		while(listAaux.size() > 0){
		    listA.addLast( (Nodo) listAaux.removeFirst() );
		    listB.addLast( (Nodo) listBaux.removeFirst() );
		}

	    }else{  //// More than 1 numbers	       		
		
		//// Rotate 90 grades
		
		for(int i=0; i<listA.size() ; ++i){
		    
		    Nodo aux = new Nodo();		    		   
		    
		    aux = (Nodo) listA.removeFirst();
		    aux.rotate90();
		    listA.addLast(aux);
		    
		}

		for(int i=0; i<listB.size() ; ++i){
		    
		    Nodo aux = new Nodo();

		    aux = (Nodo) listB.removeFirst();
		    aux.rotate90();
		    listB.addLast(aux);
		    
		}
		
		Nodo auxiliar = new Nodo();

		nW.rotate90();
		nE.rotate90();
		sE.rotate90();
		sW.rotate90();

		auxiliar.equalize(nW);
		nW.equalize(nE);
		nE.equalize(sE);
		sE.equalize(sW);
		sW.equalize(auxiliar);	

		//// Rotate horizontal axis & - operation				
		
		for(int i=0; i<listA.size() ; ++i){
		    
		    Nodo aux = new Nodo();
		    
		    aux = (Nodo) listA.removeFirst();
		    aux.rotateh();
		    listA.addLast(aux);

		}

		for(int i=0; i<listB.size() ; ++i){
		    
		    Nodo aux = new Nodo();
    
		    aux = (Nodo) listB.removeFirst();
		    aux.rotateh();
		    listB.addLast(aux);
		    
		}

		nW.rotateh();
		nE.rotateh();
		sE.rotateh();
		sW.rotateh();

		auxiliar.equalize(nW);
		nW.equalize(sW);
		sW.equalize(auxiliar);
		auxiliar.equalize(nE);
		nE.equalize(sE);
		sE.equalize(auxiliar);
		
		//// Move to origin

		int wide1 = (nE.x>sE.x)? (int) ( nE.x + (R/Math.sqrt(2)) ): (int) ( sE.x + (R/Math.sqrt(2)) );
		int wide2= (nEaux.x > sEaux.x)? (int) ( nEaux.x + (R/Math.sqrt(2)) ): (int) ( sEaux.x + (R/Math.sqrt(2)) );		
		
		for(int i=0; i<listAaux.size() ; ++i){
		    
		    Nodo aux = new Nodo();
		    
		    aux = (Nodo) listAaux.removeFirst();
		    aux.moveX(wide1);
		    listAaux.addLast(aux);

		}
		    
		for(int i=0; i<listBaux.size() ; ++i){
		    
		    Nodo aux = new Nodo();

		    aux = (Nodo) listBaux.removeFirst();
		    aux.moveX(wide1);
		    listBaux.addLast(aux);
		    
		}
		    
		for(int i=0; i<listA.size() ; ++i){
		    
		    Nodo aux = new Nodo();

		    aux = (Nodo) listA.removeFirst();
		    aux.moveX(-wide2);
		    listA.addLast(aux);

		}
		
		for(int i=0; i<listB.size() ; ++i){
		    
		    Nodo aux = new Nodo();
		    
		    aux = (Nodo) listB.removeFirst();
		    aux.moveX(-wide2);
		    listB.addLast(aux);
		    
		}

		nW.moveX(-wide2);
		nE.moveX(-wide2);
		sE.moveX(-wide2);
		sW.moveX(-wide2);
		nWaux.moveX(wide1);
		nEaux.moveX(wide1);
		sEaux.moveX(wide1);
		sWaux.moveX(wide1);

		if(nE.equals( (Nodo) listA.getFirst()))
		    for(int i=1; i < listA.size(); i++)		
			listA.addFirst( (Nodo) listA.remove(i) );
		
		if(nE.equals( (Nodo) listB.getFirst()))
		    for(int i=1; i < listB.size(); i++)		
			listB.addFirst( (Nodo) listB.remove(i) );
		
		if(nWaux.equals( (Nodo) listAaux.getLast()))
		    for(int i=1; i < listAaux.size(); i++)		
			listAaux.addFirst( (Nodo) listAaux.remove(i) );
		
		if(nWaux.equals( (Nodo) listBaux.getLast()))
		    for(int i=1; i < listBaux.size(); i++)		
			listBaux.addFirst( (Nodo) listBaux.remove(i) );
		
// 		System.out.println();
// 		System.out.print(" NW      _  ");
// 		nW.imprime();
// 		System.out.print(" NE      _  ");
// 		nE.imprime();
// 		System.out.print(" SW      _  ");
// 		sW.imprime();
// 		System.out.print(" SE      _  ");
// 		sE.imprime();
// 		System.out.print(" AFirst  _  ");
// 		((Nodo) listA.getFirst()).imprime();
// 		System.out.print(" ALast   _  ");
// 		((Nodo) listA.getLast()).imprime();
// 		System.out.print(" BFirst  _  ");
// 		((Nodo) listB.getFirst()).imprime();
// 		System.out.print(" BLast   _  ");
// 		((Nodo) listB.getLast()).imprime();
// 		System.out.println();
// 		System.out.print(" NWaux   _  ");
// 		nWaux.imprime();
// 		System.out.print(" NEaux   _  ");
// 		nEaux.imprime();
// 		System.out.print(" SWaux   _  ");
// 		sWaux.imprime();
// 		System.out.print(" SEaux   _  ");
// 		sEaux.imprime();
// 		System.out.print(" AauxFirst  ");
// 		((Nodo) listAaux.getFirst()).imprime();
// 		System.out.print(" AauxLast   ");
// 		((Nodo) listAaux.getLast()).imprime();
// 		System.out.print(" BauxFirst  ");
// 		((Nodo) listBaux.getFirst()).imprime();
// 		System.out.print(" BauxLast   ");
// 		((Nodo) listBaux.getLast()).imprime();
		
		System.out.print("  >>  ");

		if(nE.equals( (Nodo) listA.getLast())){ ///
		    System.out.print(" A ");
		    if(nWaux.equals( (Nodo) listAaux.getFirst() )){ //
			System.out.print(" 1 ");
			while(listAaux.size() > 0)
			    listA.addLast( (Nodo) listAaux.removeFirst() );
			
			if(sWaux.equals( (Nodo) listA.getLast() )){
			    if(sE.equals( (Nodo) listB.getFirst() )){
				
				while(listB.size() > 0)
				    listA.addLast( (Nodo) listB.removeFirst() );
				
				while(listBaux.size() > 0)
				    listB.addLast( (Nodo) listBaux.removeFirst() );
				
			    }else if( sE.equals( (Nodo) listB.getLast() ) ){
				
				while(listB.size() > 0)
				    listA.addLast( (Nodo) listB.removeLast() );
				
				while(listBaux.size() > 0)
				    listB.addLast( (Nodo) listBaux.removeFirst() );
				
			    }else if( sE.equals( (Nodo) listA.getFirst() ) )
				System.out.println( "Error SE(A) #1" );
			}
			
			if(sE.equals( (Nodo) listA.getFirst() )){ ///

			    if(sWaux.equals( (Nodo) listBaux.getLast() ))
				for(int i=1; i < listBaux.size(); i++)		
				    listBaux.addFirst( (Nodo) listBaux.remove(i) );

			    if(sWaux.equals( (Nodo) listBaux.getFirst() ))			
				while(listBaux.size() > 0)
				    listA.addFirst( (Nodo) listBaux.removeFirst() );
			    
			}
			
			if(listB.size() > 0 && sE.equals( (Nodo) listB.getFirst()))
			    for(int i=1; i < listB.size(); i++)		
				listB.addFirst( (Nodo) listB.remove(i) );
			
			if(listBaux.size() > 0 && sWaux.equals( (Nodo) listBaux.getLast()))
			    for(int i=1; i < listBaux.size(); i++)		
				listBaux.addFirst( (Nodo) listBaux.remove(i) ); 		
			
			if(listB.size() > 0 && sE.equals( (Nodo) listB.getLast()))
			    if(sWaux.equals( (Nodo) listBaux.getFirst() ))
				while(listBaux.size() > 0)
				    listB.addLast( (Nodo) listBaux.removeFirst() );		       
			
		    }else if(nWaux.equals( (Nodo) listBaux.getFirst() )){ //
			System.out.print(" 2 ");

			while(listBaux.size() > 0)
			    listA.addLast( (Nodo) listBaux.removeFirst() );
			
			if(sWaux.equals( (Nodo) listA.getLast() )){
			    if(sE.equals( (Nodo) listB.getFirst() )){
				
				while(listB.size() > 0)
				    listA.addLast( (Nodo) listB.removeFirst() );
				
				while(listAaux.size() > 0)
				    listB.addLast( (Nodo) listAaux.removeFirst() );
				
			    }else if( sE.equals( (Nodo) listB.getLast() ) ){
				
				while(listB.size() > 0)
				    listA.addLast( (Nodo) listB.removeLast() );
				
				while(listAaux.size() > 0)
				    listB.addLast( (Nodo) listAaux.removeFirst() );
				
			    }else if( sE.equals( (Nodo) listA.getFirst() ) )
				System.out.println( "Error SE(A) #1" );
			}
			
			if(sE.equals( (Nodo) listA.getFirst() )){ /////Corregir			   
			    if(sWaux.equals( (Nodo) listAaux.getLast() ))
				for(int i=1; i < listAaux.size(); i++)		
				    listAaux.addFirst( (Nodo) listAaux.remove(i) );

			    if(sWaux.equals( (Nodo) listAaux.getFirst() ))			
				while(listAaux.size() > 0)
				    listA.addFirst( (Nodo) listAaux.removeFirst() );
			    
			}

			if(listB.size() > 0 && sE.equals( (Nodo) listB.getFirst()))
			    for(int i=1; i < listB.size(); i++)		
				listB.addFirst( (Nodo) listB.remove(i) );
			
			if(listAaux.size() > 0 && sW.equals( (Nodo) listAaux.getLast()))
			    for(int i=1; i < listAaux.size(); i++)		
				listAaux.addFirst( (Nodo) listAaux.remove(i) );
			
			if(sE.equals( (Nodo) listB.getLast()))
			    if(sW.equals( (Nodo) listAaux.getFirst() ))
				while(listAaux.size() > 0)
				    listB.addLast( (Nodo) listAaux.removeFirst() );								
			
		    }
		    
		} else if(nE.equals( (Nodo) listB.getLast())){ ///
		    System.out.print(" B ");

		    if(nWaux.equals( (Nodo) listAaux.getFirst() )){ //
			System.out.print(" 1 ");
			
			while(listAaux.size() > 0)
			    listB.addLast( (Nodo) listAaux.removeFirst() );
			
			if(sWaux.equals( (Nodo) listB.getLast() )){
			    if(sE.equals( (Nodo) listA.getFirst() )){
				
				while(listA.size() > 0)
				    listB.addLast( (Nodo) listA.removeFirst() );
				
				while(listBaux.size() > 0)
				    listA.addLast( (Nodo) listBaux.removeFirst() );
				
			    }else if( sE.equals( (Nodo) listA.getLast() ) ){
				
				while(listA.size() > 0)
				    listB.addLast( (Nodo) listA.removeLast() );
				
				while(listBaux.size() > 0)
				    listA.addLast( (Nodo) listBaux.removeFirst() );
				
			    }else if( sE.equals( (Nodo) listB.getFirst() ) )
				System.out.println( "Error SE(A) #1" );
			}		

			if(sE.equals( (Nodo) listB.getFirst() )){
			    System.out.print( "Entro B1" );

			    if(sWaux.equals( (Nodo) listBaux.getLast() ))
				for(int i=1; i < listBaux.size(); i++)		
				    listBaux.addFirst( (Nodo) listBaux.remove(i) );

			    if(sWaux.equals( (Nodo) listBaux.getFirst() ))	       
				while(listBaux.size() > 0)
				    listB.addFirst( (Nodo) listBaux.removeFirst() );
			    
			}

			if(listA.size() > 0 && sE.equals( (Nodo) listA.getFirst()))
			    for(int i=1; i < listA.size(); i++)		
				listA.addFirst( (Nodo) listA.remove(i) );
			
			if(listBaux.size() > 0 && sWaux.equals( (Nodo) listBaux.getLast()))
			    for(int i=1; i < listBaux.size(); i++)		
				listBaux.addFirst( (Nodo) listBaux.remove(i) );
			
			if(sE.equals( (Nodo) listA.getLast()))
			    if(sWaux.equals( (Nodo) listBaux.getFirst() ))
				while(listBaux.size() > 0)
				    listA.addLast( (Nodo) listBaux.removeFirst() );								
			
		    }else if(nWaux.equals( (Nodo) listBaux.getFirst() )){ //		       
			System.out.print(" 2 ");
			
			while(listBaux.size() > 0)
			    listB.addLast( (Nodo) listBaux.removeFirst() );
			
			if(sWaux.equals( (Nodo) listB.getLast() )){
			    if(sE.equals( (Nodo) listA.getFirst() )){
				
				while(listA.size() > 0)
				    listB.addLast( (Nodo) listA.removeFirst() );
				
				while(listAaux.size() > 0)
				    listA.addLast( (Nodo) listAaux.removeFirst() );
				
			    }else if( sE.equals( (Nodo) listA.getLast() ) ){
				
				while(listA.size() > 0)
				    listB.addLast( (Nodo) listA.removeLast() );
				
				while(listAaux.size() > 0)
				    listA.addLast( (Nodo) listAaux.removeFirst() );
				
			    }else if( sE.equals( (Nodo) listB.getFirst() ) )
				System.out.println( "Error SE(A) #2" );
			}
			
			if(sE.equals( (Nodo) listB.getFirst() )){
			    System.out.print( "Entro B2" );

			    if(sWaux.equals( (Nodo) listAaux.getLast() ))
				for(int i=1; i < listAaux.size(); i++)		
				    listBaux.addFirst( (Nodo) listAaux.remove(i) );

			    if(sWaux.equals( (Nodo) listAaux.getFirst() ))
				while(listAaux.size() > 0)
				    listB.addFirst( (Nodo) listAaux.removeFirst() );
			    
			}

			if(listA.size() > 0 && sE.equals( (Nodo) listA.getFirst()))
			    for(int i=1; i < listA.size(); i++)		
				listA.addFirst( (Nodo) listA.remove(i) );
			
			if(listAaux.size() > 0 && sW.equals( (Nodo) listAaux.getLast()))
			    for(int i=1; i < listAaux.size(); i++)		
				listAaux.addFirst( (Nodo) listAaux.remove(i) );
			
			if(sE.equals( (Nodo) listA.getLast()))
			    if(sW.equals( (Nodo) listAaux.getFirst() ))
				while(listAaux.size() > 0)
				    listA.addLast( (Nodo) listAaux.removeFirst() );								
		    }
		}else 
		    System.out.print(" Error #3 Puntos Cardinales");		    							

		System.out.println();
		nE.equalize(nEaux);
		sE.equalize(sEaux);

	    }
	}

	N = listA.size();
	N1 = listB.size();

	System.out.println(" N = " + N + "  N1 = " + N1);

	node = new int[N][3];
	node1 = new int[N1][3];

	Nodo Aux = new Nodo();
	
	for(int i=0; i<N; i++){
	    Aux  = (Nodo) listA.removeLast();
	    node[i][0] = Aux.x;
	    node[i][1] = Aux.y;
	    node[i][2] = Aux.z;
	}

	for(int i=0; i<N1; i++){
	    Aux = (Nodo) listB.removeFirst();
	    node1[i][0] = Aux.x;
	    node1[i][1] = Aux.y;
	    node1[i][2] = Aux.z;	    
	}

	oldNode = new int[N][3];
	bezier = new Bezier3D[N-1];

	for (int i=0; i<(N-1); ++i) bezier[i] = new Bezier3D (E);
	
	oldNode1 = new int[N1][3];
	bezier1 = new Bezier3D[N1-1];

	for (int i=0; i<(N1-1); ++i) bezier1[i] = new Bezier3D (E);

	setBezierSegments ( );

    }
    
    public Knot (String str1 ) {
        text.addActionListener(this);
	button.addActionListener(this);
        add(text);
 	add(button);
	cadena = str1;
	
	FuntionNodes();

	addMouseListener (this);
	addMouseMotionListener (this);
    }    

    private void setBezierSegments ( ) {
	double[ ][ ] b = new double[4][3];

	for (int i=0; i<(N-1); ++i) {
	    for (int j=0; j<3; ++j) {
		b[0][j] = node[i][j];
		b[1][j] = node[i][j] + (node[i+1][j] - node[ (i == 0 ? 0 : i-1) ][j])*B;
		b[2][j] = node[i+1][j] - (node[ (i == N-2 ? N-1 : i+2) ][j] - node[i][j])*B;
		b[3][j] = node[i+1][j];
	    }
	    bezier[i].puntos (b);
	}

	for (int i=0; i<(N1-1); ++i) {
	    for (int j=0; j<3; ++j) {
		b[0][j] = node1[i][j];
		b[1][j] = node1[i][j] + (node1[i+1][j] - node1[ (i == 0 ? 0 : i-1) ][j])*B;
		b[2][j] = node1[i+1][j] - (node1[ (i == N1-2 ? N1-1 : i+2) ][j] - node1[i][j])*B;
		b[3][j] = node1[i+1][j];
	    }
	    bezier1[i].puntos (b);
	}

    }

    private void saveNode ( ) {
	oldR = R;
	oldR1 = R1;
	for (int i=0; i<N; ++i)
	    for (int j=0; j<3; ++j)
		oldNode[i][j] = node[i][j];
	
	for (int i=0; i<N1; ++i)
	    for (int j=0; j<3; ++j)
		oldNode1[i][j] = node1[i][j];
    }

    private void resetControl ( ) {
	control[0][0] = control[6][0] - 12;
	control[0][1] = control[6][1] + 12;
	control[1][0] = control[0][0] + 24;
	control[1][1] = control[0][1];
	control[2][0] = control[0][0];
	control[2][1] = control[0][1] + 24;
	control[3][0] = control[0][0] - 24;
	control[3][1] = control[0][1];
	control[4][0] = control[0][0];
	control[4][1] = control[0][1] - 24;
	control[5][0] = control[0][0];
	control[5][1] = control[0][1];
    }

    private void trans3D ( ) {
	int dx = control[conSel][0] - control[0][0];
	int dy = control[conSel][1] - control[0][1];
	double r = Math.sqrt (dx*dx + dy*dy);
	double sinA, cosA;
	switch (conSel) {
	case 1:   // Rotate X
	    sinA = dy / r;
	    cosA = dx / r;
	    for (int i=0; i<N; ++i) {
		node[i][1] = (int) (oldNode[i][1]*cosA - oldNode[i][2]*sinA);
		node[i][2] = (int) (oldNode[i][1]*sinA + oldNode[i][2]*cosA);
	    }
	    for (int i=0; i<N1; ++i) {
		node1[i][1] = (int) (oldNode1[i][1]*cosA - oldNode1[i][2]*sinA);
		node1[i][2] = (int) (oldNode1[i][1]*sinA + oldNode1[i][2]*cosA);
	    }
	    break;
	case 2:   // Rotate Y
	    sinA = -dx / r;
	    cosA = dy / r;
	    for (int i=0; i<N; ++i) {
		node[i][2] = (int) (oldNode[i][2]*cosA - oldNode[i][0]*sinA);
		node[i][0] = (int) (oldNode[i][2]*sinA + oldNode[i][0]*cosA);
	    }
	    for (int i=0; i<N1; ++i) {
		node1[i][2] = (int) (oldNode1[i][2]*cosA - oldNode1[i][0]*sinA);
		node1[i][0] = (int) (oldNode1[i][2]*sinA + oldNode1[i][0]*cosA);
	    }
	    break;
	case 3:   // Rotate Z
	    sinA = -dy / r;
	    cosA = -dx / r;
	    for (int i=0; i<N; ++i) {
		node[i][0] = (int) (oldNode[i][0]*cosA - oldNode[i][1]*sinA);
		node[i][1] = (int) (oldNode[i][0]*sinA + oldNode[i][1]*cosA);
	    }
	    for (int i=0; i<N1; ++i) {
		node1[i][0] = (int) (oldNode1[i][0]*cosA - oldNode1[i][1]*sinA);
		node1[i][1] = (int) (oldNode1[i][0]*sinA + oldNode1[i][1]*cosA);
	    }
	    break;
	case 4:   // Scale
	    double s = r / 24;
	    if (s > 0.1) {
		R = oldR * s;
		for (int i=0; i<N; ++i)
		    for (int j=0; j<3; ++j)
			node[i][j] = (int)(oldNode[i][j] * s);
		
		R1 = oldR1 * s;
		for (int i=0; i<N1; ++i)
		    for (int j=0; j<3; ++j)
			node1[i][j] = (int)(oldNode1[i][j] * s);
	    }
	    break;
	case 5:   // Move
	    for (int i=0; i<N; ++i) {
		node[i][0] = oldNode[i][0] + dx;
		node[i][1] = oldNode[i][1] + dy;
	    }
	    for (int i=0; i<N1; ++i) {
		node1[i][0] = oldNode1[i][0] + dx;
		node1[i][1] = oldNode1[i][1] + dy;
	    }
	    break;
	case 6:   // Move control
	    resetControl ( );
	    break;
	}
    }

    private void computeNodes ( ) {
	for (int i = selection; i > 0; --i) {
	    double x = node[i][0] - node[i-1][0];
	    double y = node[i][1] - node[i-1][1];
	    double z = node[i][2] - node[i-1][2];
	    double r = Math.sqrt (x*x + y*y + z*z);
	    double x2 = x * (1 - R/r);
	    double y2 = y * (1 - R/r);
	    double z2 = z * (1 - R/r);
	    node[i-1][0] += (int)x2;
	    node[i-1][1] += (int)y2;
	    node[i-1][2] += (int)z2;
	}
	for (int i = selection; i < (N-1); ++i) {
	    double x = node[i][0] - node[i+1][0];
	    double y = node[i][1] - node[i+1][1];
	    double z = node[i][2] - node[i+1][2];
	    double r = Math.sqrt (x*x + y*y + z*z);
	    double x2 = x * (1 - R/r);
	    double y2 = y * (1 - R/r);
	    double z2 = z * (1 - R/r);
	    node[i+1][0] += (int)x2;
	    node[i+1][1] += (int)y2;
	    node[i+1][2] += (int)z2;
	}
    }

    private void computeNodes1 ( ) {
	for (int i = selection1; i > 0; --i) {
	    double x = node1[i][0] - node1[i-1][0];
	    double y = node1[i][1] - node1[i-1][1];
	    double z = node1[i][2] - node1[i-1][2];
	    double r = Math.sqrt (x*x + y*y + z*z);
	    double x2 = x * (1 - R1/r);
	    double y2 = y * (1 - R1/r);
	    double z2 = z * (1 - R1/r);
	    node1[i-1][0] += (int)x2;
	    node1[i-1][1] += (int)y2;
	    node1[i-1][2] += (int)z2;
	}
	for (int i = selection1; i < (N1-1); ++i) {
	    double x = node1[i][0] - node1[i+1][0];
	    double y = node1[i][1] - node1[i+1][1];
	    double z = node1[i][2] - node1[i+1][2];
	    double r = Math.sqrt (x*x + y*y + z*z);
	    double x2 = x * (1 - R1/r);
	    double y2 = y * (1 - R1/r);
	    double z2 = z * (1 - R1/r);
	    node1[i+1][0] += (int)x2;
	    node1[i+1][1] += (int)y2;
	    node1[i+1][2] += (int)z2;
	}
    }

    public void paintComponent (Graphics g) {
	int w=getSize ( ).width;
	int h=getSize ( ).height;
	cx = w/2;
	cy = h/2;
	if (start) {
	    control[6][0] = cx/2;
	    control[6][1] = -cy/2;
	    resetControl ( );
	    start = false;
	}

	// Clean screen
	g.setColor (Color.white);
	g.fillRect (0, 0, w, h);

	// Draw control
	g.setColor (Color.black); ////
	for (int i=1; i<7; ++i)
	    g.drawLine (
			cx + control[0][0], cy - control[0][1],
			cx + control[i][0], cy - control[i][1]
			);
	g.fillOval (cx + control[0][0] - CC, cy - control[0][1] - CC, 2*CC, 2*CC);

	g.setColor (Color.white); ///////	
	g.fillOval (cx + control[1][0] - CC, cy - control[1][1] - CC, 2*CC, 2*CC);
	g.setColor (Color.red);
	g.drawOval (cx + control[1][0] - CC, cy - control[1][1] - CC, 2*CC, 2*CC);

	g.setColor (Color.white); ///////
	g.fillOval (cx + control[2][0] - CC, cy - control[2][1] - CC, 2*CC, 2*CC);
	g.setColor (Color.green); 
	g.drawOval (cx + control[2][0] - CC, cy - control[2][1] - CC, 2*CC, 2*CC);

	g.setColor (Color.white); ///////
	g.fillOval (cx + control[3][0] - CC, cy - control[3][1] - CC, 2*CC, 2*CC);
	g.setColor (Color.blue); 
	g.drawOval (cx + control[3][0] - CC, cy - control[3][1] - CC, 2*CC, 2*CC);
       
	g.setColor (Color.white); ///////
	g.fillRect (cx + control[4][0] - CC, cy - control[4][1] - CC, 2*CC, 2*CC);
	g.setColor (Color.black); 
	g.drawRect (cx + control[4][0] - CC, cy - control[4][1] - CC, 2*CC, 2*CC);

	g.setColor (Color.white); ///////
	g.fillOval (cx + control[5][0] - CC, cy - control[5][1] - CC, 2*CC, 2*CC);
	g.setColor (Color.black); 
	g.drawOval (cx + control[5][0] - CC, cy - control[5][1] - CC, 2*CC, 2*CC);

	g.setColor (Color.white); ///////
	g.fillOval (cx + control[6][0] - CC, cy - control[6][1] - CC, 2*CC, 2*CC);
	g.setColor (Color.black); 
	g.drawOval (cx + control[6][0] - CC, cy - control[6][1] - CC, 2*CC, 2*CC);	

	// Draw bezier segments
	for (int i=0; i<(N-1); ++i)
	    bezier[i].trazar (cx, cy, Color.black, g);

	for (int i=0; i<(N1-1); ++i)
	    bezier1[i].trazar (cx, cy, Color.black, g);

	// Draw nodes
	g.setColor (Color.white);
	for (int i=0; i<N; ++i)
	    g.fillRect (cx+node[i][0]-C, cy-node[i][1]-C, 2*C, 2*C);

	for (int i=0; i<N1; ++i)
	    g.fillRect (cx+node1[i][0]-C, cy-node1[i][1]-C, 2*C, 2*C);

	g.setColor (Color.black);
	for (int i=0; i<N; ++i)
	    g.drawRect (cx+node[i][0]-C, cy-node[i][1]-C, 2*C, 2*C);

	for (int i=0; i<N1; ++i)
	    g.drawRect (cx+node1[i][0]-C, cy-node1[i][1]-C, 2*C, 2*C);

	///////
	///g.setColor (Color.white);
	///g.fillRect (-60, 10, 60, -10);
    }

    public void mousePressed (MouseEvent event) {
	event.consume ( );
	int x = event.getX ( ) - cx;
	int y = cy - event.getY ( );
	conSel = -1;
	selection = -1;
	selection1 = -1;
	for (int i=1; i<7; ++i)
	    if (Math.abs (x-control[i][0]) <= CC && Math.abs (y-control[i][1]) <= CC) {
		conSel = i;
		if (conSel < 6) saveNode ( );
		return;
	    }
	for (int i=0; i<N; ++i)
	    if (Math.abs (x-node[i][0]) <= C && Math.abs (y-node[i][1]) <= C) {
		selection = i;
		return;
	    }
	for (int i=0; i<N1; ++i)
	    if (Math.abs (x-node1[i][0]) <= C && Math.abs (y-node1[i][1]) <= C) {
		selection1 = i;
		return;
	    }
    }

    public void mouseDragged (MouseEvent event) {
	event.consume ( );
	if (conSel >= 0) {
	    control[conSel][0] = event.getX ( ) - cx;
	    control[conSel][1] = cy - event.getY ( );
	    trans3D ( );
	    setBezierSegments ( );
	    repaint ( );
	}
	if (selection >= 0) {
	    node[selection][0] = event.getX ( ) - cx;
	    node[selection][1] = cy - event.getY ( );
	    computeNodes ( );
	    setBezierSegments ( );
	    repaint ( );
	}
	if (selection1 >= 0) {
	    node1[selection1][0] = event.getX ( ) - cx;
	    node1[selection1][1] = cy - event.getY ( );
	    computeNodes1 ( );
	    setBezierSegments ( );
	    repaint ( );
	}
    }

    public void mouseReleased (MouseEvent event) {
	event.consume ( );
	if (conSel >= 0) {
	    resetControl ( );
	    repaint ( );
	}
    }

    public void mouseClicked (MouseEvent event) { }
    public void mouseEntered (MouseEvent event) { }
    public void mouseExited (MouseEvent event) { }
    public void mouseMoved (MouseEvent event) { }

    public void actionPerformed (ActionEvent event) {
        Object s = event.getSource( );
	
        if (s == button) {
	    cadena=text.getText( );	    
	    FuntionNodes();	    
	    repaint( );
        } else if (s == text){
	    cadena=text.getText( );	    
	    FuntionNodes();	    
	    repaint( );
	}
    }
    
    public static void main (String[ ] args) {
	Knot kn = new Knot ("2,3,1");
	JFrame jf = new JFrame ( );
	jf.setTitle ("Tangles");
	jf.setSize (800, 800);
	jf.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
	jf.setContentPane (kn);
	jf.show ( );
    }
}
