package juego;

public class PlantaExplosiva extends Planta {

    public PlantaExplosiva(double x, double y, int fila, int col) {
        // Llama al constructor de Planta (x, y, fila, col, vida, imagen)
        
        // Le damos 1 de vida (explota al primer toque)
        // y la imagen "planta_explosiva.png"
        // Tu clase Planta se encargará automáticamente de reescalarla.
        super(x, y, fila, col, 1, "recursos/planta_explosiva.png"); 
    }
    
    // No necesita más métodos.
    // Hereda 'dibujar()' y 'recibirDanio()' de la clase Planta.
    // Su lógica de explosión se maneja en Juego.java.
}