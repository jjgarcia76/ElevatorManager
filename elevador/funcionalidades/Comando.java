//Hay que revisar como JP hace las importaciones a otras clases y los paquetes
package elevador.funcionalidades;

import elevador.modelo.Tipos.Direccion;
import elevador.modelo.Tipos.TipoComando;

public class Comando {
    public TipoComando tipo;
    public int piso;
    public Direccion direccionDeseada;

    public Comando(TipoComando tipo, int piso, Direccion direccionDeseada) {
        this.tipo = tipo;
        this.piso = piso;
        this.direccionDeseada = direccionDeseada;
    }

    public static Comando irA(int piso) {
        return new Comando(TipoComando.IR_A, piso, null);
    }
    public static Comando recoger(int piso, Direccion dir) {
        return new Comando(TipoComando.RECOGER, piso, dir);
    }
    public static Comando reset() {
        return new Comando(TipoComando.RESET, 1, null);
    }
    public static Comando apagar() {
        return new Comando(TipoComando.APAGAR, -1, null);
    }
}

