package juego;

import java.awt.Image;
import entorno.Entorno;
import entorno.Herramientas;

/**
 * Representa un Item (poción) que cae al suelo (Opcional 5).
 * Puede ser beneficioso (tipo 0) o perjudicial (tipo 1).
 * * @author [Velázquez Yoel,Céspedes Isaías,González Acuña Elías,Fuentes Elian]
 */
public class Item {

    private double x;
    private double y;
    private int tipo; // 0 = beneficioso, 1 = perjudicial
    private Image imagen;
    private double escala;

    // Hitbox (área de clic)
    private double ancho;
    private double alto;

    /**
     * Constructor del Item.
     *
     * @param x    Posición X donde aparece (donde murió el zombie).
     * @param y    Posición Y donde aparece.
     * @param tipo 0 (bueno) o 1 (malo).
     */
    public Item(double x, double y, int tipo) {
        this.x = x;
        this.y = y;
        this.tipo = tipo;

        // Carga la imagen correspondiente al tipo
        if (this.tipo == 0) {
            this.imagen = Herramientas.cargarImagen("recursos/pocion_buena.png");
        } else {
            this.imagen = Herramientas.cargarImagen("recursos/pocion_mala.png");
        }

        // Reescalado (para que mida 50px de alto)
        if (this.imagen != null) {
            int altoOriginal = this.imagen.getHeight(null);
            this.escala = (double) (50) / altoOriginal; // 50px de alto
            
            // Guarda el tamaño de la hitbox
            this.ancho = this.imagen.getWidth(null) * this.escala;
            this.alto = this.imagen.getHeight(null) * this.escala;
        } else {
            this.escala = 1.0;
            this.ancho = 30; // Fallback
            this.alto = 30; // Fallback
        }
    }

    /**
     * Dibuja el item en el suelo.
     */
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
     * Se usa en Juego.java para saber si se "agarró" la poción.
     * * @return true si fue clickeado, false si no.
     */
    public boolean fueClickeado(int mx, int my) {
        double halfAncho = this.ancho / 2;
        double halfAlto = this.alto / 2;

        // Lógica de hitbox rectangular (clic dentro del área)
        return (mx > this.x - halfAncho && mx < this.x + halfAncho &&
                my > this.y - halfAlto && my < this.y + halfAlto);
    }

    // --- Getters ---
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