//Hay que revisar como JP hace las importaciones a otras clases y los paquetes
package elevador.modelo;
import elevador.modelo.Tipos.TipoComando;

public class Comando {

    public TipoComando tipo;
    public int piso;

    public Comando(TipoComando tipo, int piso) {
        this.tipo = tipo;
        this.piso = piso;
    }

    public static Comando irA(int piso) {
        return new Comando(TipoComando.IR_A, piso);
    }
}
