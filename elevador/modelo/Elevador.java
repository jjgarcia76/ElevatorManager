//Hay que revisar como JP hace las importaciones a otras clases y los paquetes
package elevador.modelo;

import elevador.modelo.Tipos.Direccion;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Elevador implements Runnable {
    public int id;
    public int pisoMinimo = 1;
    public int pisoMaximo;
    public long tiempoMovimientoMs;   
    public long tiempoEsperaMs;       
    public int pisoActual = 1;
    public Direccion direccion = Direccion.QUIETO;

    public BlockingQueue<Comando> bandeja = new LinkedBlockingQueue<>();
    public TreeSet<Integer> destinos = new TreeSet<>();
    public boolean ejecutando = true;

    public Elevador(int id, int pisoMaximo, long tiempoMovimientoMs, long tiempoEsperaMs) {
        this.id = id;
        this.pisoMaximo = pisoMaximo;
        this.tiempoMovimientoMs = tiempoMovimientoMs;
        this.tiempoEsperaMs = tiempoEsperaMs;
    }

    //envia ordenes al elevador
    public void enviar(Comando cmd) {
        if (cmd == null) return;
        if (cmd.piso < pisoMinimo || cmd.piso > pisoMaximo) {
            System.out.println("Elevador " + id + ": este piso no es valido " + cmd.piso);
            return;
        }
        bandeja.offer(cmd);
    }

    @Override
    public void run() {
        System.out.println("Elevador " + id + " iniciado en piso " + pisoActual);

        while (ejecutando) {

            //1) Drenar comandos entrantes (no bloqueante)
            drenarBandeja();

            //2) Si no hay destinos, quedar en QUIETO y esperar un poco
            if (destinos.isEmpty()) {
                direccion = Direccion.QUIETO;
                dormir(tiempoCorto());
                continue;
            }

            //3) Elegir destino cercano de forma simple
            Integer pisoInferior = destinos.floor(pisoActual); 
            Integer pisoSuperior = destinos.ceiling(pisoActual);

            int objetivo;
            if (pisoInferior == null) {
                objetivo = pisoSuperior;
            } else if (pisoSuperior == null) {
                objetivo = pisoInferior;
            } else {
                int distAbajo = Math.abs(pisoActual - pisoInferior);
                int distArriba = Math.abs(pisoSuperior - pisoActual);
                objetivo = (distArriba <= distAbajo) ? pisoSuperior : pisoInferior;
            }

            //4) Mover un piso hacia el objetivo
            if (objetivo > pisoActual) {
                direccion = Direccion.SUBE;
                dormir(tiempoMovimientoMs);
                pisoActual++;
                System.out.println("Elevador " + id + " sube al piso " + pisoActual);
            } else if (objetivo < pisoActual) {
                direccion = Direccion.BAJA;
                dormir(tiempoMovimientoMs);
                pisoActual--;
                System.out.println("Elevador " + id + " baja al piso " + pisoActual);
            } else {
                //llega a la parada
                destinos.remove(pisoActual);
                System.out.println("Elevador " + id + " llegó al piso " + pisoActual + " (abrir/cerrar puertas)");
                dormir(tiempoEsperaMs);
            }
        }

        System.out.println("Elevador " + id + " detenido.");
    }

    //lee la bandeja y agrega destinos a la lista
    private void drenarBandeja() {
        Comando cmd;
        while ((cmd = bandeja.poll()) != null) {
            if (cmd.piso != pisoActual) {
                destinos.add(cmd.piso);
                System.out.println("Elevador " + id + " nuevo destino agregado: piso " + cmd.piso);
            } else {
                //el elevador se encuentra en el no. de piso
                System.out.println("Elevador " + id + " ya esta en piso " + cmd.piso + " (atendiendo)");
                dormir(tiempoEsperaMs);
            }
        }
    }

    //para pausar el Thread
    private void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            System.out.println("Elevador " + id + " interrupcion: ");
        }
    }

    //pequeña espera cuando no hay algo por hacer
    private long tiempoCorto() {
    //cambiar el numero ya que depende del tiempo de movimiento, se puede cambiar despues
    return tiempoMovimientoMs / 10; 
    }
}
