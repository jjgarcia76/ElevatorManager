package elevador.modelo;

import java.awt.*;

public class UI extends Frame {

    public UI() {
        super("Vista Principal");
        

        // Creacion de los paneles
        ElevatorPanel panelElevador = new ElevatorPanel();
        panelElevador.setPreferredSize(new Dimension(220, 420));
        add(panelElevador, BorderLayout.CENTER);

        // Cierre de ventana
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                System.exit(0);
            }
        });

        // Ajusta la pantalla automaticamente
        pack();
        // Evita que se pueda editar el tamaño
        setResizable(false);
        // Hace que sea visible (NO PONER FALSE)
        setVisible(true);
    }

    
    class ElevatorPanel extends Panel {
        // DATO QUEMADO (cantidad de pisos pero se distorciona porque son datos predeterminados)
        private final int pisos = 5; 

        @Override
        public void paint(Graphics g) {
            super.paint(g);

            int w = getWidth();
            int h = getHeight();

            // Estilos tipo css
            int margin = 20;               
            int ejeX = margin + 40;        
            int ejeY = margin;
            int ancho = w - ejeX - margin;
            int alto = h - 2 * margin;

            // Fondo del hueco del elevador
            g.setColor(new Color(220, 220, 220));
            g.fillRect(ejeX, ejeY, ancho, alto);

            // Líneas divisorias para los pisos
            g.setColor(Color.DARK_GRAY);
            int altoPiso = alto / pisos;
            for (int i = 0; i <= pisos; i++) {
                int y = ejeY + i * altoPiso;
                g.drawLine(ejeX, y, ejeX + ancho, y);
                if (i < pisos) {
                    g.drawString("Piso " + (pisos - i), margin, y + altoPiso / 2);
                }
            }

            // Este es el primer piso que por el momento no hace nada
            int cabinaAltura = altoPiso - 10;
            int cabinaAncho = Math.max(60, ancho - 40);
            int cabinaX = ejeX + (ancho - cabinaAncho) / 2;
            int cabinaY = ejeY + alto - altoPiso + 5;

            g.setColor(new Color(70, 130, 180)); 
            g.fillRect(cabinaX, cabinaY, cabinaAncho, cabinaAltura);
            g.setColor(Color.BLACK);
            g.drawRect(cabinaX, cabinaY, cabinaAncho, cabinaAltura);
            g.drawString("Cabina", cabinaX + (cabinaAncho / 2) - 20, cabinaY - 8);
        }
    }
}
