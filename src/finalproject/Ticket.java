package finalproject;

import java.time.LocalTime;
import java.time.Duration; 

public class Ticket {

    private static int idCounter = 1; // se crea un contador estatico para que todos tengan un id diferente 
    private String nombre, tramite;
    private char tipo;
    private LocalTime creacion, atencion;
    private int id, edad;
    private Duration duracion; 

    public Ticket(String nombre, String tramite, char tipo, int edad) {
        this.nombre = nombre;
        this.tramite = tramite;
        this.tipo = tipo;
        this.edad = edad;
        id = idCounter++;
        creacion = LocalTime.now();
        atencion = null;
        duracion = null;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTramite() {
        return tramite;
    }

    public void setTramite(String tramite) {
        this.tramite = tramite;
    }

    public char getTipo() {
        return tipo;
    }

    public void setTipo(char tipo) {
        this.tipo = tipo;
    }

    public LocalTime getCreacion() {
        return creacion;
    }

    public void setCreacion(LocalTime creacion) {
        this.creacion = creacion;
    }

    public LocalTime getAtencion() {
        return atencion;
    }

    public void setAtencion(LocalTime atencion) {
        this.atencion = atencion;
    }

    public int getId() {
        return id;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public Duration getDuracion() {
        return duracion;
    }

    public void setDuracion(Duration duracion) {
        this.duracion = duracion;
    }

    @Override
    public String toString() {
        return "El usuario " + nombre + " con el ID de " + id + ",\n"
                + " quien tiene " + edad + " años. Su tramite es " + tramite + "\n"
                + " y el tipo del tiquete asignado es tipo " + tipo + ".";
    }
}
