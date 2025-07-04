# Lenguaje Ubicuo - Guardianes de Gaia

## Conceptos Core del Juego

### Actores
- **Guardián**: Niño jugador con perfil único en el juego
- **Pacto**: Grupo familiar de 2-6 Guardianes que juegan juntos
- **Guía del Pacto**: Adulto responsable actual (rol rotativo entre padres)
- **Gaia**: La entidad que protegen los Guardianes

### Progresión
- **Nivel**: Medida de progreso del Guardián (1-50)
- **Experiencia (XP)**: Puntos acumulados para subir de nivel
- **Rango**: Título honorífico según nivel (Aprendiz, Guardián, Maestro)
- **Maestría Elemental**: Especialización en un elemento

## Dominio Walking (Caminata)

### Conceptos
- **Ruta Mágica**: Camino predefinido con puntos especiales
- **Punto Mágico**: Ubicación que otorga bonus elemental
- **Zona Elemental**: Área alrededor de un punto mágico
- **Sesión de Caminata**: Un trayecto registrado de inicio a fin

### Métricas
- **Pasos**: Unidad básica de medida de actividad
- **Energía Vital**: Recurso generado al caminar (1 energía = 10 pasos)
- **Esencia de Gaia**: Recurso universal para desafíos
- **Bonus Elemental**: Energía extra por pasar por zonas elementales

### Estados
- **En Ruta**: Guardián caminando activamente
- **Ruta Completada**: Sesión finalizada con éxito
- **Ruta Abandonada**: Sesión interrumpida

## Dominio Battle (Batalla)

### Tipos de Encuentro
- **Desafío Diario**: 5 batallas básicas por día
- **Jefe Semanal**: Batalla épica de fin de semana
- **Evento Especial**: Batallas por temporada/festividad
- **Duelo Amistoso**: Entre Guardianes del mismo Pacto

### Mecánicas
- **Turno**: Momento de juego de un participante
- **Mano**: Cartas disponibles para jugar (máx 5)
- **Mazo**: Colección completa de cartas del Guardián
- **Combo**: Combinación sinérgica de 2+ cartas
- **Cadena Elemental**: Bonus por jugar cartas del mismo elemento

### Resultados
- **Victoria Perfecta**: Ganar sin recibir daño
- **Victoria**: Completar el objetivo
- **Derrota**: No cumplir el objetivo
- **Empate**: Tiempo agotado

## Dominio Cards (Cartas)

### Clasificación
- **Carta Base**: Las 48 cartas iniciales del juego
- **Carta Especial**: Obtenidas en eventos
- **Carta Legendaria**: Únicas y poderosas

### Propiedades
- **Elemento**: TIERRA, AGUA, FUEGO, AIRE
- **Tipo**: ATAQUE, DEFENSA, CURACIÓN, UTILIDAD
- **Costo**: Energía requerida (1-5)
- **Poder**: Efectividad de la carta (1-10)
- **Rareza**: COMÚN, RARA, ÉPICA, LEGENDARIA

### Acciones
- **Escanear**: Leer QR/NFC para activar carta
- **Jugar**: Usar carta en batalla
- **Evolucionar**: Mejorar carta con recursos
- **Intercambiar**: Tradear con otros Guardianes

## Elementos del Juego

### Los Cuatro Elementos
- **TIERRA** 🌱
  - Concepto: Resistencia, protección, crecimiento
  - Ventaja contra: Agua
  - Débil contra: Aire
  
- **AGUA** 💧
  - Concepto: Fluidez, curación, adaptación
  - Ventaja contra: Fuego
  - Débil contra: Tierra

- **FUEGO** 🔥
  - Concepto: Pasión, destrucción, energía
  - Ventaja contra: Aire
  - Débil contra: Agua

- **AIRE** 💨
  - Concepto: Velocidad, evasión, libertad
  - Ventaja contra: Tierra
  - Débil contra: Fuego

## Estados del Sistema

### Estados del Guardián
- **Activo**: Jugando actualmente
- **Descansando**: Entre sesiones
- **En Batalla**: Combat activo
- **En Ruta**: Caminando

### Estados del Pacto
- **Formándose**: Menos de 2 miembros
- **Activo**: 2-6 miembros, jugando
- **Completo**: 6 miembros (máximo)
- **Inactivo**: Sin actividad en 30 días

## Métricas y Recompensas

### Logros
- **Primera Ruta**: Completar primer trayecto
- **Caminante**: 10km totales
- **Explorador**: 50km totales  
- **Peregrino**: 100km totales
- **Racha de Fuego**: 7 días consecutivos

### Multiplicadores
- **Bonus Matutino**: x1.5 XP antes de las 9am
- **Bonus de Racha**: +10% por día consecutivo
- **Bonus de Pacto**: +20% si todos caminan
- **Bonus Elemental**: x2 en punto mágico afín

## Términos Técnicos del Juego

### Recursos del Sistema
- **Llave del Guardián**: Carta QR personal única
- **Oráculo de Gaia**: La app móvil
- **Biblioteca de Rutas**: Colección de rutas del Pacto
- **Modo Cartógrafo**: Creación de rutas nuevas

### Modos de Juego
- **Modo Historia**: Dificultad adaptativa
- **Modo Desafío**: Dificultad fija
- **Modo Entrenamiento**: Sin penalizaciones
- **Modo Épico**: Máxima dificultad

## Glosario de Comandos

### Comandos de Usuario
- "Iniciar Ruta" - Comenzar tracking
- "Finalizar Ruta" - Terminar y obtener recompensas
- "Convocar Batalla" - Iniciar combate
- "Formar Pacto" - Crear grupo familiar
- "Unirse a Pacto" - Entrar a grupo existente

### Notificaciones del Sistema
- "¡Energía Completa!" - Lista para batalla
- "¡Punto Mágico Cerca!" - Bonus disponible
- "¡Nuevo Nivel!" - Subida de nivel
- "¡Desafío Disponible!" - Nueva batalla