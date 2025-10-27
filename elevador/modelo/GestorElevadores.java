//Hay que revisar como JP hace las importaciones a otras clases y los paquetes
package elevador.modelo;

import elevador.funcionalidades.Comando;
import elevador.modelo.Tipos.Direccion;
import java.util.*;

public class GestorElevadores {

    public List<Elevador> elevadores = new ArrayList<>();
    public List<Thread> hilos = new ArrayList<>();

    public GestorElevadores(int cantidadElevadores, int pisoMaximo, long tMovimientoMs, long tEsperaMs) {
        for (int i = 1; i <= cantidadElevadores; i++) {
            Elevador e = new Elevador(i, pisoMaximo, tMovimientoMs, tEsperaMs);
            elevadores.add(e);
            hilos.add(new Thread(e, "Elevador-" + i));
        }
    }

    public void iniciar() {
        for (Thread t : hilos) t.start();
        System.out.println("Gestor: " + elevadores.size() + " elevadores iniciados.");
    }

    public void detener() {
        apagarTodos(); // <- ahora existe
        System.out.println("Gestor: solicitud de detener enviada.");
    }

    public void irAPiso(int idElevador, int piso) {
        if (idElevador < 1 || idElevador > elevadores.size()) {
            System.out.println("Gestor: elevador inválido " + idElevador);
            return;
        }
        elevadores.get(idElevador - 1).enviar(Comando.irA(piso));
    }

    public void solicitarDesdePasillo(int piso, Direccion dir) {
        Elevador mejor = null;
        int mejorCosto = Integer.MAX_VALUE;

        for (Elevador e : elevadores) {
            // evitar "regresar" si ya tiene pendientes en sentido opuesto
            if (e.getDireccion() == Direccion.SUBE && piso < e.getPisoActual() && e.pendientes() > 0) continue;
            if (e.getDireccion() == Direccion.BAJA && piso > e.getPisoActual() && e.pendientes() > 0) continue;

            int distancia = Math.abs(e.getPisoActual() - piso);
            int penalizacion = e.pendientes();
            int costo = distancia + penalizacion;

            if (e.getDireccion() == Direccion.QUIETO) costo -= 1; // preferir quietos

            if (costo < mejorCosto) {
                mejorCosto = costo;
                mejor = e;
            }
        }

        // Fallback: si todos “rompen” la regla, asignar el más cercano
        if (mejor == null && !elevadores.isEmpty()) {
            mejor = elevadores.get(0);
            int dist = Math.abs(mejor.getPisoActual() - piso);
            for (Elevador e : elevadores) {
                int d = Math.abs(e.getPisoActual() - piso);
                if (d < dist) { dist = d; mejor = e; }
            }
        }

        if (mejor != null) {
            System.out.println("Gestor: asignado Elevador " + mejor.id + " para piso " + piso + " (" + dir + ")");
            mejor.enviar(Comando.recoger(piso, dir));
        }
    }

    // ===== NUEVOS NOMBRES CONSISTENTES =====

    public void resetTodos() {
        for (Elevador e : elevadores) {
            e.enviar(Comando.reset());
        }
        System.out.println("Gestor: RESET enviado a todos.");
    }

    public void apagarTodos() {
        for (Elevador e : elevadores) {
            e.enviar(Comando.apagar());
        }
        System.out.println("Gestor: APAGAR enviado a todos.");
    }

    // ===== Estado para la UI =====
    public List<String> estados() {
        List<String> s = new ArrayList<>();
        for (Elevador e : elevadores) {
            s.add("E" + e.id + " | piso=" + e.getPisoActual() + " | dir=" + e.getDireccion() + " | pendientes=" + e.pendientes());
        }
        return s;
    }
}
