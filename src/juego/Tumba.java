package juego;

/**
 * Representa el obstáculo "Tumba" (Opcional).
 * Hereda de 'Planta' para poder ser un 'ocupante' de 'Casilla'
 * y tener vida.
 * Aparece al morir un zombie y bloquea disparos.
 * * @author [Velázquez Yoel,Céspedes Isaías,González Acuña Elías,Fuentes Elian]
 */
public class Tumba extends Planta {

    /**
     * Constructor de la Tumba.
     * Llama al constructor de Planta (super) con vida baja (ej: 5).
     */
    public Tumba(double x, double y, int fila, int col) {
        // Llama al constructor de Planta(x, y, fila, col, vida, imagen)
        // Le damos 5 de vida (5 disparos para romperla)
        super(x, y, fila, col, 5, "recursos/tumba.png");
    }

    // ¡Eso es todo!
    // Hereda automáticamente 'dibujar()' y 'recibirDanio(int dano)' de Planta.
    // Su lógica de bloqueo se maneja en 'detectarColisiones' de Juego.java.
}