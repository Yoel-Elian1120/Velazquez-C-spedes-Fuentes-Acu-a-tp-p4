package juego;

// Importaciones obligatorias del entorno
import entorno.Entorno;
import entorno.Herramientas;
import entorno.InterfaceJuego;

// Importaciones estandar de Java (permitidas por el temario)
import java.awt.Color;
import java.awt.Image;
import java.util.Random;

// --- ¡AGREGADAS! Importaciones para MIDI ---
import java.io.IOException;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
// --- FIN DE IMPORTACIONES MIDI ---

public class Juego extends InterfaceJuego {

	// -----------------------------------------------------------------
	// 1. ATRIBUTOS Y CONSTANTES
	// -----------------------------------------------------------------

	// --- El Entorno ---
	private Entorno entorno;

	// --- Imágenes (cargadas una sola vez) ---
	private Image imgFondo;
	private Regalo[] regalos;	
	private Image imgExplosion; // (Opcional 3)
	
	// --- ¡NUEVAS IMÁGENES DEL HUD! ---
	private Image imgPanelStats;
	private Image imgCartaZombieNormal;
	private Image imgCartaZombieTanque;
	private Image imgCartaZombieColosal;

	// --- ¡NUEVAS ESCALAS PARA EL HUD! ---
	private double escalaZombieNormal;
	private double escalaZombieTanque;
	private double escalaZombieColosal;
	

	// --- Constantes del Tablero (Ajusta estos valores) ---
	private final int FILAS = 5;
	private final int COLUMNAS = 9;	
	private final int ANCHO_CELDA = 80;
	private final int ALTO_CELDA = 100;

	// Posición inicial del tablero en la pantalla
	private final int OFFSET_X_TABLERO = 80;	
	private final int OFFSET_Y_TABLERO = 100;	

	// --- Constantes del HUD (Ajusta estos valores) ---
	private final int ALTO_HUD = 100;
	private final int POS_X_CARTA_ROSA = 50;
	private final int POS_X_CARTA_NUEZ = 150;
	private final int POS_X_CARTA_EXPLOSIVA = 250; // (Opcional 3)
	private final int POS_Y_CARTAS = 50;
	
	private final double ANCHO_CARTA_HUD = 80; // Ancho deseado para reescalar
	private final double ALTO_CARTA_HUD = 90;  // Alto deseado para reescalar

	// --- Constantes del Juego ---
	private final int ZOMBIES_TOTALES_A_ELIMINAR = 50; // Req 8
	private final int MAX_ZOMBIES_SIMULTANEOS = 15; // Req 7

	// --- Arreglos (Estructuras de datos permitidas) ---
	private Casilla[][] tablero;
	private ZombieGrinch[] zombies;
	private Proyectil[] proyectiles;
	private CartaPlanta[] cartasHUD;
	private BolaDeNieve[] bolasDeNieve; // (Opcional 1)
	
	// <-- (Opcional 5: Items) -->
	private Item[] items;
	// --- FIN ---

	// --- NUEVO (Opcional 6: Jefe Final) ---
	private ZombieColosal jefeFinal;
	private final boolean MODO_PRUEBA_JEFE = false; // ¡MODO DE JUEGO NORMAL!
	// --- FIN NUEVO ---

	// --- Variables de Estado del Juego ---
	private int zombiesEliminados;
	private int zombiesEnPantalla;	
	private long tiempoInicioJuego;
	private long proximoSpawnZombie;
	private Random random;
	private boolean juegoGanado;
	private boolean juegoPerdido;
	
	// --- VARIABLES DEL SISTEMA DE NIVELES ---
	private int nivelActual;
	private final int KILLS_POR_NIVEL = 10; // Sube de nivel cada 10 enemigos muertos
	// --- FIN ---

	// --- Variables de Estado de Input (Req 2 y 3) ---
	private CartaPlanta plantaArrastrando;	
	private Planta plantaSeleccionadaParaMover;	
	
	// --- Variables de Efecto de Explosión (Opcional 3) ---
	private int duracionExplosion;	
	private double explosionX;
	private double explosionY;

	// --- ¡AGREGADOS! Atributos para MIDI ---
	private Sequence secuencia;
	private Sequencer secuenciador;
	// --- FIN ATRIBUTOS MIDI ---

	// -----------------------------------------------------------------
	// 2. CONSTRUCTOR Y MÉTODO MAIN
	// -----------------------------------------------------------------

