//Hay que revisar como JP hace las importaciones a otras clases y los paquetes
package elevador.modelo;

import elevador.funcionalidades.Comando;
import elevador.modelo.Tipos.Direccion;
import elevador.modelo.Tipos.TipoComando;

import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.*;

public class Elevador implements Runnable {
    //configuracion
    public int id;
    public int pisoMinimo = 1;
    public int pisoMaximo;
    public long tiempoMovimientoMs;  
    public long tiempoEsperaMs;     

    public int pisoActual = 1;
    public Direccion direccion = Direccion.SUBE;

    public BlockingQueue<Comando> bandeja = new LinkedBlockingQueue<>();
    public TreeSet<Integer> paradasArriba = new TreeSet<>();
    public TreeSet<Integer> paradasAbajo  = new TreeSet<>();

    public boolean ejecutando = true;

    //un registros (logs) por cada accion del elevador
    private Logger logger;

    public Elevador(int id, int pisoMaximo, long tiempoMovimientoMs, long tiempoEsperaMs) {
        this.id = id;
        this.pisoMaximo = pisoMaximo;
        this.tiempoMovimientoMs = tiempoMovimientoMs;
        this.tiempoEsperaMs = tiempoEsperaMs;
        configurarLogger();
    }

    private void configurarLogger() {
        try {
            logger = Logger.getLogger("Elevador" + id);
            logger.setUseParentHandlers(true);
            logger.setLevel(Level.INFO);

            FileHandler file = new FileHandler("elevador" + id + ".log", false);
            file.setFormatter(new SimpleFormatter());
            logger.addHandler(file);
        } catch (Exception e) {
            System.out.println("Elevador " + id + " no pudo crear el log: ");
        }
    }

    //API para enviar ordenes al elevador
    public void enviar(Comando cmd) {
        if (cmd == null) return;
        if (cmd.piso != -1) {
            if (cmd.piso < pisoMinimo || cmd.piso > pisoMaximo) {
                logger.info("Este piso no es valido" + cmd.piso);
                return;
            }
        }
        bandeja.offer(cmd);
    }

    @Override
    public void run() {
        logger.info("Iniciado en piso " + pisoActual + " (dir=SUBE)");
        while (ejecutando) {
            // 1) Drenar bandeja
            drenarBandeja();

            // 2) Si no hay paradas, idle breve
            if (paradasArriba.isEmpty() && paradasAbajo.isEmpty()) {
                direccion = Direccion.QUIETO;
                dormir(40);
                continue;
            }

            // 3) Elegir sentido (SCAN)
            if (direccion == Direccion.QUIETO) {
                if (!paradasArriba.isEmpty() && !paradasAbajo.isEmpty()) {
                    // elige sentido más cercano
                    int distUp   = Math.abs((paradasArriba.first()) - pisoActual);
                    int distDown = Math.abs((paradasAbajo.last())  - pisoActual);
                    direccion = (distUp <= distDown) ? Direccion.SUBE : Direccion.BAJA;
                } else if (!paradasArriba.isEmpty()) {
                    direccion = Direccion.SUBE;
                } else {
                    direccion = Direccion.BAJA;
                }
            }

            // 4) Mover según sentido
            if (direccion == Direccion.SUBE) {
                if (paradasArriba.isEmpty()) {
                    if (!paradasAbajo.isEmpty()) {
                        logger.info("Cambio de dirección: SUBE -> BAJA");
                        direccion = Direccion.BAJA;
                    } else {
                        direccion = Direccion.QUIETO;
                    }
                    continue;
                }
                if (pisoActual < pisoMaximo) {
                    dormir(tiempoMovimientoMs);
                    pisoActual++;
                    logger.info("Subiendo hacia piso " + pisoActual);
                }
                if (paradasArriba.contains(pisoActual)) {
                    paradasArriba.remove(pisoActual);
                    llegoYPara();
                }
            } else if (direccion == Direccion.BAJA) {
                if (paradasAbajo.isEmpty()) {
                    if (!paradasArriba.isEmpty()) {
                        logger.info("Cambio de dirección: BAJA -> SUBE");
                        direccion = Direccion.SUBE;
                    } else {
                        direccion = Direccion.QUIETO;
                    }
                    continue;
                }
                if (pisoActual > pisoMinimo) {
                    dormir(tiempoMovimientoMs);
                    pisoActual--;
                    logger.info("Bajando hacia piso " + pisoActual);
                }
                if (paradasAbajo.contains(pisoActual)) {
                    paradasAbajo.remove(pisoActual);
                    llegoYPara();
                }
            }
        }
        logger.info("Esta detenido");
    }

    private void llegoYPara() {
        logger.info("Llego al piso " + pisoActual + " (abrir/cerrar puertas)");
        dormir(tiempoEsperaMs);
        drenarBandeja();
    }

    private void drenarBandeja() {
        Comando cmd;
        while ((cmd = bandeja.poll()) != null) {
            if (cmd.tipo == TipoComando.APAGAR) {
                ejecutando = false;
                return;
            }
            if (cmd.tipo == TipoComando.RESET) {
                logger.info("RESET solicitado");
                paradasArriba.clear();
                paradasAbajo.clear();
                //regresa al piso 1 si no esta en el piso 1
                if (pisoActual > 1) {
                    paradasAbajo.add(1);
                    direccion = Direccion.BAJA;
                } else {
                    direccion = Direccion.SUBE;
                }
                continue;
            }
            if (cmd.tipo == TipoComando.RECOGER) {
                agregarParada(cmd.piso);
                logger.info("Pickup en piso " + cmd.piso + " (" + cmd.direccionDeseada + ")");
                continue;
            }
            if (cmd.tipo == TipoComando.IR_A) {
                //boton interno
                agregarParada(cmd.piso);
                logger.info("Destino interno agregado: piso " + cmd.piso);
            }
        }
    }

    private void agregarParada(int piso) {
        if (piso == pisoActual) {
            logger.info("Atendiendo piso actual " + pisoActual);
            dormir(tiempoEsperaMs);
            return;
        }
        if (piso > pisoActual) paradasArriba.add(piso); else paradasAbajo.add(piso);
    }

    private void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            logger.info("Interrupcion en: ");
        }
    }

    //API para la clase UI.java
    public int getPisoActual() { 
        return pisoActual; 
    }
    public Direccion getDireccion() { 
        return direccion; 
    }
    public int pendientes() { 
        return paradasArriba.size() + paradasAbajo.size(); 
    }
}
