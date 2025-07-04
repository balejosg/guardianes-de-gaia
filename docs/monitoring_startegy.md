# Estrategia de Monitoreo - Guardianes de Gaia

## Objetivos de Monitoreo

### Para el Negocio
- Validar que el juego aumenta la actividad física
- Medir engagement y retención de usuarios
- Identificar features más y menos usadas
- Optimizar la curva de dificultad

### Para Desarrollo
- Detectar y resolver problemas antes que los usuarios
- Optimizar performance y costos
- Validar decisiones técnicas
- Facilitar debugging en producción

## Métricas de Negocio (KPIs)

### 📊 Métricas de Adopción
```
guardian.signup.total - Registros totales
guardian.signup.daily - Registros por día
guardian.signup.conversion - % que completan onboarding
pacto.created.total - Pactos creados
pacto.size.average - Tamaño promedio de Pacto
```

### 🚶 Métricas de Actividad
```
walking.sessions.daily - Sesiones de caminata por día
walking.distance.average - Distancia promedio por sesión
walking.steps.total.daily - Pasos totales diarios
walking.energy.generated.hourly - Energía generada por hora
walking.magical_points.visited - Puntos mágicos visitados
```

### ⚔️ Métricas de Engagement
```
battle.participation.rate - % de usuarios que batallan
battle.completion.rate - % de batallas completadas
battle.win.rate - Tasa de victoria global
battle.average.duration - Duración promedio
cards.scanned.daily - Cartas escaneadas por día
```

### 📈 Métricas de Retención
```
retention.d1 - Retención día 1
retention.d7 - Retención día 7  
retention.d30 - Retención día 30
session.length.average - Duración promedio de sesión
dau.mau.ratio - Daily Active / Monthly Active Users
```

### 💰 Métricas de Monetización (Futuro)
```
revenue.per.user - Ingreso promedio por usuario
conversion.free.to.paid - Conversión a premium
feature.premium.usage - Uso de features premium
```

## Métricas Técnicas

### 🔧 Performance
```
http.request.duration - Latencia por endpoint
http.request.size - Tamaño de requests
database.query.duration - Tiempo de queries
cache.hit.rate - Efectividad del cache
background.job.duration - Duración de jobs
```

### ⚠️ Errores y Disponibilidad
```
http.request.error.rate - Tasa de error por endpoint
exception.count - Excepciones por tipo
uptime.percentage - Disponibilidad del servicio
crash.free.users - Usuarios sin crashes
```

### 📱 Métricas Mobile
```
app.startup.time - Tiempo de inicio
screen.load.time - Tiempo de carga por pantalla
battery.drain.rate - Consumo de batería
network.request.count - Requests por sesión
offline.capability.usage - Uso en modo offline
```

## Implementación Técnica

### Backend - Micrometer + Prometheus
```java
@Component
public class BusinessMetrics {
    private final MeterRegistry registry;
    
    // Contador simple
    public void recordGuardianCreated(Element element) {
        registry.counter("guardian.created",
            "element", element.name()
        ).increment();
    }
    
    // Gauge para valores actuales
    public void updateActiveUsers(long count) {
        registry.gauge("users.active", count);
    }
    
    // Timer para duraciones
    public void recordBattleDuration(long seconds) {
        registry.timer("battle.duration")
            .record(Duration.ofSeconds(seconds));
    }
    
    // Distribution summary para valores
    public void recordStepsInSession(long steps) {
        registry.summary("walking.steps.per.session")
            .record(steps);
    }
}
```

