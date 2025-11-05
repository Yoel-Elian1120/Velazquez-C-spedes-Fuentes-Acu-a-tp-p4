package juego;

import java.awt.Image;
import java.awt.Color;
import entorno.Entorno;
import entorno.Herramientas;

/**
 * Clase para el Jefe Final "Zombie Colosal" (Opcional 6).
 * Es un enemigo único con mucha vida y que ocupa las 5 filas.
 * * @author [Velázquez Yoel,Céspedes Isaías,González Acuña Elías,Fuentes Elian]
 */
public class ZombieColosal {

    private double x;
    private double y; // Posición Y (siempre será el centro de la pantalla)
    private double velocidad;
    private int vida;

    private Image imagen;
    private double escala;
    private double ancho; // Ancho de la hitbox (calculado)
    private double alto; // Alto de la hitbox (fijo a 5 filas)

    /**
     * Constructor del Jefe Final.
     *
     * @param x Posición X inicial (fuera de pantalla).
     * @param y Posición Y (centro de la pantalla).
     */
    public ZombieColosal(double x, double y) {
        this.x = x;
        this.y = y;
        this.velocidad = 0.2; // Más lento que un zombie normal
        this.vida = 50; // Vida muy alta (50 disparos)
        this.imagen = Herramientas.cargarImagen("recursos/z.png"); // Imagen del jefe

        // Lógica para "estirar" la imagen a 5 filas
        int altoDeseado = 500; // 5 filas * 100px/fila = 500
        this.alto = altoDeseado; // Alto de la hitbox

        if (this.imagen != null) {
            int altoOriginal = this.imagen.getHeight(null);
            if (altoOriginal > 0) {
                // Calcula la escala para que la imagen mida 500px de alto
                this.escala = (double) altoDeseado / altoOriginal;
            } else {
                this.escala = 1.0;
            }
            // Guarda el ancho resultante para la hitbox
            this.ancho = this.imagen.getWidth(null) * this.escala;
        } else {
            // Fallback (si no carga la imagen, es un rectángulo de 100x500)
            this.escala = 1.0;
            this.ancho = 100;
        }
    }

    /**
     * Mueve al jefe hacia la izquierda.
     */
    public void mover() {
        this.x -= this.velocidad;
    }

    /**
     * Dibuja al jefe y su barra de vida.
     */
    public void dibujar(Entorno e) {
        if (this.imagen != null) {
            e.dibujarImagen(this.imagen, this.x, this.y, 0, this.escala);
        } else {
            // Fallback si no carga: dibuja un rectángulo gigante
            e.dibujarRectangulo(this.x, this.y, this.ancho, this.alto, 0, Color.MAGENTA);
        }

        // (Opcional) Dibujar barra de vida del jefe
        double vidaMaxima = 50.0;
        double porcentajeVida = (double) this.vida / vidaMaxima;
        // Dibuja una barra roja encima del jefe que se va acortando
        e.dibujarRectangulo(this.x, this.y - (this.alto / 2) - 10, this.ancho * porcentajeVida, 10, 0, Color.RED);
    }

    /**
     * El jefe recibe un disparo y pierde 1 de vida.
     * * @return true si el jefe murió, false si sigue vivo.
     */
    public boolean recibirDisparo() {
        this.vida--;
        return this.vida <= 0;
    }

    // --- Getters (para las colisiones en Juego.java) ---
    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getAncho() {
        return this.ancho;
    }
}