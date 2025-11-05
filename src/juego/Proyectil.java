package juego;

import java.awt.Image;
import entorno.Entorno;
import entorno.Herramientas;

/**
 * Representa el proyectil aliado (bola de fuego) disparado por la RoseBlade.
 * * @author [Velázquez Yoel,Céspedes Isaías,González Acuña Elías,Fuentes Elian]
 */
public class Proyectil {
    private double x;
    private double y;
    private int fila;
    private double velocidad;
    private Image imagen;
    private double escala;

    // Define el tamaño (diámetro) deseado para la bola de fuego
    private final double TAMANIO_DESEADO = 40.0;

    /**
     * Constructor del Proyectil.
     *
     * @param x    Posición X inicial.
     * @param y    Posición Y (centro de la fila).
     * @param fila Fila (0-4) por la que viajará.
     */
    public Proyectil(double x, double y, int fila) {
        this.x = x;
        this.y = y;
        this.fila = fila;
        this.velocidad = 4.0; // Píxeles por tick (rápido)
        this.imagen = Herramientas.cargarImagen("recursos/balita.png");

        // --- LÓGICA DE REESCALADO (para que no se deforme) ---
        if (this.imagen != null) {
            int anchoOriginal = this.imagen.getWidth(null);
            int altoOriginal = this.imagen.getHeight(null);

            if (anchoOriginal > 0 && altoOriginal > 0) {
                double escalaAncho = TAMANIO_DESEADO / anchoOriginal;
                double escalaAlto = TAMANIO_DESEADO / altoOriginal;
                // Usa Math.min para que quepa en un cuadrado de 40x40
                this.escala = Math.min(escalaAncho, escalaAlto);
            } else {
                this.escala = 0.1; // Escala de emergencia
            }
        } else {
            this.escala = 1.0;
        }
    }

    /**
     * Mueve el proyectil hacia la derecha.
     */
    public void mover() {
        this.x += this.velocidad;
    }

    /**
     * Dibuja el proyectil.
     */
    public void dibujar(Entorno entorno) {
        if (this.imagen != null) {
            entorno.dibujarImagen(this.imagen, this.x, this.y, 0, this.escala);
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