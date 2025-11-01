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

    //Estados y configuracion de los int 
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

    //agregar el pausado para el UI o ver si se puede meter en la clas UI
    //esto se ve en el UI para el pausado
    public boolean pausado = false;

    //aqui se ven los logs de los elevadores
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

            // para que también salga en consola (principiante quiere ver prints)
            logger.setUseParentHandlers(true);

            logger.setLevel(Level.INFO);

            // true = adjuntar al final, así no borra lo viejo
            FileHandler file = new FileHandler("elevador" + id + ".log", true);
            file.setFormatter(new SimpleFormatter());
            logger.addHandler(file);

        } catch (Exception e) {
            System.out.println("Elevador " + id + " no pudo crear el log");
        }
    }

    //recibe los "comandos"
    public void enviar(Comando cmd) {
        if (cmd == null) {
            return;
        }

        // validación del piso que viene en el comando
        if (cmd.piso != -1) {
            boolean pisoInvalido = (cmd.piso < pisoMinimo || cmd.piso > pisoMaximo);
            if (pisoInvalido) {
                logger.info("[Elevador " + id + "] Este numero de piso no existe: " + cmd.piso);
                System.out.println("[Elevador " + id + "] Piso invalido rechazado: " + cmd.piso);
                return;
            }
        }

        // meter el comando en la cola
        bandeja.offer(cmd);
    }

    //Comienza el hilooooooo
    @Override
    public void run() {

        logger.info("[Elevador " + id + "] Iniciado en el piso " + pisoActual + " (direccion - SUBE)");
        System.out.println("[Elevador " + id + "] HILO INICIADO en piso " + pisoActual);

        // ciclo principal del elevador
        while (ejecutando) {

            //PAUSA
            synchronized (this) {
                while (pausado && ejecutando) {
                    try {
                        logger.info("[Elevador " + id + "] PAUSA");
                        System.out.println("[Elevador " + id + "] PAUSANDO");
                        wait();
                        logger.info("[Elevador " + id + "] PLAY");
                        System.out.println("[Elevador " + id + "] PLAY");
                    } catch (InterruptedException ex) {
                        logger.info("[Elevador " + id + "] hubo una interrupcion");
                        System.out.println("[Elevador " + id + "] Error en pausa/reanudar");
                    }
                }
            }

            //revisa los comandos pendientes
            drenarBandeja();

            //se queda quieto si no hay mas acciones o comandos puestos
            boolean noHayParadasArriba = paradasArriba.isEmpty();
            boolean noHayParadasAbajo = paradasAbajo.isEmpty();

            if (noHayParadasArriba && noHayParadasAbajo) {
                direccion = Direccion.QUIETO;
                dormir(40); //sleep
                continue;
            }

            //QUIETO    
            if (direccion == Direccion.QUIETO) {

                boolean hayArriba = !paradasArriba.isEmpty();
                boolean hayAbajo  = !paradasAbajo.isEmpty();

                if (hayArriba && hayAbajo) {
                    //escoge la direccion mas cercana que tenga
                    int primerArriba = paradasArriba.first();
                    int ultimoAbajo  = paradasAbajo.last();

                    int distUp   = Math.abs(primerArriba - pisoActual);
                    int distDown = Math.abs(ultimoAbajo  - pisoActual);

                    if (distUp <= distDown) {
                        direccion = Direccion.SUBE;
                    } else {
                        direccion = Direccion.BAJA;
                    }

                } else if (hayArriba) {
                    direccion = Direccion.SUBE;
                } else {
                    direccion = Direccion.BAJA;
                }
            }

            // movimiento cuando la direccion es SUBE
            if (direccion == Direccion.SUBE) {

                //intenta hacer un cambio de direccion si no hay mas paradas hacia abajo
                if (paradasArriba.isEmpty()) {

                    if (!paradasAbajo.isEmpty()) {
                        logger.info("[Elevador " + id + "] Cambio de direccion: SUBE -> BAJA");
                        System.out.println("[Elevador " + id + "] Cambio de direccion: SUBE -> BAJA");
                        direccion = Direccion.BAJA;
                    } else {
                        direccion = Direccion.QUIETO;
                    }

                    continue;
                }

                //subir piso
                if (pisoActual < pisoMaximo) {
                    dormir(tiempoMovimientoMs);
                    pisoActual = pisoActual + 1;
                    logger.info("[Elevador " + id + "] Subiendo al piso " + pisoActual);
                    System.out.println("[Elevador " + id + "] Subiendo... ahora en piso " + pisoActual);
                }

                //reviza si es una parada 
                if (paradasArriba.contains(pisoActual)) {
                    paradasArriba.remove(pisoActual);
                    llegoYPara();
                }

            } else if (direccion == Direccion.BAJA) {

                //intenta hacer un cambio de direccion si no hay mas paradas hacia abajo
                if (paradasAbajo.isEmpty()) {

                    if (!paradasArriba.isEmpty()) {
                        logger.info("[Elevador " + id + "] Cambio de direccion: BAJA -> -> SUBE");
                        System.out.println("[Elevador " + id + "] Cambio de direccion: BAJA -> -> SUBE");
                        direccion = Direccion.SUBE;
                    } else {
                        direccion = Direccion.QUIETO;
                    }

                    continue;
                }

                //para bajar el elevador, a que piso baja
                if (pisoActual > pisoMinimo) {
                    dormir(tiempoMovimientoMs);
                    pisoActual = pisoActual - 1;
                    logger.info("[Elevador " + id + "] Bajando al piso " + pisoActual);
                    System.out.println("[Elevador " + id + "] Bajando... ahora en piso " + pisoActual);
                }

                //vuelve a revisar si esta en la parada
                if (paradasAbajo.contains(pisoActual)) {
                    paradasAbajo.remove(pisoActual);
                    llegoYPara();
                }
            }
        }

        logger.info("[Elevador " + id + "] Detenido (APAGAR recibido o STOP)");
        System.out.println("[Elevador " + id + "] HILO TERMINADO");
    }

    //paradas de piso
    private void llegoYPara() {
        logger.info("[Elevador " + id + "] Llegó al piso " + pisoActual + " (abrir/cerrar puertas)");
        System.out.println("[Elevador " + id + "] Puertas abriendo/cerrando en piso " + pisoActual);

        dormir(tiempoEsperaMs);

        //revisa si hay nuevos comandos mientras estaba con puertas abiertas, basicamente si le llegaron nuevos comandos
        drenarBandeja(); 
    }

    //comandos de APAGAR y RESET del elevador
    private void drenarBandeja() {

        Comando cmd;

        // sacar comandos de la cola uno por uno
        while ((cmd = bandeja.poll()) != null) {

            if (cmd.tipo == TipoComando.APAGAR) {
                logger.info("[Elevador " + id + "] Se solicito APAGAFO");
                System.out.println("[Elevador " + id + "] APAGAR recibido, deteniendo hilo...");
                ejecutando = false;
                return;
            }

            if (cmd.tipo == TipoComando.RESET) {
                logger.info("[Elevador " + id + "] Se solicito un RESET");
                System.out.println("[Elevador " + id + "] RESET recibido");

                paradasArriba.clear();
                paradasAbajo.clear();

                //se vuelve al piso 1 si no estoy ya en 1
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
                logger.info("[Elevador " + id + "] Pickup en piso " + cmd.piso + " (dir solicitada " + cmd.direccionDeseada + ")");
                System.out.println("[Elevador " + id + "] RECOGER en piso " + cmd.piso + " (" + cmd.direccionDeseada + ")");
                continue;
            }

            if (cmd.tipo == TipoComando.IR_A) {
                // Botón interno
                agregarParada(cmd.piso);
                logger.info("[Elevador " + id + "] Destino agregado: piso " + cmd.piso);
                System.out.println("[Elevador " + id + "] IR_A " + cmd.piso);
            }
        }
    }

    //nueva parada y atiende la parada, por ejemplo lleva al piso 1, esta atendiendo a ese piso
    private void agregarParada(int piso) {

        if (piso == pisoActual) {
            logger.info("[Elevador " + id + "] Atendiendo en el piso " + pisoActual + " (abrir/cerrar puertas)");
            System.out.println("[Elevador " + id + "] Ya estoy en piso " + pisoActual + ", abriendo puertas directamente");
            dormir(tiempoEsperaMs);
            return;
        }

        if (piso > pisoActual) {
            paradasArriba.add(piso);
        } else {
            paradasAbajo.add(piso);
        }
    }

    //sleep()
    private void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
                logger.info("[Elevador " + id + "] algo a interrumpido el sleep()");
                System.out.println("[Elevador " + id + "] Error en sleep()");
        }
    }

    //Esto sirve para la clase UI
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



//CODIGO ANTIGUO PARA REVISIONES
/*package elevador.modelo;

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
*/