package elevador.modelo;

import elevador.modelo.Tipos.Direccion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class UI extends JFrame {

    private final GestorElevadores gestor;
    private final int pisos;

    private final JTextArea areaEstado = new JTextArea(5, 30);
    private final JPanel panelGrafico;

    //fuentes de letras
    private final Font fontLabel  = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font fontBtn    = new Font("Segoe UI", Font.BOLD, 12);
    private final Font fontField  = new Font("Segoe UI", Font.PLAIN, 13);

    public UI(GestorElevadores gestor, int pisos) {
        super("Elevator Manager");
        this.gestor = gestor;
        this.pisos = pisos;

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignore) {}
        SwingUtilities.updateComponentTreeUI(this);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(new Color(245, 245, 245));

        JPanel cardControles = new JPanel();
        cardControles.setLayout(new GridLayout(0, 1, 6, 6));
        cardControles.setBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(200, 200, 200), 1, true),
                        new EmptyBorder(10, 10, 10, 10)
                )
        );
        cardControles.setBackground(new Color(250, 250, 250));

        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        fila1.setOpaque(false);
        JTextField txtPiso = buildTextField(5);
        JComboBox<Direccion> cbDir = new JComboBox<>(Direccion.values());
        cbDir.setFont(fontField);
        JButton btnSolicitar = buildButton("Solicitar elevador");

        fila1.add(buildLabel("Piso:"));
        fila1.add(txtPiso);
        fila1.add(buildLabel("Direccion:"));
        fila1.add(cbDir);
        fila1.add(btnSolicitar);

        cardControles.add(fila1);
        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        fila2.setOpaque(false);
        JTextField txtId = buildTextField(3);
        JTextField txtDestino = buildTextField(5);
        JButton btnIr = buildButton("Ir a piso No.");

        fila2.add(buildLabel("Elevador:"));
        fila2.add(txtId);
        fila2.add(buildLabel("Destino:"));
        fila2.add(txtDestino);
        fila2.add(btnIr);

        cardControles.add(fila2);

        JPanel filaCmd = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filaCmd.setOpaque(false);
        JTextField txtIdCmd = buildTextField(3);
        JTextField txtComando = buildTextField(15);
        JButton btnEnviarCmd = buildButton("Enviar comando de texto");

        filaCmd.add(buildLabel("Elevador:"));
        filaCmd.add(txtIdCmd);
        filaCmd.add(buildLabel("Comando:"));
        filaCmd.add(txtComando);
        filaCmd.add(btnEnviarCmd);

        JLabel hint = new JLabel("Ej: IR_A 7  |  RECOGER 4 SUBE  |  RESET  |  APAGAR");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(new Color(90, 90, 90));
        filaCmd.add(hint);

        cardControles.add(filaCmd);

        JPanel fila3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        fila3.setOpaque(false);
        JButton btnReset = buildButton("Reset");
        JButton btnStop = buildButton("Stop");
        JButton btnPausar = buildButton("Pausar");
        JButton btnReanudar = buildButton("Reanudar");

        fila3.add(btnReset);
        fila3.add(btnStop);
        fila3.add(btnPausar);
        fila3.add(btnReanudar);

        cardControles.add(fila3);

        add(cardControles, BorderLayout.NORTH);

        areaEstado.setEditable(false);
        areaEstado.setFont(new Font("Consolas", Font.PLAIN, 12));
        areaEstado.setBackground(Color.WHITE);

        JScrollPane scrollEstado = new JScrollPane(areaEstado);
        scrollEstado.setBorder(BorderFactory.createTitledBorder(new LineBorder(new Color(180, 180, 180), 1, true), "Estado de elevadores", 0, 0, new Font("Segoe UI", Font.BOLD, 12), Color.DARK_GRAY));

        scrollEstado.setPreferredSize(new Dimension(600, 140));

        JPanel panelEstadoWrapper = new JPanel(new BorderLayout());
        panelEstadoWrapper.setBackground(new Color(245, 245, 245));
        panelEstadoWrapper.add(scrollEstado, BorderLayout.CENTER);

        add(panelEstadoWrapper, BorderLayout.CENTER);
        panelGrafico = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(245, 245, 245));
                g.fillRect(0, 0, getWidth(), getHeight());

                int n = gestor.elevadores.size();
                if (n == 0) return;
                int leftMargin = 30;
                int topMargin = 30;
                int colWidth = 200; 
                int spacingX = 40;   
                int cabinaWidth = 50;
                int maxDrawHeight = 600;
                int cabinaHeight;

                if (pisos > 0) {
                    cabinaHeight = maxDrawHeight / pisos;
                } else {
                    cabinaHeight = 20;
                }

                for (int i = 0; i < n; i++) {
                    Elevador e = gestor.elevadores.get(i);
                    int baseX = leftMargin + i * (colWidth + spacingX);

                    g.setColor(new Color(30, 30, 30));
                    g.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    g.drawString("Elevador " + e.id, baseX, topMargin - 10);

                    g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    for (int p = pisos; p >= 1; p--) {
                        int idxFromTop = (pisos - p);
                        int yBase = topMargin + idxFromTop * cabinaHeight;

                        if (p % 2 == 0) {
                            g.setColor(new Color(230, 230, 230));
                        } else {
                            g.setColor(new Color(240, 240, 240));
                        }
                        g.fillRect(baseX, yBase, cabinaWidth, cabinaHeight);

                        g.setColor(new Color(180, 180, 180));
                        g.drawRect(baseX, yBase, cabinaWidth, cabinaHeight);

                        g.setColor(new Color(60, 60, 60));
                        g.drawString("P" + p, baseX + cabinaWidth + 8, yBase + cabinaHeight / 2 + 4);
                    }

                    int pisoActual = e.getPisoActual();
                    int idxFromTop = (pisos - pisoActual);
                    int yElev = topMargin + idxFromTop * cabinaHeight;

                    g.setColor(new Color(0, 90, 200));
                    g.fillRoundRect(baseX, yElev, cabinaWidth, cabinaHeight, 6, 6);

                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    g.drawString("E" + e.id, baseX + 12, yElev + cabinaHeight / 2 + 4);
                }

                int nElev = gestor.elevadores.size();
                int anchoPreferido = leftMargin + nElev * (colWidth + spacingX) + 50;
                int altoPreferido  = 700;

                Dimension pref = new Dimension(anchoPreferido, altoPreferido);
                if (!pref.equals(getPreferredSize())) {
                    setPreferredSize(pref);
                    revalidate();
                }
            }
        };

        panelGrafico.setBorder(
                BorderFactory.createCompoundBorder(new LineBorder(new Color(200, 200, 200), 1, true),new EmptyBorder(10, 10, 10, 10)
                )
        );

        JScrollPane scrollSimulacion = new JScrollPane(
                panelGrafico,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );

        scrollSimulacion.setPreferredSize(new Dimension(800, 320));

        scrollSimulacion.getViewport().setBackground(new Color(245, 245, 245));
        scrollSimulacion.setBorder(
                BorderFactory.createTitledBorder(new LineBorder(new Color(200, 200, 200), 1, true), "Elevadores", 0, 0, new Font("Segoe UI", Font.BOLD, 12), new Color(70, 70, 70)
                )
        );

        add(scrollSimulacion, BorderLayout.SOUTH);

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

        btnEnviarCmd.addActionListener((ActionEvent e) -> {
            try {
                int idCmd = Integer.parseInt(txtIdCmd.getText().trim());
                String cmd = txtComando.getText().trim();
                if (cmd.isEmpty()) throw new IllegalArgumentException("Comando null");
                gestor.enviarTexto(idCmd, cmd);
                txtComando.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error en comando: " + ex.getMessage());
            }
        });

        btnReset.addActionListener(e -> gestor.resetTodos());
        btnStop.addActionListener(e -> gestor.detener());

        btnPausar.addActionListener(e -> {
            for (Elevador elev : gestor.elevadores) {
                elev.pausado = true;
            }
        });

        btnReanudar.addActionListener(e -> {
            for (Elevador elev : gestor.elevadores) {
                elev.pausado = false;
                synchronized (elev) {
                    elev.notify();
                }
            }
        });

        new Timer(200, e -> refrescarEstado()).start();
        new Timer(200, e -> panelGrafico.repaint()).start();

        setSize(900, 700);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JTextField buildTextField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setFont(fontField);
        return tf;
    }

    private JButton buildButton(String text) {
        JButton b = new JButton(text);
        b.setFont(fontBtn);
        return b;
    }

    private JLabel buildLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(fontLabel);
        return l;
    }

    private void refrescarEstado() {
        StringBuilder sb = new StringBuilder();
        for (String s : gestor.estados()) {
            sb.append(s).append('\n');
        }
        areaEstado.setText(sb.toString());
    }
}

