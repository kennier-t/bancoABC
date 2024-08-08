package finalproject;

public class Nodo {

    private Ticket ticket;
    private Nodo siguiente;

    public Nodo(Ticket ticket) {
        this.ticket = ticket;
        siguiente = null;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public Nodo getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(Nodo siguiente) {
        this.siguiente = siguiente;
    }

    @Override
    public String toString() {
        return ticket.toString();
    }

}
