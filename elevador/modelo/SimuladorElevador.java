//Este es de prueba despues se puede quitar
//Esta clase no forma parte del programa final, solo se creo para pruebas de inicio
package elevador.modelo;

import elevador.funcionalidades.Comando;

public class SimuladorElevador {
    public static void main(String[] args) {
        Elevador elevador1 = new Elevador(1, 10, 1000, 2000);
        Thread hilo = new Thread(elevador1);
        hilo.start();

        elevador1.enviar(Comando.irA(5));
        elevador1.enviar(Comando.irA(2));
        elevador1.enviar(Comando.irA(8));

        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        elevador1.enviar(Comando.apagar());
        System.out.println("Fin de la prueba");
    }
}

