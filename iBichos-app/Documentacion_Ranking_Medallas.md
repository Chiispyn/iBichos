# Sistema de Ranking y Medallas en iBichos

Este documento explica cómo funciona el sistema de clasificación (Ranking) implementado en la aplicación, así como la lógica para otorgar experiencia (XP), niveles y medallas a los jugadores.

## 1. Obtención de Puntos de Experiencia (XP) y Niveles

El progreso principal en iBichos se mide a través de **Puntos de Experiencia (XP)**. Cada vez que el usuario realiza una acción positiva en la app (como atrapar e identificar correctamente a un insecto), su XP se incrementa en Firebase Firestore.
- Si atrapar un insecto es un **descubrimiento nuevo**, el usuario recibe **+150 XP**.
- Si es una especie **ya antes capturada**, recibe **+20 XP**.

**Sistema de Niveles Automático:** A medida que el jugador cruza ciertos umbrales de XP, su "Rango" o "Nivel" se actualiza automáticamente:
- Más de **1000 XP**: *"Maestro"*
- Más de **500 XP**: *"Experto"*
- Más de **200 XP**: *"Explorador"*
- Más de **50 XP**: *"Aprendiz"*
- Menos de **50 XP**: *"Casual"*

## 2. Pantalla de Ranking (Leaderboard)

Para fomentar la competencia sana, la barra de navegación inferior de la app cuenta con un ícono de **Trofeo**, el cual abre la pantalla `RankingScreen`. 

### Backend (Firestore)
La aplicación lee directamente la colección de usuarios en Firebase y utiliza un query estructurado para traer a los **Top 50** ordenados por la mayor cantidad de Puntos de Experiencia (XP).

## 3. Lógica de las Medallas Visuales del Ranking (UI)

Dentro del listado, los **Tres Primeros Lugares** reciben un diseño visual especial:
*   🥇 **1er Lugar (Oro):** Trofeo Dorado (`#FFD700`) sobre fondo de tarjeta dorado oscuro.
*   🥈 **2do Lugar (Plata):** Trofeo Plateado (`#C0C0C0`) sobre fondo de tarjeta gris plomizo.
*   🥉 **3er Lugar (Bronce):** Trofeo Bronce (`#CD7F32`) sobre fondo de tarjeta marrón cobrizo.

Del cuarto puesto en adelante, solo se visualiza e indica el número de posición actual (`#4`, `#5`, etc.).

---

## 4. Obtención de Logros Especiales y Coleccionismo

Más allá de los puntos de experiencia en el Ranking, `CameraViewModel` de iBichos analiza taxonómica y ecológicamente a los insectos atrapados. Dependiendo de las categorías, se van desbloqueando de forma automática las siguientes **Medallas/Logros Especiales** que se agrupan en el Perfil del usuario:

### Logros por Cantidad de Descubrimientos
*   **"Primer Avistamiento"**: Otorgado al registrar de forma exitosa el primer insecto único validado por IA.
*   **"Investigador Novato"**: Recompensa obtenida al descubrir **5 especies únicas** de insectos.

### Logros de Riesgo
*   **"Cazador Valiente"**: Desbloqueado automáticamente si la IA determina que el espécimen capturado cuenta con nivel de peligrosidad ("Venenoso" o "Peligroso").

### Logros de Entomología Especializada (Por Familias/Clases)
Dependiendo del tipo de animal que la cámara clasifique, se subirán contadores ocultos que, al llenarse, te especializan como un jugador con "Experiencia de Campo" y te dan un título de experto:
*   🕸️ **"Aracnólogo"**: Entregado al capturar e identificar exitosamente un mínimo de **5 Arañas**.
*   🦋 **"Lepidopterólogo"**: Entregado al acumular un mínimo de **5 Mariposas o Polillas**.
*   🐝 **"Amigo de Polinizadores"**: Otorgado en reconocimiento ecológico al atrapar y censar **10 Abejas o Avispas**.
*   🪲 **"Coleopterólogo"**: Un nivel élite concedido únicamente a aquellos que atrapan **15 Escarabajos**.
*   🦟 **"Control de Plagas"**: Entregado a los usuarios responsables de detectar e identificar a **10 insectos categorizados como plaga**.

Todas estas medallas y clasificaciones hacen que coleccionar no sea solamente tomar fotos, ¡Sino llenar el Pokédex y el currículo taxonómico de tu perfil con logros únicos!
