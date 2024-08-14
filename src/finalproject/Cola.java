package finalproject;

import java.io.*; 
import java.time.LocalTime;// este es para obtener la hora cuando se solicita
import java.time.Duration;// este es para calcular la diferencia entre la creacion y la atencion
import java.util.ArrayList; // esto porque vamos a hacer una lista de colas (las multitramite)
import java.util.List;
import javax.swing.JOptionPane;

public final class Cola {

    private class ColaInfo { //a cada cola se le agregan todas estas caracteristicas sabiendo que vamos a tener diferentes colas
        Nodo cabeza, ultimo;
        int tamaño;
        Duration tiempoTotalAtencion = Duration.ZERO; //*esta es para acumular la duracion de todos los tiquetes de la cola
        int tiquetesAtendidos = 0;
        String nombre;

        public ColaInfo(String nombre) {
            this.nombre = nombre;
        }

        public void agregarTiempoAtencion(Duration duracion) {
            if (!duracion.isZero()) { //esto verifica que se tome en cuenta solo si la duracion no es 0, 
                                     //para no tomar en cuenta las cajas que no atendieron a la hora de hacer el reporte
                tiempoTotalAtencion = tiempoTotalAtencion.plus(duracion); //en caso de que se cumpla, suma la duracion al tiempo total
                tiquetesAtendidos++; 
            }
        }

        public double obtenerTiempoPromedio() { //aqui si los tiquetes atendidos es diferente a 0 se hace el calculo del tiempo promedio de atencion
            return tiquetesAtendidos == 0 ? 0 : tiempoTotalAtencion.getSeconds() / (double) tiquetesAtendidos;
        }

        public Duration obtenerTiempoTotalAtencion() {
            return tiempoTotalAtencion;
        }

        public int obtenerTiquetesAtendidos() {
            return tiquetesAtendidos;
        }

        public String getNombre() {
            return nombre;
        }
    }

    private final ColaInfo preferencial = new ColaInfo("Preferencial");
    private final ColaInfo unTramite = new ColaInfo("Caja Rápida");
    private final List<ColaInfo> multiTramite = new ArrayList<>(); //esta es la lista de colas multitramite que vamos a tener
    private final int cantCajas; //aqui se va a recibir la cantidad desde la configuracion del 
                                //sistema en prod.txt para hacer n cantidad de cajas multitramite 

    public Cola(Configuracion config) { //aqui carga la info del prod.txt
        this.cantCajas = config.getCantCajas();
        cajasMulti();
        try {
            cargarReportes(); // aqui carga los reportes existentes si es que hay, de la linea 235
        } catch (IOException e) {
            e.printStackTrace(); //*con esto se manejan errores de entrada y salida
        }
    }

    private void cajasMulti() {
        for (int i = 1; i <= cantCajas; i++) {
            ColaInfo cola = new ColaInfo("Caja Normal " + i); //crea una nueva cola para cada caja segun cantCajas
            multiTramite.add(cola); //añade la cola a la lista de colas que creamos antes n la linea 49
        }
    }

    public String insertar(Ticket pTicket) { //recibe el tiquete que queremos añadir
        ColaInfo info; //con esto se va a elaborar la cola con los atributos de antes
        String caja; //esto es para decir el nombre de cada caja (porque no sabemos cuantas Multi pueden haber, hasta ahorita se puede asignar ese valor
        boolean esPrimerTicket;

        switch (pTicket.getTipo()) { //y de acuerdo a su tipo se decide a que cola agregarla
            case 'P':
                info = preferencial;
                caja = "Preferencial";
                break;
            case 'A':
                info = unTramite;
                caja = "Caja Rápida";
                break;
            case 'B':
                info = obtenerColaConMenosTickets(); //selecciona la cola con menos tiquetes para añadirla a esa
                caja = "Caja Normal " + (multiTramite.indexOf(info) + 1); //se va sumando para que se llamen caja 1, caja 2, etc
                break;
            default:
                throw new IllegalArgumentException("Tipo de ticket no válido");
        }

        Nodo nuevo = new Nodo(pTicket);
        esPrimerTicket = info.cabeza == null;
        if (esPrimerTicket) { 
            info.cabeza = info.ultimo = nuevo; // es como poner preferencial.cabezaPreferencial, para no hacer el codigo repetitivo y solo escribir esta parte una vez
        } else {
            info.ultimo.setSiguiente(nuevo);
            info.ultimo = nuevo;
        }
        info.tamaño++;

        return esPrimerTicket //funcion lambda para darle un mensaje de cuando estara listo para atenderse su tiquete y en donde, *tambien añado aqui la parte de los grafos
                ? "Su tiquete está listo para ser atendido en la caja " + caja + prodComplementarios(pTicket)
                : "Su tiquete será atendido en la caja " + caja + ".\nHay " + antesQueTu(info.cabeza, info.ultimo) + " clientes por delante suyo.\n" + prodComplementarios(pTicket);
    }