	Juego() {
	    // Inicializa el objeto entorno
	    this.entorno = new Entorno(this, "La Invasión de los Zombies Grinch", 800, 600);
	    this.random = new Random();

	    // Cargar imágenes estáticas
	    this.imgFondo = Herramientas.cargarImagen("recursos/fondo.png");
	    this.imgExplosion = Herramientas.cargarImagen("recursos/explosion.png");
	    
	    // --- ¡CARGAR NUEVAS IMÁGENES DEL HUD! ---
	    this.imgPanelStats = Herramientas.cargarImagen("recursos/panel_stats.png");
	    this.imgCartaZombieNormal = Herramientas.cargarImagen("recursos/carta_zombie_normal.png");
	    this.imgCartaZombieTanque = Herramientas.cargarImagen("recursos/carta_zombie_tanque.png");
	    this.imgCartaZombieColosal = Herramientas.cargarImagen("recursos/carta_zombie_colosal.png");
	    
	    // --- ¡CALCULAR SUS ESCALAS! ---
	    
	    // --- ¡NUEVO! CALCULAR ESCALA DEL PANEL DE STATS ---
	    // (Ajusta estos '200' y '90' al tamaño que QUIERES que tenga en pantalla)
	    
	    // --- FIN DE LO NUEVO ---

	    this.escalaZombieNormal = calcularEscalaImagen(this.imgCartaZombieNormal, ANCHO_CARTA_HUD, ALTO_CARTA_HUD);
	    this.escalaZombieTanque = calcularEscalaImagen(this.imgCartaZombieTanque, ANCHO_CARTA_HUD, ALTO_CARTA_HUD);
	    this.escalaZombieColosal = calcularEscalaImagen(this.imgCartaZombieColosal, 200, 100); // El colosal es más grande
	    // --- FIN ---

	    // Inicializar variables de estado
	    this.zombiesEliminados = 0;
	    this.zombiesEnPantalla = 0;
	    this.juegoGanado = false;
	    this.juegoPerdido = false;
	    this.plantaArrastrando = null;
	    this.plantaSeleccionadaParaMover = null;
	    this.tiempoInicioJuego = System.currentTimeMillis();
	    
	    this.nivelActual = 1;
	    
	    this.proximoSpawnZombie = this.tiempoInicioJuego + 3000;
	    this.duracionExplosion = 0; 

	    // Inicializar los arreglos
	    this.tablero = new Casilla[FILAS][COLUMNAS];
	    inicializarTablero();

	    this.cartasHUD = new CartaPlanta[3]; 
	    inicializarHUD();

	    inicializarRegalos(); 
	    
	    this.zombies = new ZombieGrinch[MAX_ZOMBIES_SIMULTANEOS];
	    this.proyectiles = new Proyectil[50];
	    this.bolasDeNieve = new BolaDeNieve[100];
	    this.items = new Item[20];
	    this.jefeFinal = null; 

		// --- ¡AGREGADO! Bloque de carga de MIDI ---
		// Cargar y reproducir la música MIDI (Sección 4.2.2)
	 // --- ¡AGREGADO! Bloque de carga de MIDI (Versión InputStream) ---
	 		try { 
	 		   
	 		   // 1. Obtenemos el archivo como un "flujo de datos" (más fiable)
	 		   java.io.InputStream midiStream = ClassLoader.getSystemResourceAsStream("sup.mid");
	 		   
	 		   // 2. Cargamos el flujo directamente
	 		   this.secuencia = MidiSystem.getSequence(midiStream); 
	 		    
	 		   this.secuenciador = MidiSystem.getSequencer(); 
	 		   this.secuenciador.open(); 
	 		   this.secuenciador.setSequence(this.secuencia); 
	 		   this.secuenciador.setTempoFactor(1.0f); // 1.0f es velocidad normal
	 		   this.secuenciador.setLoopCount(Sequencer.LOOP_CONTINUOUSLY); // Repetir indefinidamente
	 		   this.secuenciador.start(); 
	 		  } 
	 		  catch (NullPointerException e) {
	 			  // Esto pasará si 'midiStream' es nulo (no encontró el archivo)
	 			  System.out.println("Error al cargar MIDI: No se encontró el archivo 'end.mid' en src/");
	 			  e.printStackTrace();
	 		  }
	 		  catch (InvalidMidiDataException | IOException | MidiUnavailableException e) { 
	 			  // Esto pasará si el archivo está dañado o hay un problema de sonido
	 		   	  System.out.println("Error al cargar el archivo MIDI 'end.mid': " + e.getMessage());
	 		   	  e.printStackTrace(); 
	 		  }
	 		// --- FIN DEL BLOQUE MIDI ---
		// --- FIN DEL BLOQUE MIDI ---

	    // Inicia el juego!
	    this.entorno.iniciar();
	}

	// -----------------------------------------------------------------
	// 3. MÉTODO TICK (Bucle principal del juego)
	// -----------------------------------------------------------------

	public void tick() {
		if (juegoGanado || juegoPerdido) {
			dibujarEscena();	
			dibujarFinDeJuego();	
			return;	
		}

		long tiempoActual = System.currentTimeMillis();

		// 1. PROCESAR ENTRADAS
		manejarInput(tiempoActual);

		// 2. ACTUALIZAR ESTADO
		actualizarZombies(tiempoActual);	
		
		if (this.jefeFinal != null) { 
			this.jefeFinal.mover();
		}
		
		actualizarProyectiles();
		actualizarBolasDeNieve();
		actualizarPlantas(tiempoActual);
		actualizarEfectoExplosion();

		// 3. GENERAR NUEVAS ENTIDADES
		generarZombies(tiempoActual); 

		// 4. DETECTAR COLISIONES
		detectarColisiones();

		// 5. DIBUJAR TODO
		dibujarEscena(); 

		// 6. COMPROBAR ESTADO
		comprobarEstadoJuego(); 
	}

	// -----------------------------------------------------------------
	// 4. MÉTODOS DE INICIALIZACIÓN (Llamados desde el constructor)
	// -----------------------------------------------------------------
	
	private void inicializarRegalos() {
		this.regalos = new Regalo[FILAS];
		double x = celdaAPixelX(0);
		for (int f = 0; f < FILAS; f++) {
			double y = celdaAPixelY(f);
			this.regalos[f] = new Regalo(x, y, this.entorno, ANCHO_CELDA, ALTO_CELDA);
		}
	}

	private void inicializarTablero() {
		for (int f = 0; f < FILAS; f++) {
			for (int c = 0; c < COLUMNAS; c++) {
				double x = celdaAPixelX(c);
				double y = celdaAPixelY(f);
				String textura;
				if ((f + c) % 2 == 0) {
					textura = "recursos/pastoverde.jpg";
				} else {
					textura = "recursos/pastoseco.jpg";
				}
				this.tablero[f][c] = new Casilla(x, y, textura);
			}
		}
	}

	private void inicializarHUD() {
		long cooldownRosa = 10;
		long cooldownNuez = 10; // <-- Sigue en modo Test (10ms)
		
		this.cartasHUD[0] = new CartaPlanta(POS_X_CARTA_ROSA, POS_Y_CARTAS,
				"rosablade", cooldownRosa, "recursos/carta_rosa.png");
		this.cartasHUD[1] = new CartaPlanta(POS_X_CARTA_NUEZ, POS_Y_CARTAS,
				"wallnut", cooldownNuez, "recursos/carta_nuez.png");
				
		long cooldownExplosiva = 20000;	
		this.cartasHUD[2] = new CartaPlanta(POS_X_CARTA_EXPLOSIVA, POS_Y_CARTAS,
				"explosiva", cooldownExplosiva, "recursos/cartaexplosiva.png");
	}
	
	private void colocarTumba(int f, int c) {
		if (f >= 0 && f < FILAS && c > 0 && c < COLUMNAS) {
			if (this.tablero[f][c].ocupante == null) {
				double x = celdaAPixelX(c);
				double y = celdaAPixelY(f);
				this.tablero[f][c].ocupante = new Tumba(x, y, f, c);
			}
		}
	}


