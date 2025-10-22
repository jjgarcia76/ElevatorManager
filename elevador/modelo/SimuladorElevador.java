//Este es de prueba despues se puede quitar
package elevador.modelo;

public class SimuladorElevador{
    public static void main(String[] args) {
        Elevador elevador1 = new Elevador(1, 10, 1000, 2000);
        Thread hilo = new Thread(elevador1);
        hilo.start();

        elevador1.enviar(new Comando(Tipos.TipoComando.IR_A, 5));
        elevador1.enviar(new Comando(Tipos.TipoComando.IR_A, 2));
        elevador1.enviar(new Comando(Tipos.TipoComando.IR_A, 8));

        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        elevador1.ejecutando = false;
        System.out.println("Simulación finalizada.");
    }
}