    private ColaInfo obtenerColaConMenosTickets() {
        return multiTramite.stream() // stream convierte la lista multitramite en un flujo que con la siguiente linea se puede recorrer 
                .min((c1, c2) -> Integer.compare(c1.tamaño, c2.tamaño)) // se recorre y saca cual tiene menos tiquetes con su atributo tamaño
                .orElseThrow(() -> new RuntimeException("No se encontraron colas multitramite"));
    }

    public int antesQueTu(Nodo cabeza, Nodo ultimo) { // nada raro aqui, es solo para decirle al usuario de cuanto es la fila adelante de el
        int contador = 0;
        Nodo aux = cabeza;
        while (aux != ultimo) {
            aux = aux.getSiguiente();
            contador++;
        }
        return contador;
    }

    public String atender(ColaInfo cola) { // esta funcion se va a llamar en atender por prioridad para no escribirla varias veces
        if (cola.cabeza == null) {
            return "La cola no contiene elementos.";
        } else {
            Ticket ticketAtendido = cola.cabeza.getTicket(); //atiende siempre al primero en la cola
            LocalTime ahora = LocalTime.now(); //obtiene la hora a la que fue atendido
            Duration duracion = Duration.between(ticketAtendido.getCreacion(), ahora); // saca la duracion
            ticketAtendido.setAtencion(ahora); // aqui la asigna
            ticketAtendido.setDuracion(duracion); // aqui la asigna 
            cola.agregarTiempoAtencion(duracion); // llama a la funcion de la linea 22

            if (cola.cabeza == cola.ultimo) { 
                cola.cabeza = cola.ultimo = null;
            } else {
                cola.cabeza = cola.cabeza.getSiguiente(); 
            }

            cola.tamaño--; // se modifica el tamaño de la cola por si se siguen añadiendo tiquetes 
            escribirTicketAtendido(ticketAtendido); //se llama la funcion de la linea 146 para escribirlo en reportes 
            return "El tiquete de " + ticketAtendido.getNombre() + " fue atendido"; //retorna el nombre del ticket atendido
        }
    }

    private void escribirTicketAtendido(Ticket ticket) { // esto es para los reportes, cada tiquete atendido se agrega para ser tomado en cuenta
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("reportes.txt", true))) { //nnombre del archivo
            writer.write("Ticket Atendido - ID: " + ticket.getId() //escribe la info del ticket por partes 
                    + ", Nombre: " + ticket.getNombre()
                    + ", Trámite: " + ticket.getTramite()
                    + ", Tipo: " + ticket.getTipo()
                    + ", Edad: " + ticket.getEdad()
                    + ", Hora de Creación: " + ticket.getCreacion()
                    + ", Hora de Atención: " + ticket.getAtencion()
                    + ", Duración: " + ticket.getDuracion().getSeconds() + " segundos\n");
        } catch (IOException e) {
            e.printStackTrace(); //*
        }
    }

