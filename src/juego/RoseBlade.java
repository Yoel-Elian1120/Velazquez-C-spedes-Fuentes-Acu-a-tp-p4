package juego;

/**
 * Representa la planta de ataque "RoseBlade".
 * Hereda de 'Planta'.
 * Su lógica principal es disparar proyectiles con un cooldown.
 * * @author [Velázquez Yoel,Céspedes Isaías,González Acuña Elías,Fuentes Elian]
 */
public class RoseBlade extends Planta {

    // Cooldown de disparo (tiempo en milisegundos)
    private final long COOLDOWN_DISPARO = 2000; // 2 segundos entre disparos
    private long ultimoDisparo;

    /**
     * Constructor de RoseBlade.
     * Llama al constructor de Planta (super) con los valores específicos
     * de esta planta (vida=1, imagen="recursos/rosa.png").
     * Req 4: Muere al ser colisionada.
     */
    public RoseBlade(double x, double y, int fila, int col) {
        // Llama al constructor de Planta(x, y, fila, col, vida, imagen)
        super(x, y, fila, col, 1, "recursos/rosa.png"); // Req 4: 1 de vida (muere al toque)

        // Permite que la planta dispare apenas se planta
        this.ultimoDisparo = System.currentTimeMillis() - COOLDOWN_DISPARO;
    }

    /**
     * Verifica si ha pasado suficiente tiempo para volver a disparar.
     * Es llamado desde Juego.java en 'actualizarPlantas()'.
     * * @param tiempoActual El tiempo actual del sistema.
     * @return true si puede disparar, false si está en cooldown.
     */
    public boolean puedeDisparar(long tiempoActual) {
        return (tiempoActual - this.ultimoDisparo) >= COOLDOWN_DISPARO;
    }

    /**
     * Crea y devuelve un nuevo objeto Proyectil.
     * Actualiza el tiempo del último disparo para reiniciar el cooldown.
     * * @return Un nuevo objeto Proyectil.
     */
    public Proyectil disparar() {
        this.ultimoDisparo = System.currentTimeMillis();

        // Crea el proyectil un poco a la derecha de la planta
        return new Proyectil(this.x + 30, this.y, this.fila);
    }

    // El método dibujar() se hereda automáticamente de Planta.
    // El método recibirDanio() se hereda automáticamente de Planta.
}