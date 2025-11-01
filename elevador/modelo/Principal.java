package elevador.modelo;

import javax.swing.*;
import java.awt.*;

public class Principal {
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                //se agregaron datos por defecto para que el usuario sepa que tiene que colocar, es mas que todo como ejemplo de uso
                int n = Integer.parseInt(JOptionPane.showInputDialog("Cantidad de elevadores:", "3"));
                int pisos = Integer.parseInt(JOptionPane.showInputDialog("Cantidad de pisos:", "12"));
                long tMov = Long.parseLong(JOptionPane.showInputDialog("Tiempo entre pisos (ms):", "500"));
                long tStop = Long.parseLong(JOptionPane.showInputDialog("Tiempo de espera en piso (ms):", "900"));

                GestorElevadores gestor = new GestorElevadores(n, pisos, tMov, tStop);
                gestor.iniciar();
                //modelos de la clase UI
                new UI(gestor, pisos);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Esta opcion no es valida usa uno que si lo sea >:(  " + e.getMessage());
            }
        });
    }
}