	// -----------------------------------------------------------------
	// 5. MÉTODOS DE LÓGICA (Llamados desde tick())
	// -----------------------------------------------------------------

private void manejarInput(long tiempoActual) {
		
		// --- 1. LÓGICA DE PRESIONAR EL MOUSE ---
		if (entorno.sePresionoBoton(entorno.BOTON_IZQUIERDO)) {
			
			// Primero, chequear si se clickeó el HUD para plantar
			for (int i = 0; i < cartasHUD.length; i++) {
				if (cartasHUD[i].fueClickeada(entorno.mouseX(), entorno.mouseY()) &&
						cartasHUD[i].estaLista(tiempoActual)) {
					this.plantaArrastrando = cartasHUD[i];
					this.plantaSeleccionadaParaMover = null; // Deselecciona cualquier otra
					break;	
				}
			}

			// Si no se clickeó el HUD, chequear si se clickeó un item
			if (this.plantaArrastrando == null) {
				boolean itemClickeado = chequearClickItem();
				
				// Si no fue un item, chequear si se clickeó una PLANTA en el tablero
				if (!itemClickeado) {
					// Este método PONE una planta en 'plantaSeleccionadaParaMover'
					// si se hace clic sobre ella, o la pone en NULL si se clickea pasto.
					manejarInputSeleccionPlanta();
				}
			}
		}

		// --- 2. LÓGICA DE SOLTAR EL MOUSE ---
		if (entorno.seLevantoBoton(entorno.BOTON_IZQUIERDO)) {

			// --- Caso A: Estaba arrastrando una NUEVA planta (desde el HUD) ---
			if (this.plantaArrastrando != null) {
				int[] celda = pixelACelda(entorno.mouseX(), entorno.mouseY());
				int f = celda[0];
				int c = celda[1];
				
				if (esCeldaValidaParaPlantar(f, c)) {	
					double x = celdaAPixelX(c);
					double y = celdaAPixelY(f);
					if (this.plantaArrastrando.getTipo().equals("rosablade")) {
						this.tablero[f][c].ocupante = new RoseBlade(x, y, f, c);
					} else if (this.plantaArrastrando.getTipo().equals("wallnut")) {
						this.tablero[f][c].ocupante = new WallNut(x, y, f, c);
					}
					else if (this.plantaArrastrando.getTipo().equals("explosiva")) {
						this.tablero[f][c].ocupante = new PlantaExplosiva(x, y, f, c);
					}
					
					this.plantaArrastrando.iniciarRecarga(tiempoActual);
				}
				this.plantaArrastrando = null; // Termina el arrastre
			}
			
			// --- Caso B: Estaba moviendo una planta EXISTENTE (¡NUEVA LÓGICA!) ---
			else if (this.plantaSeleccionadaParaMover != null) {
				
				// Celda original (donde estaba la planta)
				int f_actual = this.plantaSeleccionadaParaMover.getFila();
				int c_actual = this.plantaSeleccionadaParaMover.getCol();

				// Celda destino (donde se soltó el mouse)
				int[] celdaDestino = pixelACelda(entorno.mouseX(), entorno.mouseY());
				int f_nueva = celdaDestino[0];
				int c_nueva = celdaDestino[1];

				if (f_nueva == f_actual && c_nueva == c_actual) {
					// **Sub-caso 1: Se soltó en la MISMA celda.**
					// El usuario solo hizo clic, quiere usar las teclas (WASD).
					// No hacemos nada, la planta sigue seleccionada.
				}
				else if (esCeldaValidaParaPlantar(f_nueva, c_nueva)) {
					// **Sub-caso 2: Se soltó en una NUEVA celda válida.**
					// ¡Movemos la planta!
					
					Planta plantaAMover = this.plantaSeleccionadaParaMover;

					// Actualizamos sus coordenadas y fila/col
					plantaAMover.actualizarPosicion(celdaAPixelX(c_nueva), celdaAPixelY(f_nueva), f_nueva, c_nueva);
					
					// La movemos en el tablero
					this.tablero[f_nueva][c_nueva].ocupante = plantaAMover;
					this.tablero[f_actual][c_actual].ocupante = null; // Vaciar la celda vieja

					// Deseleccionamos, ya que el movimiento se completó
					this.plantaSeleccionadaParaMover = null;
				}
				else {
					// **Sub-caso 3: Se soltó en un LUGAR INVÁLIDO.**
					// (Fuera del tablero, en la columna 0, o celda ocupada)
					// Se cancela la acción. Deseleccionamos la planta.
					this.plantaSeleccionadaParaMover = null;
				}
			}
		}

		// --- 3. LÓGICA DE MOVER CON TECLADO (WASD) ---
		// Esta parte solo se ejecutará si la planta sigue seleccionada
		// (es decir, si el usuario está en el "Sub-caso 1").
		if (this.plantaSeleccionadaParaMover != null) {
			manejarInputMoverPlanta();
		}
	}

	private void manejarInputSeleccionPlanta() {
		int[] celda = pixelACelda(entorno.mouseX(), entorno.mouseY());
		int f = celda[0];
		int c = celda[1];
		if (f == -1 || c == -1) {
			this.plantaSeleccionadaParaMover = null;
			return;
		}
		Casilla casillaClickeada = this.tablero[f][c];
		
		if (casillaClickeada.ocupante != null &&	
			!(casillaClickeada.ocupante instanceof Tumba) ) {	
			
			this.plantaSeleccionadaParaMover = casillaClickeada.ocupante;
		} else {
			this.plantaSeleccionadaParaMover = null;
		}
	}