//VERSION DE UI SIN DISEÑO BONITO
/*
package elevador.modelo;

import elevador.modelo.Tipos.Direccion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class UI extends JFrame {

    private final GestorElevadores gestor;
    private final int pisos;

    private final JTextArea areaEstado = new JTextArea(10, 30);

    //parte grafica de abajo
    private final JPanel panelGrafico;

    public UI(GestorElevadores gestor, int pisos) {
        super("Elevator Manager");
        this.gestor = gestor;
        this.pisos = pisos;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        //Panel de controles digamos que hay un panel jsjs
        JPanel controles = new JPanel();
        controles.setLayout(new GridLayout(0, 1, 8, 8));

        //solicitar elevador en piso 1
        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JTextField txtPiso = new JTextField(5);
        JComboBox<Direccion> cbDir = new JComboBox<>(Direccion.values());
        JButton btnSolicitar = new JButton("Solicitar elevador");

        fila1.add(new JLabel("Piso:"));
        fila1.add(txtPiso);
        fila1.add(new JLabel("Direccion:"));
        fila1.add(cbDir);
        fila1.add(btnSolicitar);

        controles.add(fila1);

        // ----- Fila 2: IR A (comando interno dentro del elevador)
        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JTextField txtId = new JTextField(3);
        JTextField txtDestino = new JTextField(5);
        JButton btnIr = new JButton("Ir a piso No.");

        fila2.add(new JLabel("Elevador:"));
        fila2.add(txtId);
        fila2.add(new JLabel("Destino:"));
        fila2.add(txtDestino);
        fila2.add(btnIr);

        controles.add(fila2);

        // ----- Fila 3: Protocolo de comandos de texto
        JPanel filaCmd = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JTextField txtIdCmd = new JTextField(3);
        JTextField txtComando = new JTextField(15);
        JButton btnEnviarCmd = new JButton("Enviar comando textual");

        filaCmd.add(new JLabel("Elevador:"));
        filaCmd.add(txtIdCmd);
        filaCmd.add(new JLabel("Comando:"));
        filaCmd.add(txtComando);
        filaCmd.add(btnEnviarCmd);

        // ejemplo de comandos: IR_A 7 | RECOGER 4 | SUBE | RESET | APAGAR 
        controles.add(filaCmd);

        // ----- Fila 4: Reset / Stop / Pausar / Reanudar
        JPanel fila3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton btnReset    = new JButton("Reset");
        JButton btnStop     = new JButton("Stop");
        JButton btnPausar   = new JButton("Pausar");
        JButton btnReanudar = new JButton("Reanudar");

        fila3.add(btnReset);
        fila3.add(btnStop);
        fila3.add(btnPausar);
        fila3.add(btnReanudar);

        controles.add(fila3);

        add(controles, BorderLayout.NORTH);

        // =========================
        // CENTRO: ESTADO (texto)
        // =========================
        areaEstado.setEditable(false);
        JScrollPane scrollEstado = new JScrollPane(areaEstado);
        scrollEstado.setBorder(BorderFactory.createTitledBorder("Estado de elevadores"));
        add(scrollEstado, BorderLayout.CENTER);

        // =========================
        // SUR: PANEL GRAFICO
        // =========================
        panelGrafico = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                int w = getWidth();
                int h = getHeight();

                int n = gestor.elevadores.size();
                if (n == 0) return;

                int colWidth = w / n;
                int pisoHeight = (pisos == 0) ? h : (h / pisos);

                for (int i = 0; i < n; i++) {
                    Elevador e = gestor.elevadores.get(i);

                    int xCol = i * colWidth + 20;
                    g.setColor(Color.BLACK);
                    g.drawString("Elevador " + e.id, xCol, 15);

                    // Dibujar pisos
                    for (int p = 1; p <= pisos; p++) {
                        int yBase = h - (p * pisoHeight);
                        g.setColor(Color.GRAY);
                        g.drawRect(xCol, yBase, 40, pisoHeight);

                        g.setColor(Color.DARK_GRAY);
                        g.drawString("P" + p, xCol + 45, yBase + pisoHeight / 2);
                    }

                    // Cabina del elevador
                    int pisoActual = e.getPisoActual();
                    int yElev = h - (pisoActual * pisoHeight);

                    g.setColor(Color.BLUE);
                    g.fillRect(xCol, yElev, 40, pisoHeight);

                    g.setColor(Color.WHITE);
                    g.drawString("E" + e.id, xCol + 10, yElev + pisoHeight / 2);
                }
            }
        };
        panelGrafico.setPreferredSize(new Dimension(600, 300));
        panelGrafico.setBorder(BorderFactory.createTitledBorder(""));
        add(panelGrafico, BorderLayout.SOUTH);

        // =========================
        // LISTENERS
        // =========================

        // Botón: Solicitar elevador (pasillo)
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

        // Botón: Ir a piso (interno)
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

        // Botón: enviar comando textual
        btnEnviarCmd.addActionListener((ActionEvent e) -> {
            try {
                int idCmd = Integer.parseInt(txtIdCmd.getText().trim());
                String cmd = txtComando.getText().trim();
                if (cmd.isEmpty()) throw new IllegalArgumentException("Comando vacío");

                gestor.enviarTexto(idCmd, cmd);
                txtComando.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error en comando: " + ex.getMessage());
            }
        });

        // Reset y Stop globales
        btnReset.addActionListener(e -> gestor.resetTodos());
        btnStop.addActionListener(e -> gestor.detener());

        // Pausar todos los elevadores
        btnPausar.addActionListener(e -> {
            for (Elevador elev : gestor.elevadores) {
                elev.pausado = true;
            }
        });

        // Reanudar todos los elevadores
        btnReanudar.addActionListener(e -> {
            for (Elevador elev : gestor.elevadores) {
                elev.pausado = false;
                synchronized (elev) {
                    elev.notify();
                }
            }
        });

        // =========================
        // TIMERS DE REFRESCO VISUAL
        // =========================

        // Actualiza el panel de texto con estados
        new Timer(200, e -> refrescarEstado()).start();

        // Redibuja la simulación gráfica
        new Timer(200, e -> panelGrafico.repaint()).start();

        // =========================
        // CONFIG VENTANA
        // =========================
        setSize(700, 750);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void refrescarEstado() {
        StringBuilder sb = new StringBuilder();
        for (String s : gestor.estados()) {
            sb.append(s).append('\n');
        }
        areaEstado.setText(sb.toString());
    }
}
*/
