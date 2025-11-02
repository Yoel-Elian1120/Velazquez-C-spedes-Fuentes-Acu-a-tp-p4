package juego;

import java.awt.Image;
import entorno.Entorno;
import entorno.Herramientas;

public class Proyectil {
    private double x;
    private double y;
    private int fila;
    private double velocidad;
    private Image imagen;
    private double escala;
    
    // Define aquí el tamaño que quieres que tenga la bola (ej: 25 píxeles)
    private final double TAMANIO_DESEADO = 40.0; 

    public Proyectil(double x, double y, int fila) {
        this.x = x;
        this.y = y;
        this.fila = fila;
        this.velocidad = 4.0; // Píxeles por tick
        
        // Usamos el nombre de imagen que pusiste: "bola.png"
        this.imagen = Herramientas.cargarImagen("recursos/balita.png");
        // --- LÓGICA DE REESCALADO (CORREGIDA) ---
        // Esto asegura que la bola mantenga su forma original
        if (this.imagen != null) {
            int anchoOriginal = this.imagen.getWidth(null);
            int altoOriginal = this.imagen.getHeight(null);

            // Si la imagen se cargó mal (ancho 0), previene división por cero
            if (anchoOriginal > 0 && altoOriginal > 0) {
                // Calcula la escala necesaria para el ancho y el alto
                double escalaAncho = TAMANIO_DESEADO / anchoOriginal;
                double escalaAlto = TAMANIO_DESEADO / altoOriginal;
                
                // Usa la escala más pequeña de las dos
                // Así te aseguras que quepa en un cuadrado de 25x25 sin deformarse
                this.escala = Math.min(escalaAncho, escalaAlto);
            } else {
                // Si la imagen no cargó bien, usa una escala por defecto
                this.escala = 0.1; 
            }

        } else {
            this.escala = 1.0; // No se pudo cargar la imagen
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
        // Asegúrate que esta línea esté activa
        entorno.dibujarImagen(this.imagen, this.x, this.y, 0, this.escala);

        // Borra o comenta la línea del círculo rojo
        // entorno.dibujarCirculo(this.x, this.y, 20, java.awt.Color.RED);
    }

    // --- Getters ---
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