	private void manejarInputMoverPlanta() {
		if (entorno.sePresiono(entorno.TECLA_ENTER)) {
			this.plantaSeleccionadaParaMover = null;
			return;	
		}

		if (this.plantaSeleccionadaParaMover == null) {
			return;
		}

		int filaActual = this.plantaSeleccionadaParaMover.getFila();
		int colActual = this.plantaSeleccionadaParaMover.getCol();

		if (this.tablero[filaActual][colActual].ocupante != this.plantaSeleccionadaParaMover) {
			this.plantaSeleccionadaParaMover = null;
			return;
		}

		int filaNueva = filaActual;
		int colNueva = colActual;

		if (entorno.sePresiono(entorno.TECLA_ARRIBA) || entorno.sePresiono('w')) {
			filaNueva--;
		}
		if (entorno.sePresiono(entorno.TECLA_ABAJO) || entorno.sePresiono('s')) {
			filaNueva++;
		}
		if (entorno.sePresiono(entorno.TECLA_IZQUIERDA) || entorno.sePresiono('a')) {
			colNueva--;
		}
		if (entorno.sePresiono(entorno.TECLA_DERECHA) || entorno.sePresiono('d')) {
			colNueva++;
		}

		if (filaNueva == filaActual && colNueva == colActual) {
			return;
		}

		if (esCeldaValidaParaPlantar(filaNueva, colNueva)) {
			
			Planta plantaAMover = this.plantaSeleccionadaParaMover;

			plantaAMover.actualizarPosicion(celdaAPixelX(colNueva), celdaAPixelY(filaNueva), filaNueva, colNueva);
			this.tablero[filaNueva][colNueva].ocupante = plantaAMover;
			this.tablero[filaActual][colActual].ocupante = null;
		}
	}
	
	private void actualizarZombies(long tiempoActual) {
		for (int i = 0; i < this.zombies.length; i++) {
			if (this.zombies[i] != null) {
				this.zombies[i].mover();
				BolaDeNieve bola = this.zombies[i].intentarDisparar(tiempoActual);
				if (bola != null) {
					for (int b = 0; b < this.bolasDeNieve.length; b++) {
						if (this.bolasDeNieve[b] == null) {
							this.bolasDeNieve[b] = bola;
							break;	
						}
					}
				}
				if (this.zombies[i].getX() < -50) {
					this.zombies[i] = null;	
					this.zombiesEnPantalla--;
				}
			}
		}
	}

	private void actualizarProyectiles() {
		for (int i = 0; i < this.proyectiles.length; i++) {
			if (this.proyectiles[i] != null) {
				this.proyectiles[i].mover();
				if (this.proyectiles[i].getX() > entorno.ancho() + 50) {
					this.proyectiles[i] = null; // Req 11
				}
			}
		}
	}

	private void actualizarBolasDeNieve() {
		for (int i = 0; i < this.bolasDeNieve.length; i++) {
			if (this.bolasDeNieve[i] != null) {
				this.bolasDeNieve[i].mover();
				if (this.bolasDeNieve[i].getX() < 0) {	
					this.bolasDeNieve[i] = null; // Req 11
				}
			}
		}
	}
	
	private void actualizarEfectoExplosion() {
		if (this.duracionExplosion > 0) {
			this.duracionExplosion--;
		}
	}

	private void actualizarPlantas(long tiempoActual) {
		for (int f = 0; f < FILAS; f++) {
			for (int c = 0; c < COLUMNAS; c++) {
				Planta p = this.tablero[f][c].ocupante;
				if (p != null && p instanceof RoseBlade) {
					RoseBlade rosa = (RoseBlade) p;
					if (rosa.puedeDisparar(tiempoActual)) {
						for (int i = 0; i < this.proyectiles.length; i++) {
							if (this.proyectiles[i] == null) {
								this.proyectiles[i] = rosa.disparar();	
								break;
							}
						}
					}
				}
			}
		}
	}

	private void generarZombies(long tiempoActual) {

		// --- LÓGICA DE SPAWN DEL JEFE ---
		if (this.jefeFinal == null) { 
			if (MODO_PRUEBA_JEFE && this.zombiesEliminados == 0 && this.zombiesEnPantalla == 0) {
				double xSpawn = this.entorno.ancho() + 150; 
				double ySpawn = (this.entorno.alto() + ALTO_HUD) / 2.0;
				this.jefeFinal = new ZombieColosal(xSpawn, ySpawn);
				if (MODO_PRUEBA_JEFE) return; 
			}
			
			int zombiesRestantes = ZOMBIES_TOTALES_A_ELIMINAR - this.zombiesEliminados;
			
			if (!MODO_PRUEBA_JEFE && zombiesRestantes <= 0 && this.zombiesEnPantalla == 0) { // <-- CORREGIDO A '<= 0'
				double xSpawn = this.entorno.ancho() + 150;
				double ySpawn = (this.entorno.alto() + ALTO_HUD) / 2.0;
				this.jefeFinal = new ZombieColosal(xSpawn, ySpawn);
			}
		}
		// --- FIN LÓGICA DEL JEFE ---

		if (this.jefeFinal != null && MODO_PRUEBA_JEFE) {
			return; 
		}
	
		// --- Lógica de spawn de zombies normales ---
		if (this.zombiesEnPantalla >= MAX_ZOMBIES_SIMULTANEOS) {
			return;
		}
		if (tiempoActual < this.proximoSpawnZombie) {
			return;
		}
		if (this.zombiesEliminados + this.zombiesEnPantalla >= ZOMBIES_TOTALES_A_ELIMINAR) {
			return;
		}
		
		for (int i = 0; i < this.zombies.length; i++) {
			if (this.zombies[i] == null) {
				
				int filaRandom = this.random.nextInt(FILAS);	
				double xSpawn = this.entorno.ancho() + 50;	
				double ySpawn = celdaAPixelY(filaRandom);

				// --- INICIO: LÓGICA DE DIFICULTAD POR NIVEL ---
				double chanceTanque = 0.10 + (this.nivelActual * 0.10); 
				if (chanceTanque > 0.8) { 
					chanceTanque = 0.8; 
				}

				int tipoZombie;
				if (this.random.nextDouble() < chanceTanque) {
					tipoZombie = 1; // Tanque
				} else {
					tipoZombie = 0; // Normal
				}
				this.zombies[i] = new ZombieGrinch(xSpawn, ySpawn, filaRandom, tipoZombie);
				
				long baseCooldown = 2000 - (this.nivelActual * 200);
				int rangoCooldown = 3000 - (this.nivelActual * 200);

				if (baseCooldown < 500) baseCooldown = 500;
				if (rangoCooldown < 1000) rangoCooldown = 1000;

				long tiempoRandom = baseCooldown + this.random.nextInt(rangoCooldown);
				this.proximoSpawnZombie = tiempoActual + tiempoRandom;
				// --- FIN: LÓGICA DE DIFICULTAD POR NIVEL ---
				
				this.zombiesEnPantalla++;
				break; 
			}
		}
	}


