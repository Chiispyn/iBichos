# Sistema de Gamificación y Ligas Competitivas en iBichos

Este documento explica cómo funciona el motor central de gamificación implementado en la aplicación, así como la arquitectura técnica de puntos de experiencia (XP), niveles (Ligas) y la obtención de medallas (Logros). Todo esto está ahora centralizado y configurado en el archivo maestro de configuración técnica `GamificationConfig.kt` bajo una Clean Architecture.

---

## 1. Obtención de Puntos de Experiencia (XP)

El progreso principal en iBichos se mide a través de **Puntos de Experiencia (XP)**. Cada vez que el usuario sube una foto a través de la interfaz de la cámara y la inteligencia artificial (Kindwise) retorna una probabilidad de certeza mayor al 40%, se otorgan puntos a la colección del usuario en Firestore.

*   **Descubrimiento Inédito (Nueva Especie):** Si es la primera vez que la base de datos registra ese nombre científico asociado a ese usuario, recibe una bonificación de **+150 XP**.
*   **Captura Repetida:** Si el espécimen corresponde a una especie ya registrada en el historial del usuario, recibe un incentivo base de **+20 XP**.

*Nota: Cualquier captura en revisión o exitosa suma XP, pero si el usuario está bajo una sanción por moderación (Shadowbanned), las operaciones de escritura de XP se anulan silenciosamente en el backend (`UserRepositoryImpl`).*

---

## 2. Ligas Competitivas y Sistema de Niveles

A medida que el jugador acumula Puntos de Experiencia en su documento de Firestore (`gamification.xp`), el backend le actualiza automáticamente su Liga/Nivel basado en los siguientes umbrales parametrizables:

| Rango | Nivel en Pantalla | XP Requerida |
| :--- | :--- | :--- |
| `CASUAL` | 🟢 Casual | 0 XP a 99 XP |
| `AMATEUR` | 🔵 Aficionado (Amateur) | 100 XP a 499 XP |
| `EXPLORER` | 🟡 Explorador | 500 XP a 1499 XP |
| `ENTOMOLOGIST`| 🟠 Entomólogo | 1500 XP a 4999 XP |
| `BUG_MASTER` | 🔴 Maestro de Bichos | 5000 XP o más |

---

## 3. Pantalla de Pizarras de Prestigio (Ranking)

La competencia entre usuarios no se limita a la XP. La aplicación permite ordenar a los usuarios (`RankingScreen`) según tres vectores de competencia extraídos dinámicamente usando queries en Firebase Firestore:

1.  ⚔️ **Sabios (XP):** Ordenados por la mayor suma de puntos de experiencia acumulados.
2.  🦟 **Especies Únicas:** Ordenados según el conteo real de biodiversidad descubierta (`gamification.uniqueInsectsCount`).
3.  🏅 **Coleccionistas (Medallas):** Ordenados por el total de logros biológicos desbloqueados.

La interfaz visual asocia dinámicamente la foto de perfil en la nube (Cloudinary) de cada usuario en el top global. A los 3 primeros lugares, el sistema los resalta con un **estilo metálico (Oro, Plata, Bronce)** para denotar su podio global.

---

## 4. Logros Biológicos (Medallas)

El sistema de medallas (`gamification.medals`) se evalúa tras cada captura usando el módulo `GamificationConfig.evaluateMedalsToUnlock()`. Al desbloquear un logro, se mapea mediante el Enum `MedalInfo` a una UI interactiva que muestra las descripciones y el estado del logro dentro del Perfil del explorador.

### 🥉 Progreso General
*   **Primer Avistamiento (`FIRST_CAPTURE`)**: Otorgado al registrar de forma exitosa el primer insecto único validado por IA.
*   **Investigador Novato (`NOVICE_RESEARCHER`)**: Recompensa obtenida al descubrir **5 especies únicas** de insectos.

### 💀 Riesgo y Sobrevivencia
*   **Cazador Valiente (`BRAVE_HUNTER`)**: Desbloqueado si la IA o el entomólogo determinan que el espécimen cuenta con nivel de peligrosidad (`VENOMOUS` o similar).

### 🐛 Entomología Especializada (Por Familias/Clases)
Dependiendo del tipo de animal que el motor infiera taxonómicamente, se subirán contadores de categorías específicos (`categoryCounts` en Firestore). Al llenarse ciertos umbrales, el usuario se vuelve experto:
*   🕸️ **Aracnólogo (`ARACHNOLOGIST`)**: Entregado al acumular un mínimo de **10 especies diferentes de arácnidos**.
*   🦋 **Lepidopterólogo (`LEPIDOPTEROLOGIST`)**: Entregado al atrapar al menos **10 especies diferentes de mariposas o polillas**.
*   🐝 **Amigo Polinizador (`POLLINATOR_FRIEND`)**: Otorgado en reconocimiento al censar al menos **10 abejas, avispas u hormigas**.
*   🪲 **Coleopterólogo (`COLEOPTEROLOGIST`)**: Un nivel alto concedido únicamente a aquellos que logran atrapar **15 escarabajos diferentes**.

### 5. Trazabilidad Histórica y Big Data
Todos los eventos significativos asociados a la gamificación (Subidas de nivel, Medallas desbloqueadas o Especies descubiertas) no solo mutan el documento del usuario, sino que despachan un evento inmutable tipo _Append-Only_ a la colección paralela `events` mediante el componente `EventRepositoryImpl`. Esto garantiza que los cambios de nivel o progreso estén fehados para visualizaciones en dashboards externos o propósitos de moderación.
