package finalproject;

import java.io.*;
import java.util.Scanner;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Configuracion {

    private static final String CONFIG_FILE = "prod.txt"; //aqui elegimos el nombre al archivo
    private final String nombreBanco;
    private final int cantCajas;
    private String user1 = "admin1";
    private String user2 = "admin2";
    private String pwd1 = "123";
    private String pwd2 = "321";

    public boolean autentificacion() { 
        JPanel panel = new JPanel(); // esto para crear un panel en el cual poner lo que hace falta para iniciar sesion
        JTextField userField = new JTextField(20); // con estos añadimos un lugar en el cual poner el user en el cual se verian hasta 20 digitos
        JPasswordField pwdField = new JPasswordField(20); //lo mismo aqui pero con JPassword, que lo hace invisible al usuario

        panel.add(new JLabel("Por favor ingrese su nombre de usuario: ")); //aqui se agrega todo al panel
        panel.add(userField);
        panel.add(new JLabel("Por favor ingrese su contraseña: ")); //JLabel es para poner texto de lo que estamos pidiendo
        panel.add(pwdField);

        int option = JOptionPane.showConfirmDialog(null, panel, "Autenticación", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) { //si le dan ok al ingresar datos confirma lo siguiente
            String iUser = userField.getText(); //obtiene el usuario ingresado
            String iPwd = new String(pwdField.getPassword()); // obtener la contraseña como string

            return ((iUser.equals(user1) && iPwd.equals(pwd1)) || (iUser.equals(user2) && iPwd.equals(pwd2)));//revisa que los datos coincidan
        }

        return false; 
    }

    public void scrapping() {
        try {
            Document doc = Jsoup.connect("https://servicios.davivienda.cr/master/v1/davicotizador/").get(); 
            // jsoup es para obtener y manipular info de algun website, en este caso de davivienda
            
            // aquí selecciona directamente las celdas de compra y venta del dólar del website
            String compra = doc.select("table.table-striped tbody tr:has(td:contains(DOLAR)) td.col-2").first().text();
            String venta = doc.select("table.table-striped tbody tr:has(td:contains(DOLAR)) td.col-2").get(1).text();

            JOptionPane.showMessageDialog(null, "Compra: " + compra + "\nVenta: " + venta);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "No se pudo cargar la información del website.");
        }
    }

    public Configuracion() throws IOException {
        File file = new File(CONFIG_FILE); //aqui es donde se crea el archivo con el nombre que antes le dimos
        if (file.exists()) { //aqui verifica si existe, si es el caso no hace falta pedir informacion de nuevo porque ya la tenemos de antes
            try (Scanner scanner = new Scanner(file)) { // con esto se lee el archivo
                nombreBanco = scanner.nextLine(); // lee la primera linea y la asigna como el nombre (EJ: en el txt va a decir ABC)
                cantCajas = Integer.parseInt(scanner.nextLine()); // lee la segunda linea y la asigna como cant de cajas (EJ: en el txt va a decir 3)
            }
        } else { //si fuera que no hay un archivo entonces se le pide la info al usuario y se guarda 
            nombreBanco = solicitarNombreBanco();
            cantCajas = solicitarCantidadCajas();
            guardarConfiguracion();
        }
    }

    private String solicitarNombreBanco() {
        return JOptionPane.showInputDialog("Ingrese el nombre que desea que tenga su banco: ");
    }

    private int solicitarCantidadCajas() {
        return Integer.parseInt(JOptionPane.showInputDialog("Ingrese el numero de cajas multitramite que desea disponer en su banco: "));
    }

    private void guardarConfiguracion() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONFIG_FILE))) { // con esto la info se guarda en el archivo que le indiquemos
            writer.write(nombreBanco);
            writer.newLine(); // se pone como si fuera un \n para que no vaya todo pegado
            writer.write(String.valueOf(cantCajas));
        }
    }

    // y aqui los getters
    public String getNombreBanco() {
        return nombreBanco;
    }

    public int getCantCajas() {
        return cantCajas;
    }
}