### Mobile - Firebase Analytics
```dart
class AnalyticsService {
  final FirebaseAnalytics _analytics = FirebaseAnalytics.instance;
  
  // Evento con parámetros
  Future<void> logRouteCompleted({
    required String routeId,
    required int steps,
    required int energyGenerated,
    required Duration duration,
  }) async {
    await _analytics.logEvent(
      name: 'route_completed',
      parameters: {
        'route_id': routeId,
        'steps': steps,
        'energy_generated': energyGenerated,
        'duration_minutes': duration.inMinutes,
        'points_visited': _magicalPointsVisited,
      },
    );
  }
  
  // User properties
  Future<void> updateUserProperties({
    required int guardianLevel,
    required String element,
    required int pactoSize,
  }) async {
    await _analytics.setUserProperty(
      name: 'guardian_level',
      value: guardianLevel.toString(),
    );
    await _analytics.setUserProperty(
      name: 'element',
      value: element,
    );
  }
}
```

## Dashboards

### 📊 Dashboard Ejecutivo (Grafana)
```
┌─────────────────────┬─────────────────────┬─────────────────────┐
│   Usuarios Activos  │  Retención D1/D7/D30│    Pasos Totales    │
│      📈 2,847       │   📊 85% / 72% / 61%│    🚶 1.2M / día    │
├─────────────────────┼─────────────────────┼─────────────────────┤
│  Sesiones por Hora  │   Tasa de Batallas  │  Feature Adoption   │
│   [График почасовой]│      ⚔️ 68%         │  [Toggle metrics]   │
├─────────────────────┴─────────────────────┴─────────────────────┤
│                    Mapa de Calor de Actividad                     │
│                  [Heatmap por día y hora]                         │
└───────────────────────────────────────────────────────────────────┘
```

### 🔧 Dashboard Técnico (Grafana)
```
┌─────────────────────┬─────────────────────┬─────────────────────┐
│    Latencia P95     │    Error Rate       │     CPU / Memory    │
│      📊 248ms       │     🔴 0.12%        │   📊 45% / 2.1GB    │
├─────────────────────┼─────────────────────┼─────────────────────┤
│   Request Rate      │   Cache Hit Rate    │   Active Threads    │
│    📈 847 req/s     │     🎯 94.2%        │      🔧 127         │
└─────────────────────┴─────────────────────┴─────────────────────┘
```

## Alertas

### 🚨 Alertas Críticas (PagerDuty)
- Error rate > 5% por 5 minutos
- Latencia P95 > 1s por 5 minutos  
- Disponibilidad < 99.5%
- Disk space < 10%
- Crash rate > 1%

### ⚠️ Alertas de Negocio (Slack)
- DAU cae > 20% vs promedio
- Retención D1 < 70%
- Sin nuevos usuarios en 1 hora
- Feature toggle sin uso en 7 días
- Tasa de victoria < 40% o > 80%

### 📧 Alertas de Mantenimiento (Email)
- Certificados SSL expiran en 30 días
- Logs ocupan > 80% del espacio
- Queries lentas > 100 en 1 hora
- Background jobs fallando repetidamente

## Herramientas de Monitoreo

### Stack de Monitoreo
```
Aplicación → Micrometer → Prometheus → Grafana
     ↓            ↓            ↓          ↓
   Logs    →   Logstash  → Elastic  → Kibana
     ↓                                    
  Errores  →           Sentry        
     ↓
 Analytics →      Firebase/Mixpanel
```

### Acceso a Dashboards
- **Grafana**: https://monitoring.guardianes.com
- **Kibana**: https://logs.guardianes.com  
- **Sentry**: https://sentry.guardianes.com
- **Togglz**: https://api.guardianes.com/admin/toggles

## Plan de Evolución

### Fase 1 - MVP (Actual)
- [x] Métricas básicas de negocio
- [x] Monitoring de infraestructura
- [x] Alertas críticas
- [x] Dashboard ejecutivo simple

### Fase 2 - Post-MVP
- [ ] Analytics predictivo
- [ ] Alertas inteligentes (ML)
- [ ] Real User Monitoring (RUM)
- [ ] Distributed tracing completo

### Fase 3 - Escala
- [ ] A/B testing framework
- [ ] Análisis de cohortes avanzado
- [ ] Personalización por usuario
- [ ] Optimización automática