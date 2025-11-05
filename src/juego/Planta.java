package juego;

import java.awt.Image;
import entorno.Entorno;
import entorno.Herramientas;

/**
 * Clase base para todos los objetos que pueden ocupar una 'Casilla'.
 * Define los atributos y comportamientos comunes que "heredarán"
 * las clases hijas como RoseBlade, WallNut, Tumba, etc.
 * * @author [Velázquez Yoel,Céspedes Isaías,González Acuña Elías,Fuentes Elian]
 */
public class Planta {
    // Atributos 'protected' para que las clases hijas puedan accederlos
    // directamente.
    protected double x;
    protected double y;
    protected int fila;
    protected int col;
    protected int vida;
    protected Image imagen;
    protected double escala;

    // Constantes de tamaño (deben coincidir con Juego.java)
    protected final int ANCHO_CELDA = 80;
    protected final int ALTO_CELDA = 100;

    /**
     * Constructor de la Planta base.
     *
     * @param x            Posición X en píxeles (centro).
     * @param y            Posición Y en píxeles (centro).
     * @param fila         Fila (0-4) que ocupa.
     * @param col          Columna (0-8) que ocupa.
     * @param vida         Resistencia de la planta.
     * @param nombreImagen El nombre del archivo de imagen (ej: "recursos/rosa.png").
     */
    public Planta(double x, double y, int fila, int col, int vida, String nombreImagen) {
        this.x = x;
        this.y = y;
        this.fila = fila;
        this.col = col;
        this.vida = vida;
        this.imagen = Herramientas.cargarImagen(nombreImagen);

        // Lógica de reescalado (problema de imágenes deformadas)
        calcularEscala();
    }

    /**
     * Calcula la escala necesaria para que la imagen quepa en la celda
     * sin deformarse (mantiene la relación de aspecto).
     */
    private void calcularEscala() {
        if (this.imagen == null) {
            this.escala = 1.0;
            return;
        }

        int anchoOriginal = this.imagen.getWidth(null);
        int altoOriginal = this.imagen.getHeight(null);

        if (anchoOriginal <= 0 || altoOriginal <= 0) {
            this.escala = 1.0;
            return;
        }

        double escalaAncho = (double) ANCHO_CELDA / anchoOriginal;
        double escalaAlto = (double) ALTO_CELDA / altoOriginal;

        // Usa la escala MÁS PEQUEÑA (Math.min) para que la imagen quepa
        // dentro de la celda sin desbordarse.
        this.escala = Math.min(escalaAncho, escalaAlto) * 0.9; // 0.9 para un pequeño margen
    }

    /**
     * Dibuja la planta en el entorno.
     * Este método es llamado por Juego.java en cada tick.
     */
    public void dibujar(Entorno entorno) {
        if (this.imagen != null) {
            entorno.dibujarImagen(this.imagen, this.x, this.y, 0, this.escala);
        }
    }

    /**
     * Actualiza la posición de la planta (Req 3: Mover planta).
     * Se usa cuando el jugador mueve la planta con WASD o arrastrando.
     */
    public void actualizarPosicion(double nuevoX, double nuevoY, int nuevaFila, int nuevaCol) {
        this.x = nuevoX;
        this.y = nuevoY;
        this.fila = nuevaFila;
        this.col = nuevaCol;
    }

    /**
     * Reduce la vida de la planta cuando es golpeada (ej. por una BolaDeNieve).
     * * @param dano La cantidad de vida a restar.
     * @return true si la planta murió (vida <= 0), false si sigue viva.
     */
    public boolean recibirDanio(int dano) {
        this.vida -= dano;
        return this.vida <= 0; // Devuelve true si la vida llegó a 0 o menos
    }

    // --- Métodos Getters (necesarios para Juego.java) ---

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public int getFila() {
        return this.fila;
    }

    public int getCol() {
        return this.col;
    }
}