	private void explotarEn(double x, double y) {
		double RADIO_EXPLOSION = 150;	
		this.explosionX = x;
		this.explosionY = y;
		this.duracionExplosion = 10;	

		for (int i = 0; i < this.zombies.length; i++) {
			ZombieGrinch z = this.zombies[i];
			if (z != null) {
				double dist = Math.sqrt(
						Math.pow(z.getX() - x, 2) +
						Math.pow(z.getY() - y, 2)
				);
				
				if (dist < RADIO_EXPLOSION) {
					this.zombies[i] = null;
					this.zombiesEliminados++;
					this.zombiesEnPantalla--;
					
					if (this.zombiesEliminados % KILLS_POR_NIVEL == 0 && this.zombiesEliminados < ZOMBIES_TOTALES_A_ELIMINAR) {
						this.nivelActual++;
						System.out.println("¡NIVEL " + this.nivelActual + " ALCANZADO!"); 
					}
				}
			}
		}
		
		if (this.jefeFinal != null) {
			double distJefe = Math.sqrt(
						Math.pow(this.jefeFinal.getX() - x, 2) +
						Math.pow(this.jefeFinal.getY() - y, 2)
				);
			if (distJefe < RADIO_EXPLOSION) {
				for (int d = 0; d < 10; d++) {
					boolean murioJefe = this.jefeFinal.recibirDisparo();
					if (murioJefe) {
						this.jefeFinal = null;
						this.zombiesEliminados += 100; // Gana el juego
						break;
					}
				}
			}
		}
	}

	private boolean chequearClickItem() {
		int mouseX = entorno.mouseX();
		int mouseY = entorno.mouseY();
		
		for (int i = 0; i < this.items.length; i++) {
			Item item = this.items[i];
			
			if (item != null && item.fueClickeado(mouseX, mouseY)) {
				aplicarEfectoItem(item.getTipo());
				this.items[i] = null;
				return true; 
			}
		}
		return false; 
	}

	private void aplicarEfectoItem(int tipo) {
		for (int i = 0; i < this.cartasHUD.length; i++) {
			if (this.cartasHUD[i] != null) {
				if (tipo == 0) { // Beneficioso
					this.cartasHUD[i].acelerarRecarga();
				} else { // Perjudicial
					this.cartasHUD[i].desacelerarRecarga();
				}
			}
		}
	}

	private void crearItem(double x, double y, int tipo) {
		for (int i = 0; i < this.items.length; i++) {
			if (this.items[i] == null) {
				this.items[i] = new Item(x, y, tipo);
				break; 
			}
		}
	}

	private void detectarColisiones() {

		// --- 1. Proyectiles vs Zombies ---
		for (int i = 0; i < this.proyectiles.length; i++) {
			Proyectil p = this.proyectiles[i];
			if (p == null) continue;
			for (int j = 0; j < this.zombies.length; j++) {
				ZombieGrinch z = this.zombies[j];
				if (z == null) continue;
				if (p.getFila() == z.getFila() && colisionan(p, z)) {
					this.proyectiles[i] = null;
					boolean murio = z.recibirDisparo();
				
					if (murio) {
						double xZombie = z.getX();
						double yZombie = z.getY();
						
						int[] celdaZombie = pixelACelda(xZombie, yZombie);
						int f = celdaZombie[0];
						int c = celdaZombie[1];
						
						if (this.zombiesEliminados == 0) {
							crearItem(xZombie, yZombie, 0); 
						} else {
							if (this.random.nextDouble() < 0.100) { 
								colocarTumba(f, c);
							} else if (this.random.nextDouble() < 0.15) { 
								int tipoItem = this.random.nextInt(2); 
								crearItem(xZombie, yZombie, tipoItem);
							}
						}

						this.zombies[j] = null;
						this.zombiesEliminados++;
						this.zombiesEnPantalla--;
						
						if (this.zombiesEliminados % KILLS_POR_NIVEL == 0 && this.zombiesEliminados < ZOMBIES_TOTALES_A_ELIMINAR) {
							this.nivelActual++;
							System.out.println("¡NIVEL " + this.nivelActual + " ALCANZADO!"); 
						}
					}
					break;
				}
			}
		}

		// --- 2. Zombies vs Plantas ---
		for (int j = 0; j < this.zombies.length; j++) {
			ZombieGrinch z = this.zombies[j];
			if (z == null) continue;
			
			int[] celdaZombie = pixelACelda(z.getX(), z.getY());
			int fz = celdaZombie[0];
			int cz = celdaZombie[1];
			if (fz == -1 || cz == -1) continue;
			
			Planta planta = this.tablero[fz][cz].ocupante;
			
			if (planta != null && colisionan(z, planta)) {
				
				if (planta instanceof RoseBlade) {
					this.tablero[fz][cz].ocupante = null; 
				} 
				else if (planta instanceof WallNut) {
					z.detenerse();
					boolean murioNuez = planta.recibirDanio(1); 
					if (murioNuez) {
						this.tablero[fz][cz].ocupante = null;
					}
				}
				else if (planta instanceof Tumba) {
					z.detenerse();
					boolean murioTumba = planta.recibirDanio(1); 
					if (murioTumba) {
						this.tablero[fz][cz].ocupante = null;
					}
				}
				else if (planta instanceof PlantaExplosiva) {
					double explosionX = planta.getX();
					double explosionY = planta.getY();
					this.tablero[fz][cz].ocupante = null; 
					explotarEn(explosionX, explosionY);
				}
			}
		}
		
		// --- 3. BolasDeNieve vs Plantas ---
		for (int i = 0; i < this.bolasDeNieve.length; i++) {
			BolaDeNieve bola = this.bolasDeNieve[i];
			if (bola == null) continue;

			int[] celdaBola = pixelACelda(bola.getX(), bola.getY());
			int fb = celdaBola[0];
			int cb = celdaBola[1];
			if (fb == -1 || cb == -1) continue;

			Planta planta = this.tablero[fb][cb].ocupante;
			
			if (planta != null && !(planta instanceof PlantaExplosiva) && colisionan(bola, planta)) {
				
				this.bolasDeNieve[i] = null;
				boolean murioPlanta = planta.recibirDanio(1); 
				if (murioPlanta) {
					this.tablero[fb][cb].ocupante = null;
				}
				break; 
			}
		}
		
		// --- 4. Proyectiles vs Tumbas ---
		for (int i = 0; i < this.proyectiles.length; i++) {
			Proyectil p = this.proyectiles[i];
			if (p == null) continue;

			int[] celdaProy = pixelACelda(p.getX(), p.getY());
			int fp = celdaProy[0];
			int cp = celdaProy[1];
			if (fp == -1 || cp == -1) continue;

			Planta planta = this.tablero[fp][cp].ocupante;

			if (planta != null && (planta instanceof Tumba)) {
				
				if (colisionan(p, planta)) {
					this.proyectiles[i] = null; 
					boolean murioObstaculo = planta.recibirDanio(1);
					if (murioObstaculo) {
						this.tablero[fp][cp].ocupante = null;
					}
					break;
				}
			}
		}
		
		// --- 5. Proyectiles vs JEFE FINAL ---
		if (this.jefeFinal != null) {
			for (int i = 0; i < this.proyectiles.length; i++) {
				Proyectil p = this.proyectiles[i];
				if (p == null) continue;
				
				if (colisionan(p, this.jefeFinal)) {
					this.proyectiles[i] = null; 
					boolean murioJefe = this.jefeFinal.recibirDisparo();
					
					if (murioJefe) {
						this.jefeFinal = null;
						this.zombiesEliminados += 100; // Gana el juego
					}
					break;
				}
			}
		}
		
		// --- 6. Jefe vs Plantas ---
		if (this.jefeFinal != null) {
			
			double frenteJefeX = this.jefeFinal.getX() - (this.jefeFinal.getAncho() / 2.0);
			int[] celda = pixelACelda(frenteJefeX, celdaAPixelY(0)); 
			int colJefe = celda[1];
			
			if (colJefe != -1) { 
				
				for (int f = 0; f < FILAS; f++) {
					Planta planta = this.tablero[f][colJefe].ocupante;
					
					if (planta != null && colisionan(this.jefeFinal, planta)) {
						if (planta instanceof RoseBlade) {
							this.tablero[f][colJefe].ocupante = null; 
						}
						else if (planta instanceof PlantaExplosiva) {
							double explosionX = planta.getX();
							double explosionY = planta.getY();
							this.tablero[f][colJefe].ocupante = null; 
							explotarEn(explosionX, explosionY);
						}
						else if (planta instanceof WallNut || planta instanceof Tumba) {
							boolean murioObstaculo = planta.recibirDanio(1);
							if (murioObstaculo) {
								this.tablero[f][colJefe].ocupante = null;
							}
						}
					}
				}
			}
		}
		
	} // <-- Fin de detectarColisiones()

