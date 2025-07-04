# Resumen Completo del Proyecto "Guardianes de Gaia"

## **📋 Información General del Proyecto**

### **Concepto Central**
"Guardianes de Gaia" es un juego de rol y cartas cooperativo que transforma los trayectos diarios a pie al colegio en una saga épica. El objetivo es hacer que caminar sea divertido y adictivo para familias con niños de 6-12 años.

### **Problema que Resuelve**
- **Sedentarismo infantil**: Falta de actividad física diaria
- **Desconexión con la naturaleza**: Los niños pasan poco tiempo al aire libre
- **Rutinas aburridas**: El trayecto al colegio es visto como una obligación
- **Falta de tiempo familiar**: Pocas actividades compartidas entre padres e hijos

### **Propuesta de Valor**
- Convierte el camino al colegio en una aventura épica
- Fomenta ejercicio físico sin que se sienta como ejercicio
- Crea vínculos familiares a través del juego cooperativo
- Desarrolla conciencia ambiental mediante narrativa ecológica

## **🎮 Mecánicas de Juego Básicas**

### **Flujo de Juego Diario**
1. **Mañana**: Caminar al colegio recolecta recursos automáticamente
2. **Tarde**: Usar esos recursos en una partida de cartas cooperativa
3. **Noche**: (Versiones avanzadas) Crafteo de objetos para el día siguiente

### **Componentes Físicos**
- **Cartas Interactivas**: Pueden ser escaneadas via QR (básico) o NFC (premium)
- **Llave del Guardián**: Carta de identidad única por niño
- **Cartas de Habilidad**: Representan acciones/hechizos en el juego
- **Cartas de Amuleto**: Equipamiento que otorga bonus pasivos (versiones avanzadas)

### **Recursos del Juego**
- **Energía Vital**: Se obtiene caminando (automático por pasos)
- **Esencia de Gaia**: Recurso universal para completar desafíos
- **Bonus Elementales**: Ventajas opcionales por pasar por zonas específicas (Tierra, Agua, Fuego, Aire)

### **Estructura de Equipo**
- **Guardianes**: Los niños jugadores (2-6 por Pacto)
- **Pacto**: El equipo/guild de Guardianes
- **Guía del Pacto**: Adulto facilitador (rol rotativo entre padres)

## **📱 Componente Tecnológico**

### **App "Oráculo de Gaia"**
- **Gestión del Pacto**: Dashboard para padres
- **Escáner de Cartas**: Lee códigos QR/NFC
- **Contador de Pasos**: Tracking automático de actividad
- **Generador de Rutas**: Sistema de mapeo colaborativo
- **Narrador Opcional**: Voz automática para eventos

### **Sistema de Rutas Colaborativo**
**Innovación Clave**: Los propios Guías marcan los lugares especiales en sus rutas habituales, en lugar de depender de bases de datos externas.

#### **Proceso de Creación de Rutas**:
1. **Modo Cartógrafo**: Grabación GPS del trayecto
2. **Marcado Manual**: Los padres señalan "lugares mágicos"
3. **Clasificación Elemental**: Asignan tipo de bonus a cada lugar
4. **Personalización**: Nombres únicos para cada ubicación
5. **Biblioteca Personal**: Gestión de múltiples rutas por situación

#### **Ventajas del Sistema**:
- Funciona en cualquier lugar del mundo
- Precisión total según cada familia
- Flexibilidad completa en horarios
- Engagement adicional de los adultos

## **🏗️ Evolución del Diseño**

### **Versión Original (Problemas Identificados)**
- **Excesiva complejidad**: Múltiples tipos de recursos, mecánicas avanzadas desde el inicio
- **Dependencia tecnológica**: Solo NFC, requerimientos técnicos altos
- **Rigidez de rutas**: Dependencia de ubicaciones específicas para recursos
- **Sobrecarga del adulto**: Un solo Guía con todas las responsabilidades

### **Primera Iteración de Mejoras**
- **Sistema Dual QR/NFC**: Accesibilidad tecnológica mejorada
- **Roles Rotativos**: Distribución de responsabilidades entre múltiples adultos
- **Tres Eras de Complejidad**: Introducción gradual de mecánicas
- **Herramientas de Gestión**: Dashboard parental, biblioteca de rutas

### **Segunda Iteración (Sistema de Rutas)**
- **Ecos Universales**: Un solo tipo de recurso base para eliminar dependencias de ubicación
- **Bonus Opcionales**: Ventajas por variedad, no requisitos obligatorios
- **Mapeo Colaborativo**: Los Guías crean sus propios mapas mágicos
- **Flexibilidad Total**: Funciona con cualquier ruta habitual

## **🎯 Versión MVP (Producto Mínimo Viable)**

### **Alcance del MVP**
- **Duración**: 8 semanas de desarrollo
- **Presupuesto**: €11,500
- **Objetivo**: Validar la pregunta central: "¿Puede un juego de cartas hacer que caminar al colegio sea adictivo?"

### **Funcionalidades Incluidas**
- ✅ Contador de pasos básico
- ✅ Escaneo QR de cartas
- ✅ 48 cartas básicas (12 por elemento)
- ✅ 5 desafíos diarios simples
- ✅ Progresión de XP básica (niveles 1-10)
- ✅ Marcado manual de rutas
- ✅ Dashboard básico de progreso

### **Funcionalidades Excluidas (Para V2+)**
- ❌ Sagas semanales narrativas
- ❌ Sistema de clases/especializaciones
- ❌ Amuletos y loot avanzado
- ❌ Eventos estacionales
- ❌ Múltiples Pactos
- ❌ Intercambio de rutas entre usuarios
- ❌ Detección automática de zonas

