package juego;

public class WallNut extends Planta {
    
    public WallNut(double x, double y, int fila, int col) {
        // Llama al constructor de Planta (x, y, fila, col, vida, imagen)
        // Le ponemos una vida alta (ej: 20 "mordiscos")
        super(x, y, fila, col, 120, "recursos/nuez.png"); 
    }

    /**
     * Reduce la vida de la nuez en 1.
     * Este método es llamado por Juego.java cada tick que un zombie
     * está colisionando con ella.
     * * @return true si la vida de la nuez llegó a 0, false en caso contrario.
     */
    public boolean recibirDanio() {
        this.vida--; // 'vida' es un atributo heredado de Planta
        return this.vida <= 0;
    }
    
    // No tiene métodos de ataque, solo "es".
    // El método dibujar() se hereda automáticamente de Planta.
}