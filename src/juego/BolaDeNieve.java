package juego;

import java.awt.Image;
import entorno.Entorno;
import entorno.Herramientas;

/**
 * Representa el proyectil enemigo (Bola de Nieve)
 * disparado por el ZombieGrinch (Opcional 1).
 * * @author [Velázquez Yoel,Céspedes Isaías,González Acuña Elías,Fuentes Elian]
 */
public class BolaDeNieve {

    // Atributos
    private double x;
    private double y;
    private int fila;
    private double velocidad;
    private Image img;
    private double escala;

    private final int ALTO_CELDA = 100;

    /**
     * Constructor de la BolaDeNieve.
     *
     * @param x    Posición X inicial.
     * @param y    Posición Y (centro de la fila).
     * @param fila Fila (0-4) por la que viajará.
     */
    public BolaDeNieve(double x, double y, int fila) {
        this.x = x;
        this.y = y;
        this.fila = fila;
        this.velocidad = 3.0; // Píxeles por tick (velocidad media)
        this.img = Herramientas.cargarImagen("recursos/zombiebola.png");

        // Lógica de reescalado
        if (this.img != null) {
            int altoOriginal = this.img.getHeight(null);
            // Escala la bola para que sea un 25% del alto de la celda
            double altoDeseado = (double) ALTO_CELDA * 0.25; // 25px
            this.escala = altoDeseado / altoOriginal;
        } else {
            this.escala = 1.0; // Valor por defecto
        }
    }

    /**
     * Mueve la bola de nieve hacia la izquierda.
     */
    public void mover() {
        this.x -= this.velocidad;
    }

    /**
     * Dibuja la bola de nieve en el entorno.
     */
    public void dibujar(Entorno e) {
        if (this.img != null) {
            e.dibujarImagen(this.img, this.x, this.y, 0, this.escala);
        } else {
            // Fallback si no carga la imagen (un círculo cian)
            e.dibujarCirculo(this.x, this.y, 10, java.awt.Color.CYAN);
        }
    }

    // --- Getters (para colisiones) ---
    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public int getFila() {
        return this.fila;
    }
}