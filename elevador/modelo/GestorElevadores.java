//Hay que revisar como JP hace las importaciones a otras clases y los paquetes
package elevador.gestor;

import elevador.modelo.Comando;
import elevador.modelo.Elevador;

import java.util.ArrayList;
import java.util.List;

public class GestorElevadores {

    public List<Elevador> elevadores = new ArrayList<>();
    public List<Thread> hilos = new ArrayList<>();

    public GestorElevadores(int cantidadElevadores, int pisos, long tMovimientoMs, long tEsperaMs) {
        for (int i = 1; i <= cantidadElevadores; i++) {
            Elevador e = new Elevador(i, pisos, tMovimientoMs, tEsperaMs);
            elevadores.add(e);
            hilos.add(new Thread(e, "Elevador-" + i));
        }
    }

    public void iniciar() {
        for (Thread t : hilos) {
            t.start();
        }
    }

    public void detener() {
        for (Elevador elevador : elevadores) {
            elevador.ejecutando = false;
        }
    }

    //por ahora envia un destino directo al elevador por un id
    public void irAPiso(int idElevador, int piso) {
        if (idElevador < 1 || idElevador > elevadores.size()) {
            System.out.println("Elevador no valido " + idElevador);
            return;
        }
        elevadores.get(idElevador - 1).enviar(Comando.irA(piso));
    }

    public static void main(String[] args) {
        //configuracion para la demostracion 
        int cantidad = 1;    
        int pisos = 12;       
        long mov = 500;      
        long espera = 900;    

        GestorElevadores app = new GestorElevadores(cantidad, pisos, mov, espera);
        app.iniciar();

        //prueba de destinos para el elevador 1
        app.irAPiso(1, 5);
        app.irAPiso(1, 2);
        app.irAPiso(1, 9);
        app.irAPiso(1, 1);
    }
}
