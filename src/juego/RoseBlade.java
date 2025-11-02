package juego;

import entorno.Entorno;

public class RoseBlade extends Planta {
    
    private final long COOLDOWN_DISPARO = 2000; // 2 segundos entre disparos
    private long ultimoDisparo;

    public RoseBlade(double x, double y, int fila, int col) {
        // Llama al constructor de Planta (x, y, fila, col, vida, imagen)
    	super(x, y, fila, col, 1, "recursos/rosa.png"); // Req 4: 1 de vida (muere al toque)
        
        this.ultimoDisparo = System.currentTimeMillis(); // Puede disparar de inmediato
    }
    
    /**
     * Verifica si ha pasado suficiente tiempo para volver a disparar.
     */
    public boolean puedeDisparar(long tiempoActual) {
        return (tiempoActual - this.ultimoDisparo) >= COOLDOWN_DISPARO;
    }

    /**
     * Crea y devuelve un nuevo objeto Proyectil.
     * Actualiza el tiempo del último disparo.
     */
    public Proyectil disparar() {
        this.ultimoDisparo = System.currentTimeMillis();
        
        // Crea el proyectil un poco a la derecha de la planta
        // (Asume que Proyectil(x, y, fila))
        return new Proyectil(this.x + 30, this.y, this.fila);
    }
    
    // El método dibujar() se hereda automáticamente de Planta
}