public String atenderPorPrioridad() {
    String menu = "Seleccione la caja para atender un tiquete:\n"
                + "1. Caja Preferencial\n"
                + "2. Caja de Trámites Rápidos\n"
                + "3. Caja de Trámites Múltiples";

    int opcion = Integer.parseInt(JOptionPane.showInputDialog(menu));

    switch (opcion) {
        case 1: // Atender en la caja preferencial
            if (preferencial.cabeza != null) {
                return atender(preferencial);
            } else {
                return "La cola preferencial está vacía.";
            }
        case 2: // Atender en la caja de trámites rápidos
            if (unTramite.cabeza != null) {
                return atender(unTramite);
            } else {
                return "La cola de trámites rápidos está vacía.";
            }
        case 3: // Atender en la caja de trámites múltiples
            for (ColaInfo cola : multiTramite) {
                if (cola.cabeza != null) {
                    return atender(cola);
                }
            }
            return "Todas las colas de trámites múltiples están vacías.";
        default:
            return "Opción no válida.";
    }
}

    public String prodComplementarios(Ticket pTicket) { //esta es la parte de los grafos, de esta no estoy tan seguro si esta bien
        switch (pTicket.getTramite()) {
            case "Depósitos":
                return ".\nOfrecer: Le ofrecemos también nuestro servicio de seguros.";
            case "Retiros":
                return ".\nOfrecer: Le recordamos que le ofrecemos la opción de retiro\n"
                        + "sin tarjeta desde los cajeros automáticos";
            case "Cambio de divisas":
                return ".\nOfrecer: Aproveche nuestras tasas preferenciales para el cambio de divisas.\n"
                        + "Además, puede obtener información sobre nuestros servicios de asesoramiento financiero.";
            default:
                throw new IllegalArgumentException("\nTipo de trámite no válido.");
        }
    }

    public void guardarTiquetesNoAtendidos() throws IOException { //esta es la parte de la persistencia de la informacion
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("tiquetes_no_atendidos.txt"))) { // el nombre del archivo
            guardarColaNoAtendidos(preferencial, writer); //escribe los restantes de cada cola
            guardarColaNoAtendidos(unTramite, writer);
            for (ColaInfo cola : multiTramite) {
                guardarColaNoAtendidos(cola, writer);
            }
        }
    }

    private void guardarColaNoAtendidos(ColaInfo cola, BufferedWriter writer) throws IOException { // aqui se comprueba si hay tickets sin atender
        Nodo nodo = cola.cabeza; // esto es como un auxiliar para recorrer la lista
        while (nodo != null) {
            Ticket ticket = nodo.getTicket(); // escribe la info de cada ticket para ser asignados al correrse de nuevo
            writer.write(ticket.getNombre() + "," + ticket.getTramite() + "," + ticket.getTipo() + "," + ticket.getEdad() + "," + ticket.getCreacion() + "\n");
            nodo = nodo.getSiguiente();
        }
    }

    public void cargarTiquetesNoAtendidos() { //la encargada de cargar los tiquetes que no se atendieron anteriormente 
        File file = new File("tiquetes_no_atendidos.txt");
        if (!file.exists()) { //si no existe nada mas ignora esta funcion
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) { //esto es para leer el archivo
            String line; //aqui se almacena cada linea que se va leyendo en el archivo
            while ((line = reader.readLine()) != null) { //con el while lee todo el archivo hasta el final
                String[] datos = line.split(","); //divide la lina en partes usando la coma como el lugar en el que separa
                String nombre = datos[0]; //aqui extrae los datos del archivo para volvr a insertar el ticket 
                String tramite = datos[1];
                char tipo = datos[2].charAt(0);
                int edad = Integer.parseInt(datos[3]);
                LocalTime creacion = LocalTime.parse(datos[4]);

                Ticket ticket = new Ticket(nombre, tramite, tipo, edad); //se define el ticket que se va a crear 
                ticket.setCreacion(creacion); //asi se mantiene su hora de creacion

                insertar(ticket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cargarReportes() throws IOException { //esta funcion es para cargar tickets resueltos anteriormente para que sean tomados en cuenta para el reporte
        File file = new File("reportes.txt");
        if (!file.exists()) { //si el archivo no existe solo se ignora la funcion
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line; //aqui igual se almacena cada linea del archivo, justo como en la funcion de cargarTiquetesNoAtendidos en la linea 210
            while ((line = reader.readLine()) != null) { //con esto recorre todo el file para ver todos los tickets
                String[] partes = line.split(", "); //mismo proceso de nuevo
                if (partes.length < 8) continue;// si el ticket estuviera incompleto mejor lo ignora

                String nombre = partes[1].split(": ")[1];
                String tramite = partes[2].split(": ")[1];
                char tipo = partes[3].split(": ")[1].charAt(0);
                int edad = Integer.parseInt(partes[4].split(": ")[1]);
                LocalTime creacion = LocalTime.parse(partes[5].split(": ")[1]);
                LocalTime atencion = LocalTime.parse(partes[6].split(": ")[1]);
                Duration duracion = Duration.ofSeconds(Long.parseLong(partes[7].split(": ")[1].replace(" segundos", "")));

                Ticket ticket = new Ticket(nombre, tramite, tipo, edad);
                ticket.setCreacion(creacion);
                ticket.setAtencion(atencion);
                ticket.setDuracion(duracion); // crea el ticket con toda la info cargada

                ColaInfo info = determinarColaPorTipo(tipo); //se asigna a la cola que corresponda
                if (info != null) {
                    info.agregarTiempoAtencion(duracion); //agrega la duracion de la cola si esta si contiene elementos, para reportes
                }
            }
        }
    }

    private ColaInfo determinarColaPorTipo(char tipo) {
        switch (tipo) {
            case 'P':
                return preferencial;
            case 'A':
                return unTramite;
            case 'B':
                return obtenerColaConMenosTickets(); //linea 107
            default:
                return null;
        }
    }

    public String generarReportes() throws IOException {
        StringBuilder reportes = new StringBuilder();

        ColaInfo mejorCaja = preferencial; //estos son los valores por defecto pero se van a ir cambiando si hay stats mejores
        String nombreCaja = "Preferencial";
        int maxTiquetes = preferencial.obtenerTiquetesAtendidos();

        if (unTramite.obtenerTiquetesAtendidos() > maxTiquetes) { //compara con la caja rapida
            mejorCaja = unTramite;
            nombreCaja = "Caja Rápida";
            maxTiquetes = unTramite.obtenerTiquetesAtendidos();
        }

        for (int i = 0; i < multiTramite.size(); i++) { //compara con cada una de las cajas multitramite
            ColaInfo cola = multiTramite.get(i);
            if (cola.obtenerTiquetesAtendidos() > maxTiquetes) {
                mejorCaja = cola;
                nombreCaja = "Caja Normal " + (i + 1);
                maxTiquetes = cola.obtenerTiquetesAtendidos();
            }
        }

        double tiempoPromedioGeneral = obtenerTiempoPromedioGeneral(); //suma el tiempo promedio de todas las colas linea 332

        reportes.append("Caja con la mayor cantidad de clientes atendidos: ") //empieza a construir el reporte que le enseñaremos al usuario
                .append(nombreCaja)
                .append(" con ")
                .append(mejorCaja.obtenerTiquetesAtendidos())
                .append(" clientes atendidos.\n");

        reportes.append("Total de clientes atendidos: ") //obtiene todos los tiquetes atendidos
                .append(preferencial.obtenerTiquetesAtendidos() + unTramite.obtenerTiquetesAtendidos()
                        + multiTramite.stream().mapToInt(ColaInfo::obtenerTiquetesAtendidos).sum())
                .append("\n");

        double tiempoPromedioMejorCaja = mejorCaja.obtenerTiempoPromedio(); //aqui saca el tiempo promedio de la mejor caja
        reportes.append("Caja con el mejor tiempo de atención promedio: ") //y lo agrega al reporte
                .append(nombreCaja)
                .append(" con ")
                .append(formatearTiempo(tiempoPromedioMejorCaja))
                .append(".\n");

        reportes.append("Tiempo promedio de atención en general: ") 
                .append(formatearTiempo(tiempoPromedioGeneral))
                .append(".");

        return reportes.toString(); //esto es lo que se va a imprimir cuando el usuario pide los reportes
    }

    private double obtenerTiempoPromedioGeneral() {
        Duration tiempoTotal = Duration.ZERO; //las stats comienzan en 0 y se iran modificando para el reporte
        int tiquetesTotales = 0;

        tiempoTotal = tiempoTotal.plus(preferencial.obtenerTiempoTotalAtencion());
        tiquetesTotales += preferencial.obtenerTiquetesAtendidos();

        tiempoTotal = tiempoTotal.plus(unTramite.obtenerTiempoTotalAtencion());
        tiquetesTotales += unTramite.obtenerTiquetesAtendidos();

        for (ColaInfo cola : multiTramite) {
            tiempoTotal = tiempoTotal.plus(cola.obtenerTiempoTotalAtencion()); //en estas lineas se suman las stats de cada caja
            tiquetesTotales += cola.obtenerTiquetesAtendidos();
        }

        return tiquetesTotales == 0 ? 0 : tiempoTotal.getSeconds() / (double) tiquetesTotales; 
    }

    private String formatearTiempo(double segundos) { //lineas 324 y 320
        long minutos = (long) (segundos / 60); //se calculan los minutos de acuerdo a cuantos segundos pasaron
        segundos %= 60;//calcula segundos restantes despues de extraer los minutos
        return minutos + " minutos y " + segundos + " segundos"; //esto devuelve el tiempo en minutos y segundos
    }


    @Override
    public String toString() { //un toString de toda la vida, para ver el estado de las cajas cuando se quiera 
        StringBuilder resultado = new StringBuilder();

        // cola Preferencial
        resultado.append("Cola Preferencial:\n");
        Nodo nodo = preferencial.cabeza;
        while (nodo != null) {
            resultado.append(nodo.toString()).append("\n");
            nodo = nodo.getSiguiente();
        }
        resultado.append("\n");

        // cola Caja Rápida
        resultado.append("Cola Caja Rápida:\n");
        nodo = unTramite.cabeza;
        while (nodo != null) {
            resultado.append(nodo.toString()).append("\n");
            nodo = nodo.getSiguiente();
        }
        resultado.append("\n");

        // colas Multi-Trámite
        resultado.append("Colas Multi-Trámite:\n");
        for (int i = 0; i < multiTramite.size(); i++) {
            resultado.append("Caja Normal ").append(i + 1).append(":\n");
            nodo = multiTramite.get(i).cabeza;
            while (nodo != null) {
                resultado.append(nodo.toString()).append("\n");
                nodo = nodo.getSiguiente();
            }
            resultado.append("\n");
        }

        return resultado.toString();
    }
}