	private void comprobarEstadoJuego() {
		// Condición de Victoria (Corregida para el jefe final)
	    if (this.zombiesEliminados > ZOMBIES_TOTALES_A_ELIMINAR) { 
	        this.juegoGanado = true;
	    }
		
		if (this.juegoPerdido) return;
		
		double limiteRegalosX = OFFSET_X_TABLERO + (ANCHO_CELDA / 2.0);
		
		// Condición de Derrota (Zombies Normales)
		for (int j = 0; j < this.zombies.length; j++) {
			if (this.zombies[j] != null && this.zombies[j].getX() < limiteRegalosX) {
				this.juegoPerdido = true;
				return;
			}
		}
		
		// Condición de Derrota (Jefe Final)
		if (this.jefeFinal != null) {
			double frenteJefeX = this.jefeFinal.getX() - (this.jefeFinal.getAncho() / 2.0);
			if (frenteJefeX < limiteRegalosX) {
				this.juegoPerdido = true;
			}
		}
	}


	// -----------------------------------------------------------------
	// 6. MÉTODOS DE DIBUJO (Llamados desde tick())
	// -----------------------------------------------------------------

	// --- MÉTODO HUD TOTALMENTE NUEVO (Y CORREGIDO) ---
	// --- MÉTODO HUD TOTALMENTE NUEVO (ADAPTADO A TU IMAGEN panel_stats.png) ---
	private void dibujarHUD() {
	    
	    // 1. Dibuja tu imagen "panel_stats.png" como fondo principal del HUD
	    //    (Asume que la imagen mide 800x100)
	    if (this.imgPanelStats != null) {
	        // Usa escala 1.0 para que ocupe todo el HUD
	        entorno.dibujarImagen(this.imgPanelStats, entorno.ancho() / 2, ALTO_HUD / 2, 0, 1.0); 
	    } else {
	        // Fallback si no carga la imagen (dibuja la barra gris)
	        entorno.dibujarRectangulo(entorno.ancho() / 2, ALTO_HUD / 2, entorno.ancho(), ALTO_HUD, 0, new Color(50, 50, 50));
	    }
	    
	    // (Reemplaza "Impact" con el nombre de la fuente que instalaste)
	    String nombreFuente = "Impact"; 

	    // 2. Dibuja las cartas de PLANTAS (a la izquierda)
	    long tiempoActual = System.currentTimeMillis();
	    for (int i = 0; i < this.cartasHUD.length; i++) {
	        if (this.cartasHUD[i] != null) {
	            this.cartasHUD[i].dibujar(entorno, tiempoActual);
	        }
	    }

	    // 3. Dibuja el Nivel y las Cartas de Zombies (en el centro)
	    double centroX = 400; // Centro de la pantalla
	    double centroY = 50;  // Centro del HUD
	    
	    // Dibuja el Nivel
	    try {
	        entorno.cambiarFont(nombreFuente, 18, Color.WHITE); // Texto BLANCO para el fondo de madera
	    } catch (Exception e) {
	        entorno.cambiarFont("Arial", 18, Color.WHITE); // Fallback
	    }
	    entorno.escribirTexto("NIVEL: " + this.nivelActual, centroX - 80, 30); 

	    // Dibuja la carta del Zombie Normal (siempre)
	    if (this.imgCartaZombieNormal != null) {
	        entorno.dibujarImagen(this.imgCartaZombieNormal, centroX, centroY, 0, this.escalaZombieNormal);
	    }
	    
	    // Dibuja la carta del Zombie Tanque (solo desde Nivel 2)
	    if (this.nivelActual >= 2 && this.imgCartaZombieTanque != null) {
	        entorno.dibujarImagen(this.imgCartaZombieTanque, centroX + 80, centroY, 0, this.escalaZombieTanque);
	    }
	    
	    // 4. Dibuja el texto de las estadísticas (sobre el cartel de la derecha)
	    
	    // Define la posición del cartel (ajusta 'panelStatsX' si es necesario)
	    double panelStatsX = 680; // Posición X del cartel que está en tu imagen de fondo
	    double panelStatsY = 50;  // Posición Y
	    
	    // 5. Dibuja la carta del Jefe (si aparece) O las estadísticas
	    if (this.jefeFinal != null) {
	        // Tapa las stats con la carta del jefe
	        entorno.dibujarImagen(this.imgCartaZombieColosal, panelStatsX, panelStatsY, 0, this.escalaZombieColosal);
	    } else {
	        // --- Dibuja las estadísticas sobre el cartel de madera ---
	        
	        try {
	            entorno.cambiarFont(nombreFuente, 18, Color.BLACK); // Texto NEGRO para el cartel claro
	        } catch (Exception e) {
	            entorno.cambiarFont("Arial", 16, Color.BLACK); // Fuente de respaldo
	        }
	        
	        long tiempoTranscurridoSeg = (tiempoActual - this.tiempoInicioJuego) / 1000;
	        int zombiesRestantes = ZOMBIES_TOTALES_A_ELIMINAR - this.zombiesEliminados;
	        if (zombiesRestantes < 0) zombiesRestantes = 0;

	        // --- AJUSTA ESTAS POSICIONES PARA QUE CAIGAN EN CADA TABLÓN ---
	        // (Tendrás que probar y ajustar los valores 'Y' para que queden centrados)
	        int textoX = (int)(panelStatsX - 70); // Posición X del texto (a la izquierda del centro del cartel)
	        int textoY_Eliminados = (int)panelStatsY - 5; // Primer tablón
	        int textoY_Restantes  = (int)panelStatsY + 20;  // Segundo tablón
	        int textoY_Tiempo     = (int)panelStatsY + 45; // Tercer tablón

	        entorno.escribirTexto("ELIMINADOS: " + this.zombiesEliminados, textoX, textoY_Eliminados);
	        entorno.escribirTexto("RESTANTES: " + zombiesRestantes, textoX, textoY_Restantes);
	        entorno.escribirTexto("TIEMPO: " + tiempoTranscurridoSeg, textoX, textoY_Tiempo);
	    }
	}
	// --- FIN MÉTODO HUD ---
	private void dibujarEscena() {
		// 1. Fondo
		entorno.dibujarImagen(imgFondo, entorno.ancho() / 2, entorno.alto() / 2, 0, 1.0);
		
		// 2. HUD
		dibujarHUD();
		
		// 3. Tablero (Dibuja el PASTO)
		dibujarTablero();
		
		// 4. Regalos
		dibujarRegalos();
		
		// 5. Entidades (Plantas, Zombies, Tumbas, Proyectiles)
		dibujarPlantas();	
		dibujarZombies();
		dibujarProyectiles();
		dibujarBolasDeNieve(); // (Opcional 1)

		// 6. Jefe (Opcional 6)
		if (this.jefeFinal != null) {
			this.jefeFinal.dibujar(entorno);
		}
		
		// 7. Items (Opcional 5)
		dibujarItems();
		
		// 8. Input Visual (Mouse)
		if (this.plantaArrastrando != null) {
			double escalaFantasma = 0.2;	
			entorno.dibujarImagen(
					this.plantaArrastrando.getImagen(),
					entorno.mouseX(),
					entorno.mouseY(),
					0,
					escalaFantasma);
		}
		
		// 9. Efectos Visuales (Explosión)
		dibujarEfectoExplosion(); // (Opcional 3)
	}

