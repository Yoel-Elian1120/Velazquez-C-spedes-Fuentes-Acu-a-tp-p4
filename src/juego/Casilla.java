package juego;

import java.awt.Image;
import entorno.Entorno;
import entorno.Herramientas;

/**
 * Representa una única celda (casillero) en el tablero de juego.
 * Actúa como un contenedor para la textura de pasto y para
 * la planta que la está ocupando.
 * * @author [Velázquez Yoel,Céspedes Isaías,González Acuña Elías,Fuentes Elian]
 */
public class Casilla {
    private double x;
    private double y;
    private Image textura;
    
    /**
     * Referencia a la planta que está en esta casilla.
     * Es 'public' para que Juego.java pueda accederla fácilmente (ej:
     * tablero[f][c].ocupante = new RoseBlade(...)).
     * Si no hay planta, su valor es 'null'.
     */
    public Planta ocupante;

    // Constantes de tamaño (deben coincidir con Juego.java)
    private final int ANCHO_CELDA = 80;
    private final int ALTO_CELDA = 100;
    
    private double escala;

    /**
     * Constructor de la Casilla.
     *
     * @param x             Posición X del centro.
     * @param y             Posición Y del centro.
     * @param nombreTextura El archivo de imagen (pasto verde o seco).
     */
    public Casilla(double x, double y, String nombreTextura) {
        this.x = x;
        this.y = y;
        this.textura = Herramientas.cargarImagen(nombreTextura);
        this.ocupante = null; // Todas las casillas inician vacías

        // Calcula la escala para que la textura RELLENE la celda
        this.calcularEscala();
    }

    /**
     * Calcula la escala necesaria para que la textura
     * RELLENE la celda de 80x100.
     */
    private void calcularEscala() {
        if (this.textura == null) {
            this.escala = 1.0;
            return;
        }

        int anchoOriginal = this.textura.getWidth(null);
        int altoOriginal = this.textura.getHeight(null);

        if (anchoOriginal <= 0 || altoOriginal <= 0) {
            this.escala = 1.0;
            return;
        }

        double escalaAncho = (double) ANCHO_CELDA / anchoOriginal;
        double escalaAlto = (double) ALTO_CELDA / altoOriginal;

        // Usa la escala MÁS GRANDE (Math.max) para que la imagen
        // cubra toda la celda (se desborda, pero la celda vecina lo tapa).
        this.escala = Math.max(escalaAncho, escalaAlto);
    }

    /**
     * Dibuja la textura de pasto de esta casilla.
     * (No dibuja la planta, eso se hace en Juego.java).
     */
    public void dibujar(Entorno entorno) {
        if (this.textura != null) {
            entorno.dibujarImagen(this.textura, this.x, this.y, 0, this.escala);
        }
    }
}