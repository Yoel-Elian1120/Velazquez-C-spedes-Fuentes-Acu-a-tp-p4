package juego;

/**
 * Representa la "Planta Explosiva" (Opcional).
 * Hereda de 'Planta'.
 * Es una planta de un solo uso que explota al contacto.
 * * @author [Velázquez Yoel,Céspedes Isaías,González Acuña Elías,Fuentes Elian]
 */
public class PlantaExplosiva extends Planta {

    /**
     * Constructor de PlantaExplosiva.
     * Llama al constructor de Planta (super) con vida=1.
     */
    public PlantaExplosiva(double x, double y, int fila, int col) {
        // Llama al constructor de Planta(x, y, fila, col, vida, imagen)
        // Le damos 1 de vida (explota al primer toque)
        super(x, y, fila, col, 1, "recursos/planta_explosiva.png");
    }

    // No necesita más métodos.
    // Hereda 'dibujar()' y 'recibirDanio()' de la clase Planta.
    // Toda la lógica de la explosión (daño en área) se maneja en
    // Juego.java, dentro del método 'explotarEn()'.
}