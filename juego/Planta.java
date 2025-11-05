package juego;

import java.awt.Image;
import entorno.Entorno;
import entorno.Herramientas;

public class Planta {
    // Atributos protegidos para que las clases hijas (RoseBlade) puedan usarlos
    protected double x;
    protected double y;
    protected int fila;
    protected int col;
    protected int vida; // <-- ¡Excelente que ya tengas este atributo!
    protected Image imagen;
    protected double escala;

    // Constantes de tamaño (deben coincidir con Juego.java)
    protected final int ANCHO_CELDA = 80;
    protected final int ALTO_CELDA = 100;

    public Planta(double x, double y, int fila, int col, int vida, String nombreImagen) {
        this.x = x;
        this.y = y;
        this.fila = fila;
        this.col = col;
        this.vida = vida; // <-- Se asigna la vida que viene del constructor
        this.imagen = Herramientas.cargarImagen(nombreImagen);
        
        // Lógica de reescalado (Req de reescalar imagen)
        calcularEscala();
    }

    /**
     * Calcula la escala necesaria para que la imagen quepa en la celda.
     */
    private void calcularEscala() {
        if (this.imagen == null) {
            this.escala = 1.0;
            return;
        }
        
        int anchoOriginal = this.imagen.getWidth(null);
        int altoOriginal = this.imagen.getHeight(null);

        double escalaAncho = (double) ANCHO_CELDA / anchoOriginal;
        double escalaAlto = (double) ALTO_CELDA / altoOriginal;
        
        // Usa la escala más pequeña para que quepa sin deformarse
        this.escala = Math.min(escalaAncho, escalaAlto) * 0.9; // 0.9 para un pequeño margen
    }

    /**
     * Dibuja la planta en el entorno.
     */
    public void dibujar(Entorno entorno) {
        entorno.dibujarImagen(this.imagen, this.x, this.y, 0, this.escala);
    }
    
    /**
     * Actualiza la posición de la planta (Req 3: Mover planta)
     */
    public void actualizarPosicion(double nuevoX, double nuevoY, int nuevaFila, int nuevaCol) {
        this.x = nuevoX;
        this.y = nuevoY;
        this.fila = nuevaFila;
        this.col = nuevaCol;
    }

    // --- NUEVO: MÉTODO PARA RECIBIR DAÑO ---
    /**
     * Reduce la vida de la planta cuando es golpeada (ej. por una BolaDeNieve).
     * @param dano La cantidad de vida a restar.
     * @return true si la planta murió (vida <= 0), false si sigue viva.
     */
    public boolean recibirDanio(int dano) {
        this.vida -= dano;
        return this.vida <= 0; // Devuelve true si la vida llegó a 0 o menos
    }
    // --- FIN DE LO NUEVO ---


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