package juego;

/**
 * Representa la planta defensiva "WallNut" (Nuez).
 * Hereda de 'Planta'.
 * Su única función es tener una vida alta para bloquear zombies.
 * * @author [Velázquez Yoel,Céspedes Isaías,González Acuña Elías,Fuentes Elian]
 */
public class WallNut extends Planta {

    /**
     * Constructor de WallNut.
     * Llama al constructor de Planta (super) con los valores específicos
     * de esta planta (vida=120, imagen="recursos/nuez.png").
     */
    public WallNut(double x, double y, int fila, int col) {
        // Llama al constructor de Planta(x, y, fila, col, vida, imagen)
        // Le ponemos una vida alta (ej: 120 "mordiscos" o golpes)
        super(x, y, fila, col, 120, "recursos/nuez.png");
    }

    /**
     * Este método es llamado por Juego.java cada tick que un zombie
     * está colisionando con ella.
     * (NOTA: Este método no es usado, Juego.java usa el 'recibirDanio(1)'
     * heredado de Planta. Se mantiene por consistencia del desarrollo inicial).
     * * @return true si la vida de la nuez llegó a 0, false en caso contrario.
     */
    public boolean recibirDanio() {
        this.vida--; // 'vida' es un atributo heredado de Planta
        return this.vida <= 0;
    }

    // No tiene métodos de ataque, solo "es".
    // El método dibujar() se hereda automáticamente de Planta.
    // El método recibirDanio(int dano) se hereda de Planta.
}