	private void dibujarRegalos() {
		for (int f = 0; f < this.regalos.length; f++) {
			if (this.regalos[f] != null) {
				this.regalos[f].dibujar();
			}
		}
	}

	private void dibujarTablero() {
		for (int f = 0; f < FILAS; f++) {
			for (int c = 0; c < COLUMNAS; c++) {
				if (this.tablero[f][c] != null) {
					this.tablero[f][c].dibujar(entorno);
				}
			}
		}
	}

	private void dibujarPlantas() {
		for (int f = 0; f < FILAS; f++) {
			for (int c = 0; c < COLUMNAS; c++) {
				Planta p = this.tablero[f][c].ocupante;
				if (p != null) {
					// Resplandor si está seleccionada
					if (p == this.plantaSeleccionadaParaMover) {	
						if (p instanceof WallNut) {
							entorno.dibujarCirculo(p.getX(), p.getY(), 45, Color.YELLOW);	
						}	
						else if (p instanceof RoseBlade) {
							entorno.dibujarCirculo(p.getX(), p.getY(), 40, Color.YELLOW);	
						}
						else if (p instanceof PlantaExplosiva) {
							entorno.dibujarCirculo(p.getX(), p.getY(), 40, Color.YELLOW);
						}
					}
					// Dibuja la planta (o tumba, o explosiva)
					p.dibujar(entorno);	
				}
			}
		}
	}

	private void dibujarZombies() {
		for (int i = 0; i < this.zombies.length; i++) {
			if (this.zombies[i] != null) {
				this.zombies[i].dibujar(entorno);
			}
		}
	}

	private void dibujarProyectiles() {
		for (int i = 0; i < this.proyectiles.length; i++) {
			if (this.proyectiles[i] != null) {
				this.proyectiles[i].dibujar(entorno);
			}
		}
	}
	
