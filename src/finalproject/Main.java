package finalproject;

import java.io.IOException;
import javax.swing.JOptionPane;

public class Main {

    public static void main(String[] args) throws IOException {

        Configuracion config = new Configuracion();
        Cola c1 = new Cola(config);
        if (config.autentificacion()) {
            boolean menu = true;
            c1.cargarTiquetesNoAtendidos();
            JOptionPane.showMessageDialog(null, "Bienvenido al banco " + config.getNombreBanco());
            while (menu) {
                int option = Integer.parseInt(JOptionPane.showInputDialog("<html><b>MENÚ</b></html>\n"
                        + "Por favor elija una de las opciones: \n"
                        + "1. Crear un tiquete.\n"
                        + "2. Atender tiquete.\n"
                        + "3. Ver estado de las cajas.\n"
                        + "4. Generar reportes.\n"
                        + "5. Ver cambio del dolar.\n"
                        + "6. Salir"));
                switch (option) {
                    case 1 -> {
                        String nombre = JOptionPane.showInputDialog("Ingrese su nombre: ");
                        int id = Integer.parseInt(JOptionPane.showInputDialog("Ingrese su edad: "));
                        String tramite = JOptionPane.showInputDialog("Ingrese el tipo de trámite:\n"
                                + "• Depósitos\n"
                                + "• Retiros\n"
                                + "• Cambio de divisas");
                        char tipo = (JOptionPane.showInputDialog("Ingrese el tipo de trámite:\n"
                                + "• P: Preferencial\n"
                                + "• A: Un solo trámite\n"
                                + "• B: Dos o más trámites").charAt(0));
                        JOptionPane.showMessageDialog(null, c1.insertar(new Ticket(nombre, tramite, tipo, id)));
                    }
                    case 2 ->
                        JOptionPane.showMessageDialog(null, c1.atenderPorPrioridad());
                    case 3 ->
                        JOptionPane.showMessageDialog(null, c1.toString());
                    case 4 -> {
                        JOptionPane.showMessageDialog(null, c1.generarReportes());
                    }
                    case 5 ->
                        config.scrapping();
                    case 6 -> {
                        c1.guardarTiquetesNoAtendidos();
                        menu = false;
                    }
                    default ->
                        JOptionPane.showMessageDialog(null, "La opción no es válida.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "Su nombre de usuario o contraseña no son válidos.");
        }
    }
}
