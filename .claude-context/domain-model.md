# Modelo de Dominio - Guardianes de Gaia

## Guardian (Aggregate Root)
- GuardianId (VO): UUID único
- GuardianName (VO): 3-20 caracteres, único en Pacto
- Level (VO): 1-50
- ExperiencePoints (VO): 0-999 (reset al subir nivel)
- Element (VO): EARTH, WATER, FIRE, AIR
- PactoId (VO): Referencia al Pacto
- CreatedAt (VO): Timestamp de creación

### Invariantes
- Nombre único dentro del Pacto
- Nivel mínimo 1, máximo 50
- Experiencia se resetea al subir nivel
- Máximo 6 Guardianes por Pacto

### Comandos
- CreateGuardian(id, name, element, pactoId)
- CompleteRoute(routeId, steps, duration)
- GainExperience(points)
- LevelUp()

### Eventos
- GuardianCreated(guardianId, name, element, pactoId)
- RouteCompleted(guardianId, routeId, experienceGained)
- GuardianLeveledUp(guardianId, newLevel)

## Route (Aggregate Root)
- RouteId (VO): UUID único
- RouteName (VO): Nombre descriptivo
- MagicalPoints (VO): Lista de {lat, lng, element, bonus}
- Distance (VO): en metros
- CreatorId (VO): Guía que la creó
- PactoId (VO): Pacto al que pertenece

### Invariantes
- Mínimo 2 puntos mágicos
- Distancia mínima 100m
- Máximo 10 puntos mágicos por ruta

## Battle (Aggregate Root)
- BattleId (VO): UUID único
- GuardianId (VO): Guardián participante
- ChallengeType (VO): DAILY, BOSS, SPECIAL
- CardsPlayed (VO): Lista de CardId
- Result (VO): VICTORY, DEFEAT, ABANDONED
- EnergySpent (VO): Cantidad de energía usada
- Duration (VO): Duración en segundos

### Invariantes
- Energía mínima requerida según desafío
- Máximo 20 cartas jugadas por batalla
- Duración máxima 10 minutos

## Card (Entity)
- CardId (VO): Código QR único
- CardName (VO): Nombre de la carta
- Element (VO): EARTH, WATER, FIRE, AIR
- CardType (VO): ATTACK, DEFENSE, HEAL, UTILITY
- EnergyCost (VO): 1-5
- Effect (VO): Descripción del efecto

### Invariantes
- Costo de energía positivo
- Nombre único por elemento

## Value Objects Compartidos

### Element
- EARTH: Tierra, defensivo
- WATER: Agua, curativo
- FIRE: Fuego, ofensivo
- AIR: Aire, utilidad

### Location
- latitude: double
- longitude: double
- accuracy: metros

### DateTimeRange
- start: LocalDateTime
- end: LocalDateTime
- Invariante: start < end

## Servicios de Dominio

### EnergyCalculator
- Calcula energía por pasos
- Aplica bonus por puntos mágicos
- Límite diario de energía

### BattleEngine
- Resuelve efectos de cartas
- Calcula daño/curación
- Determina condiciones de victoria

### ExperienceCalculator
- XP por ruta completada
- Bonus por racha diaria
- Multiplicadores especiales