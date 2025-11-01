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
            Elevador elevador = new Elevador(i, pisoMaximo, tMovimientoMs, tEsperaMs);
            elevadores.add(elevador);
            Thread hilo = new Thread(elevador, "Elevador-" + i);
            hilos.add(hilo);
        }
    }

    //ciclo
    public void iniciar() {
        for (Thread t : hilos) {
            t.start();
        }
        System.out.println(elevadores.size() + " elevadores iniciados");
    }

    public void detener() {
        apagarTodos();
        System.out.println("solicitud de detener enviada");
    }

    //comandos del elevador

    //supongamos de que hay un boton adentro del elevador
    public void irAPiso(int idElevador, int piso) {
        if (idElevador < 1 || idElevador > elevadores.size()) {
            System.out.println("Elevador no valido " + idElevador);
            return;
        }

        Elevador elegido = elevadores.get(idElevador - 1);
        elegido.enviar(Comando.irA(piso));
        System.out.println("Enviado IR_A " + piso + " al Elevador " + idElevador);
    }

    //llamada de piso + elevador 
    public void solicitarDesdePasillo(int piso, Direccion dir) {
        Elevador mejor = null;
        int mejorCosto = Integer.MAX_VALUE;

        for (Elevador e : elevadores) {
            //si ya tiene una ruta activa evita devolver elevadores
            if (e.getDireccion() == Direccion.SUBE && piso < e.getPisoActual() && e.pendientes() > 0) {
                continue;
            }
            if (e.getDireccion() == Direccion.BAJA && piso > e.getPisoActual() && e.pendientes() > 0) {
                continue;
            }
            int distancia = Math.abs(e.getPisoActual() - piso);
            int penalizacion = e.pendientes();
            int costo = distancia + penalizacion;
            //hay una preferencia por los que estan en QUIETO
            if (e.getDireccion() == Direccion.QUIETO) {
                costo = costo - 1;
            }

            if (costo < mejorCosto) {
                mejorCosto = costo;
                mejor = e;
            }
        }

        //busca al mas cercano
        if (mejor == null && !elevadores.isEmpty()) {
            mejor = elevadores.get(0);
            int distanciaMenor = Math.abs(mejor.getPisoActual() - piso);

            for (Elevador e : elevadores) {
                int d = Math.abs(e.getPisoActual() - piso);
                if (d < distanciaMenor) {
                    distanciaMenor = d;
                    mejor = e;
                }
            }
        }

        if (mejor != null) {
            System.out.println("Elevador " + mejor.id + " para piso " + piso + " (" + dir + ")");
            mejor.enviar(Comando.recoger(piso, dir));
        } else {
            System.out.println("No se encontro un elevador disponible para el piso " + piso);
        }
    }

    //acciones
    public void resetTodos() {
        for (Elevador e : elevadores) {
            e.enviar(Comando.reset());
        }
        System.out.println("RESET enviado a todos los elevadores");
    }

    public void apagarTodos() {
        for (Elevador e : elevadores) {
            e.enviar(Comando.apagar());
        }
        System.out.println("APAGAR enviado a todos los elevadores");
    }

    //Protocolo textual, IR_A <piso>, RECOGER <piso> <SUBE|BAJA>, RESET, APAGAR
    public void enviarTexto(int idElevador, String mensaje) {
        if (idElevador < 1 || idElevador > elevadores.size()) {
            System.out.println("elevador no valido " + idElevador);
            return;
        }

        Elevador e = elevadores.get(idElevador - 1);

        if (mensaje == null || mensaje.trim().isEmpty()) {
            System.out.println("comando vacio ");
            return;
        }

        String[] partes = mensaje.trim().split("\\s+");
        String tipo = partes[0].toUpperCase();

        try {
            if (tipo.equals("IR_A")) {
                if (partes.length < 2) {
                    System.out.println("Este es el formato correcto: IR_A <piso>");
                    return;
                }

                int piso = Integer.parseInt(partes[1]);
                e.enviar(Comando.irA(piso));
                System.out.println("[PROTOCOLO] IR_A " + piso + " -> E" + e.id);
            }

            else if (tipo.equals("RECOGER")) {
                if (partes.length < 3) {
                    System.out.println("Este es el formato correcto: RECOGER <piso> <SUBE|BAJA>");
                    return;
                }

                int piso = Integer.parseInt(partes[1]);
                Direccion dir = Direccion.valueOf(partes[2].toUpperCase());
                e.enviar(Comando.recoger(piso, dir));
                System.out.println("[PROTOCOLO] RECOGER " + piso + " " + dir + " -> E" + e.id);
            }

            else if (tipo.equals("RESET")) {
                e.enviar(Comando.reset());
                System.out.println("[PROTOCOLO] RESET -> E" + e.id);
            }

            else if (tipo.equals("APAGAR")) {
                e.enviar(Comando.apagar());
                System.out.println("[PROTOCOLO] APAGAR -> E" + e.id);
            }

            else {
                System.out.println("Comando desconocido: " + mensaje);
            }

        } catch (Exception ex) {
            System.out.println("Error interpretando comando '" + mensaje + "'");
        }
    }

    //Estado de la consola de la interfaz
    public List<String> estados() {
        List<String> lista = new ArrayList<>();

        for (Elevador e : elevadores) {
            String estado = "E" + e.id + " | piso - " + e.getPisoActual() + " | estado - " + e.getDireccion() + " | pendientes - " + e.pendientes();
            lista.add(estado);
        }

        return lista;
    }
}


// CODIGO ANTIGUO PARA REVISIONES

/*package elevador.modelo;

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
*/