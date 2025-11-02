package juego;

import java.awt.Image;
import entorno.Entorno;
import entorno.Herramientas;

public class BolaDeNieve {

    // Atributos
    private double x;
    private double y;
    private int fila;
    private double velocidad;
    private Image img;
    private double escala; // <-- Atributo para la escala

    // Constante para el reescalado (debe coincidir con Juego.java)
    private final int ALTO_CELDA = 100;

    // Constructor
    public BolaDeNieve(double x, double y, int fila) {
        this.x = x;
        this.y = y;
        this.fila = fila;
        this.velocidad = 3; // Ajusta la velocidad
        this.img = Herramientas.cargarImagen("recursos/zombiebola.png"); 
        
        // <-- NUEVO: Lógica de reescalado -->
        if (this.img != null) {
            int altoOriginal = this.img.getHeight(null);
            // Hacemos que la bola sea, por ejemplo, un 25% del alto de la celda
            double altoDeseado = (double)ALTO_CELDA * 0.25; 
            this.escala = altoDeseado / altoOriginal;
        } else {
            this.escala = 1.0; // Valor por defecto si no carga
        }
        // --- FIN NUEVO ---
    }

    // Métodos
    
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
            // <-- MODIFICADO: Usa la escala calculada -->
            e.dibujarImagen(this.img, this.x, this.y, 0, this.escala);
        } else {
            // Fallback si no carga la imagen (un círculo de 10px)
            e.dibujarCirculo(this.x, this.y, 10, java.awt.Color.CYAN);
        }
    }

    // Getters (para las colisiones)
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