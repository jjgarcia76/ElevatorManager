package elevador.modelo;

import javax.swing.*;
import java.awt.*;

public class Principal {
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                int n = Integer.parseInt(JOptionPane.showInputDialog("Cantidad de elevadores:", "3"));
                int pisos = Integer.parseInt(JOptionPane.showInputDialog("Cantidad de pisos:", "12"));
                long tMov = Long.parseLong(JOptionPane.showInputDialog("Tiempo entre pisos (ms):", "500"));
                long tStop = Long.parseLong(JOptionPane.showInputDialog("Tiempo de espera en piso (ms):", "900"));

                GestorElevadores gestor = new GestorElevadores(n, pisos, tMov, tStop);
                gestor.iniciar();
                new UI(gestor, pisos); // << conectar UI con el modelo
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Entrada inválida: " + e.getMessage());
            }
        });
    }
}

