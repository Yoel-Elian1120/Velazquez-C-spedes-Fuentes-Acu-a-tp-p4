package juego;

public class Tumba extends Planta {

    public Tumba(double x, double y, int fila, int col) {
        // Llama al constructor de Planta (x, y, fila, col, vida, imagen)
        // Le damos 5 de vida y la imagen de la tumba.
        super(x, y, fila, col, 5, "recursos/tumba.png"); 
    }
    
    // ¡Eso es todo!
    // Hereda automáticamente 'dibujar()' y 'recibirDanio(int dano)' de la clase Planta.
    // No necesita 'actualizarPosicion' porque no se mueve.
    // No necesita 'disparar' porque no ataca.
}