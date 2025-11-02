package juego;

import java.awt.Image;
import entorno.Entorno;
import entorno.Herramientas;

public class Item {

    private double x;
    private double y;
    private int tipo; // 0 = beneficioso, 1 = perjudicial
    private Image imagen;
    private double escala;

    // Hitbox (área de clic)
    private double ancho;
    private double alto;

    public Item(double x, double y, int tipo) {
        this.x = x;
        this.y = y;
        this.tipo = tipo;

        // Carga la imagen correspondiente
        if (this.tipo == 0) {
            this.imagen = Herramientas.cargarImagen("recursos/pocion_buena.png"); 
        } else {
            this.imagen = Herramientas.cargarImagen("recursos/pocion_mala.png"); 
        }

        // Reescalado (puedes ajustar el 0.5)
        if (this.imagen != null) {
            int altoOriginal = this.imagen.getHeight(null);
            // Escala a un 50% del alto de una celda (100 * 0.5)
            this.escala = (double)(50) / altoOriginal; 
            
            this.ancho = this.imagen.getWidth(null) * this.escala;
            this.alto = this.imagen.getHeight(null) * this.escala;
        } else {
            this.escala = 1.0;
            this.ancho = 30; // Fallback
            this.alto = 30;  // Fallback
        }
    }

    public void dibujar(Entorno e) {
        if (this.imagen != null) {
            e.dibujarImagen(this.imagen, this.x, this.y, 0, this.escala);
        } else {
            // Fallback si no carga la imagen
            if (this.tipo == 0) {
                e.dibujarCirculo(this.x, this.y, 30, java.awt.Color.GREEN);
            } else {
                e.dibujarCirculo(this.x, this.y, 30, java.awt.Color.RED);
            }
        }
    }

    /**
     * Comprueba si las coordenadas (mx, my) del mouse están dentro del item.
     */
    public boolean fueClickeado(int mx, int my) {
        double halfAncho = this.ancho / 2;
        double halfAlto = this.alto / 2;

        return (mx > this.x - halfAncho && mx < this.x + halfAncho &&
                my > this.y - halfAlto && my < this.y + halfAlto);
    }

    // Getters
    public int getTipo() {
        return this.tipo;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }
}