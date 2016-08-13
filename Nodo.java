public class Nodo {

    public int x;
    public int y;
    public int z;

    public Nodo(){

	// x=0;
// 	y=0;
// 	z=0;
	
    }

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