### **Stack Tecnológico MVP**
- **Frontend**: React Native
- **Base de Datos**: SQLite local
- **Cámara**: react-native-camera
- **GPS**: react-native-background-job
- **Cartas**: PDF imprimible (sin costes de producción)

## **📊 Métricas de Éxito**

### **KPIs Principales**
- **Retención Semanal**: >70% tras 4 semanas
- **Engagement Diario**: >5 días/semana de uso
- **Satisfacción Familiar**: >8/10 en encuesta
- **Tiempo de Setup**: <30 minutos primera vez
- **Completación Tutorial**: >90%

### **Plan de Testing**
1. **Testing Interno**: 1 familia, 1 semana (detectar bugs críticos)
2. **Beta Cerrado**: 3-5 familias, 2 semanas (validar engagement)
3. **Beta Abierto**: 15-20 familias, 4 semanas (validar escalabilidad)

## **🗓️ Roadmap de Desarrollo**

### **MVP (Semanas 1-8)**
- **Sprint 1-2**: Fundamentos técnicos y navegación básica
- **Sprint 3-4**: Gameplay core y mecánicas de cartas
- **Sprint 5-6**: Integración completa y balanceado
- **Sprint 7-8**: Polish, testing y preparación para beta

### **V2: Sagas Semanales (Mes 3-4)**
- Narrativas conectadas de 5 días
- Sistema de crafteo básico
- Eventos estacionales simples
- Mejoras de UI/UX

### **V3: Comunidad (Mes 5-6)**
- Múltiples Pactos por instalación
- Intercambio de rutas básico
- Sistema de guild mejorado
- Funciones sociales básicas

### **V4: Especialización (Mes 7-8)**
- Clases de Guardián (Protector/Sanador/Invocador)
- Sistema de loot y Amuletos
- Eventos de mundo globales
- Monetización sostenible

## **🎴 Sistema de Cartas del MVP**

### **Distribución por Elemento (48 cartas totales)**
Cada elemento tiene 12 cartas distribuidas en 4 categorías:
- **3 cartas de Defensa**: Protección y mitigación de daño
- **3 cartas de Curación**: Recuperación y soporte
- **3 cartas de Ataque**: Daño directo y control
- **3 cartas de Utilidad**: Efectos especiales y ventajas

### **Ejemplos de Cartas por Elemento**

**🌱 Tierra**: Escudo de Piedra, Bálsamo de Flores, Temblor Menor, Crecimiento Rápido
**💧 Agua**: Escudo de Hielo, Lluvia Sanadora, Chorro de Presión, Camino de Hielo
**🔥 Fuego**: Muro de Llamas, Calor Vital, Bola de Fuego, Antorcha Eterna
**💨 Aire**: Escudo de Viento, Brisa Revitalizante, Ráfaga Cortante, Vuelo Breve

## **💡 Decisiones de Diseño Clave**

### **1. Cooperación vs Competición**
- **Decisión**: 100% cooperativo
- **Razón**: Fomenta vínculos familiares y trabajo en equipo

### **2. Automatización vs Control Manual**
- **Decisión**: Mapeo colaborativo manual por los Guías
- **Razón**: Mayor engagement adulto, funciona universalmente, precisión total

### **3. Complejidad Gradual**
- **Decisión**: Sistema de "Tres Eras" con introducción progresiva
- **Razón**: Evita abrumar a nuevos usuarios, permite maestría gradual

### **4. Dependencia Tecnológica**
- **Decisión**: QR como método principal, NFC como premium opcional
- **Razón**: Máxima accesibilidad económica y técnica

### **5. Recursos del Juego**
- **Decisión**: Recurso universal base + bonus opcionales por ubicación
- **Razón**: Garantiza progresión independiente de factores externos

## **🔮 Potencial de Mercado**

### **Audiencia Objetivo**
- **Primaria**: Familias con niños 6-12 años en áreas urbanas/suburbanas
- **Secundaria**: Colegios interesados en gamificación del ejercicio
- **Terciaria**: Comunidades enfocadas en sostenibilidad y vida saludable

### **Diferenciación Competitiva**
- **Único en su categoría**: No existen juegos que gamifiquen específicamente el trayecto escolar
- **Barrera de entrada**: El sistema de mapeo colaborativo es difícil de replicar
- **Network effects**: Cada familia que usa el juego mejora la experiencia de su comunidad local

### **Modelo de Monetización Potential**
- **Freemium**: Funcionalidades básicas gratuitas, características premium de pago
- **Expansiones**: Nuevos sets de cartas, temas estacionales
- **Merchandising**: Cartas físicas premium, accesorios del juego
- **Licencias**: Adaptaciones para otros mercados/idiomas

## **⚖️ Riesgos y Mitigaciones**

### **Riesgos Técnicos**
- **Precisión GPS**: Mitigado con marcado manual de precisión
- **Duración de batería**: Optimización del tracking en segundo plano
- **Compatibilidad dispositivos**: Enfoque en tecnologías estándar (QR)

### **Riesgos de Adoption**
- **Curva de aprendizaje**: Mitigado con sistema de Tres Eras
- **Coordinación familiar**: Herramientas de gestión y roles rotativos
- **Fatiga del concepto**: Contenido estacional y eventos especiales

### **Riesgos de Negocio**
- **Competencia**: Primera ventaja del movimiento, patent potencial del sistema
- **Estacionalidad**: Adaptación a diferentes condiciones climáticas
- **Escalabilidad**: Arquitectura diseñada para crecimiento modular

**Este resumen contiene toda la información necesaria para que otra LLM comprenda completamente el proyecto "Guardianes de Gaia", su evolución de diseño, y el estado actual del MVP planificado.**