	private void dibujarBolasDeNieve() {
		for (int i = 0; i < this.bolasDeNieve.length; i++) {
			if (this.bolasDeNieve[i] != null) {
				this.bolasDeNieve[i].dibujar(entorno);
			}
		}
	}
	
	private void dibujarItems() {
		for (int i = 0; i < this.items.length; i++) {
			if (this.items[i] != null) {
				this.items[i].dibujar(entorno);
			}
		}
	}
	
	private void dibujarEfectoExplosion() {
		if (this.duracionExplosion > 0 && this.imgExplosion != null) {
			
			double diametroDeseado = this.ANCHO_CELDA * 3;
			int anchoOriginal = this.imgExplosion.getWidth(null);
			
			if (anchoOriginal > 0) {	
				double escala = diametroDeseado / anchoOriginal;
				entorno.dibujarImagen(this.imgExplosion, this.explosionX, this.explosionY, 0, escala);
			}
		}
	}

	private void dibujarFinDeJuego() {
		entorno.cambiarFont("Arial", 50, Color.BLACK);
		if (this.juegoGanado) {
			entorno.escribirTexto("¡GANASTE!", 270, 290);
			entorno.cambiarFont("Arial", 50, Color.GREEN);
			entorno.escribirTexto("¡GANASTE!", 272, 292);
		}
		if (this.juegoPerdido) {
			entorno.escribirTexto("¡PERDISTE!", 270, 290);
			entorno.cambiarFont("Arial", 50, Color.RED);
			entorno.escribirTexto("¡PERDISTE!", 272, 292);
		}
	}

	// -----------------------------------------------------------------
	// 7. MÉTODOS AUXILIARES (Utilidades)
	// -----------------------------------------------------------------

	// --- ¡NUEVO MÉTODO AUXILIAR PARA REESCALAR! ---
	/**
	 * Calcula la escala de una imagen para que quepa en un tamaño deseado.
	 */
	private double calcularEscalaImagen(Image img, double anchoDeseado, double altoDeseado) {
		if (img == null) {
			return 1.0;
		}
		
		int anchoOriginal = img.getWidth(null);
		int altoOriginal = img.getHeight(null);
		
		if (anchoOriginal <= 0 || altoOriginal <= 0) {
			return 1.0;
		}
		
		double escalaAncho = anchoDeseado / anchoOriginal;
		double escalaAlto = altoDeseado / altoOriginal;
		
		// Usa la escala más pequeña para que quepa sin deformarse
		return Math.min(escalaAncho, escalaAlto) * 0.9; // 0.9 para un pequeño margen
	}
	// --- FIN ---


	private int[] pixelACelda(double x, double y) {
		if (y < OFFSET_Y_TABLERO || y >= OFFSET_Y_TABLERO + FILAS * ALTO_CELDA) {
			return new int[]{-1, -1};
		}
		if (x < OFFSET_X_TABLERO || x >= OFFSET_X_TABLERO + COLUMNAS * ANCHO_CELDA) {
			return new int[]{-1, -1};
		}
		int f = (int) ((y - OFFSET_Y_TABLERO) / ALTO_CELDA);
		int c = (int) ((x - OFFSET_X_TABLERO) / ANCHO_CELDA);
		return new int[]{f, c};
	}

	private double celdaAPixelX(int col) {
		return OFFSET_X_TABLERO + (col * ANCHO_CELDA) + (ANCHO_CELDA / 2.0);
	}

	private double celdaAPixelY(int fila) {
		return OFFSET_Y_TABLERO + (fila * ALTO_CELDA) + (ALTO_CELDA / 2.0);
	}

	private boolean esCeldaValidaParaPlantar(int f, int c) {
		if (f < 0 || f >= FILAS || c < 0 || c >= COLUMNAS) {
			return false;
		}
		if (c == 0) { // Columna de regalos
			return false;
		}
		if (this.tablero[f][c].ocupante != null) { // Casilla ocupada
			return false;
		}
		return true;
	}

	private boolean colisionan(Proyectil p, ZombieGrinch z) {
		if (p == null || z == null) {
			return false;
		}
		double distancia = Math.sqrt(
				Math.pow(p.getX() - z.getX(), 2) +
				Math.pow(p.getY() - z.getY(), 2)
		);
		return distancia < 40;
	}

	private boolean colisionan(ZombieGrinch z, Planta p) {
		if (z == null || p == null) {
			return false;
		}
		double distancia = Math.sqrt(
				Math.pow(z.getX() - p.getX(), 2) +
				Math.pow(z.getY() - p.getY(), 2)
		);
		return distancia < 45; // Colisión más grande para Zombie-Planta
	}
	
	private boolean colisionan(ZombieColosal z, Planta p) {
		if (z == null || p == null) {
			return false;
		}
		return Math.abs(p.getX() - z.getX()) < (z.getAncho() / 2 + ANCHO_CELDA / 2) &&
				Math.abs(p.getY() - z.getY()) < (500 / 2 + ALTO_CELDA / 2); // 500 = alto jefe
	}

	private boolean colisionan(BolaDeNieve b, Planta p) {
		if (b == null || p == null) {
			return false;
		}
		double distancia = Math.sqrt(
				Math.pow(b.getX() - p.getX(), 2) +
				Math.pow(b.getY() - p.getY(), 2)
		);
		return distancia < 35; // Colisión más pequeña para proyectiles
	}
	
	private boolean colisionan(Proyectil p, Planta pl) {
		if (p == null || pl == null) {
			return false;
		}
		double distancia = Math.sqrt(
				Math.pow(p.getX() - pl.getX(), 2) +
				Math.pow(p.getY() - pl.getY(), 2)
		);
		return distancia < 35; // Colisión pequeña
	}

	private boolean colisionan(Proyectil p, ZombieColosal z) {
		if (p == null || z == null) {
			return false;
		}
		return Math.abs(p.getX() - z.getX()) < z.getAncho() / 2 &&
			 	Math.abs(p.getY() - z.getY()) < 500 / 2; // 500 = alto de 5 filas
	}


// Fin de la clase Juego
/**
* Método main (Punto de entrada de la aplicación)
*/
	@SuppressWarnings("unused")
		public static void main(String[] args) {
			Juego juego = new Juego();
}

} // Fin de la clase Juego