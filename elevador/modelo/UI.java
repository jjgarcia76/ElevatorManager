package elevador.modelo;

import elevador.modelo.Tipos.Direccion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class UI extends JFrame {

    private final GestorElevadores gestor;
    private final int pisos;

    private final JTextArea areaEstado = new JTextArea(10, 30);

    public UI(GestorElevadores gestor, int pisos) {
        super("Elevator Manager");
        this.gestor = gestor;
        this.pisos = pisos;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel de controles
        JPanel controles = new JPanel(new GridLayout(0, 1, 6, 6));

        // Solicitar desde pasillo
        JPanel fila1 = new JPanel();
        JTextField txtPiso = new JTextField(5);
        JComboBox<Direccion> cbDir = new JComboBox<>(Direccion.values());
        JButton btnSolicitar = new JButton("Solicitar elevador");
        fila1.add(new JLabel("Piso:")); fila1.add(txtPiso);
        fila1.add(new JLabel("Dirección:")); fila1.add(cbDir);
        fila1.add(btnSolicitar);
        controles.add(fila1);

        // Ir a piso (interno)
        JPanel fila2 = new JPanel();
        JTextField txtId = new JTextField(3);
        JTextField txtDestino = new JTextField(5);
        JButton btnIr = new JButton("Ir a piso (interno)");
        fila2.add(new JLabel("Elevador:")); fila2.add(txtId);
        fila2.add(new JLabel("Destino:")); fila2.add(txtDestino);
        fila2.add(btnIr);
        controles.add(fila2);

        // Reset / Stop
        JPanel fila3 = new JPanel();
        JButton btnReset = new JButton("Reset");
        JButton btnStop  = new JButton("Stop");
        fila3.add(btnReset); fila3.add(btnStop);
        controles.add(fila3);

        add(controles, BorderLayout.NORTH);

        // Estado
        areaEstado.setEditable(false);
        add(new JScrollPane(areaEstado), BorderLayout.CENTER);

        // Listeners
        btnSolicitar.addActionListener((ActionEvent e) -> {
            try {
                int piso = Integer.parseInt(txtPiso.getText().trim());
                Direccion dir = (Direccion) cbDir.getSelectedItem();
                if (piso < 1 || piso > pisos) throw new IllegalArgumentException("Piso fuera de rango");
                gestor.solicitarDesdePasillo(piso, dir);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        btnIr.addActionListener((ActionEvent e) -> {
            try {
                int id = Integer.parseInt(txtId.getText().trim());
                int dest = Integer.parseInt(txtDestino.getText().trim());
                if (dest < 1 || dest > pisos) throw new IllegalArgumentException("Piso fuera de rango");
                gestor.irAPiso(id, dest);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        btnReset.addActionListener(e -> gestor.resetTodos());
        btnStop.addActionListener(e -> gestor.detener());

        // Timer de refresco
        new Timer(200, e -> refrescarEstado()).start();

        setSize(600, 400);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void refrescarEstado() {
        StringBuilder sb = new StringBuilder();
        for (String s : gestor.estados()) sb.append(s).append('\n');
        areaEstado.setText(sb.toString());
    }
}

