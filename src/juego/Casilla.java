package juego;

import java.awt.Image;
import entorno.Entorno;
import entorno.Herramientas;

public class Casilla {
    private double x;
    private double y;
    private Image textura;
    public Planta ocupante; // Es 'public' para que Juego.java pueda acceder fácil

    // --- 1. DEBEMOS AÑADIR ESTAS CONSTANTES ---
    // (Deben coincidir con las de Juego.java)
    private final int ANCHO_CELDA = 80;
    private final int ALTO_CELDA = 100;
    
    // --- 2. DEBEMOS AÑADIR UNA VARIABLE DE ESCALA ---
    private double escala;

    
    public Casilla(double x, double y, String nombreTextura) {
        this.x = x;
        this.y = y;
        this.textura = Herramientas.cargarImagen(nombreTextura);
        this.ocupante = null; // Inicia vacía
        
        // --- 3. DEBEMOS CALCULAR LA ESCALA ---
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

        // Calcula las dos escalas (ancho y alto)
        double escalaAncho = (double) ANCHO_CELDA / anchoOriginal;
        double escalaAlto = (double) ALTO_CELDA / altoOriginal;
        
        // Usa la escala MÁS GRANDE (Math.max) para que la imagen
        // cubra toda la celda (aunque se desborde un poco,
        // la celda siguiente lo tapará).
        this.escala = Math.max(escalaAncho, escalaAlto);
    }

    /**
     * Dibuja la textura de pasto de esta casilla.
     */
    public void dibujar(Entorno entorno) {
        // --- 4. DEBEMOS USAR LA ESCALA CALCULADA ---
        // (En lugar de '1.0', usamos 'this.escala')
        entorno.dibujarImagen(this.textura, this.x, this.y, 0, this.escala);
    }
}