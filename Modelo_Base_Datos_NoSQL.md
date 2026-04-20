# Modelo de Base de Datos NoSQL (Firestore)

El proyecto "iBichos" utiliza Firebase Firestore, una base de datos NoSQL. A diferencia de las bases de datos tradicionales (como MySQL) que usan tablas relacionadas entre sí, Firestore guarda la información en un formato de **Colecciones y Documentos**.

## 1. ¿Por qué NO hay una colección para el "Ranking"?

En nuestra base de datos **no existe una colección separada para el Ranking**. Esto no es un error, sino una decisión técnica correcta para evitar problemas. Las razones son:

* **Evitar Datos Duplicados:** Los puntos de experiencia (XP) le pertenecen al jugador y ya están guardados en su perfil (`users`). Si creáramos una colección extra solo para el ranking, tendríamos el mismo dato guardado en dos lugares distintos. Esto nos obligaría a sumar los puntos en dos partes a la vez cada vez que se atrapa un bicho. Si algo falla (ej. se corta el internet), los datos quedarían descuadrados (alguien podría subir de nivel pero seguir abajo en el ranking). A esto en programación se le llama evitar tener los datos dispersos y mantener "una única fuente de verdad".
* **Firestore es muy veloz ordenando:** Nuestra base de datos está diseñada para buscar y ordenar datos al instante. Para armar el Ranking, el sistema simplemente va a la colección de jugadores (`users`) y le pide: *"Entrégame a los 50 mejores ordenados de mayor a menor según su XP"*. No necesitamos una colección aparte que nos haga el mismo trabajo.

Crear una colección extra solo serviría si tuviéramos millones de jugadores al día y organizar los puntos en vivo pusiera lenta la aplicación, pero para este tipo de proyecto, calcularlo al vuelo a partir de los perfiles es la mejor opción.

---

## 2. Estructura de la Base de Datos

A continuación se detalla cómo se guarda la información. Se divide en dos grandes "carpetas" o colecciones principales.

### Colección: `users`
Guarda el progreso de los jugadores. Cada documento representa a una persona y su ID (el nombre del documento) es el que le asigna Firebase de manera segura cuando se registran.

* **ID del Documento:** `[Código único de Auth]` *(Ej: `AB12cdEF34...`)*
  * `displayName` *(Texto)*: El nombre que eligió el usuario (Ej: "Juan Pérez").
  * `email` *(Texto)*: Su correo de ingreso.
  * `xp` *(Número)*: El total de puntos que ha ganado. Es la clave para calcular su rango y en qué posición del ranking aparece.
  * `level` *(Texto)*: El nombre de su rango actual (Ej: "Casual", "Aprendiz", "Maestro").
  * `avatarUrl` *(Texto)*: El link donde está guardada su foto de perfil.
  * `totalCaptures` *(Número)*: Contador bruto general temporal.
  * `uniqueInsectsCount` *(Número)*: Taxones descubiertos diferentes.
  * `medals` *(Array de Strings)*: **DATO EMBEBIDO.** Lista atómica de preseas desbloqueadas evitando un `JOIN` a otra tabla externa.
  * `medalsEarnedAt` *(Objeto JSON/Map)*: Registro histórico de cuándo se ganó cada medalla `{"Aracnólogo": 1713636551000}`.
  * `levelUpAt` *(Objeto JSON/Map)*: Historial de fechas en las que el usuario subió de rango `{"Explorador": 1713636551000}`.
  * `categoryCounts` *(Objeto JSON/Map)*: **DATO EMBEBIDO.** Archiva conteos directos `{"Arácnidos": 4}` para estadísticas directas sin iterar miles de documentos extras.

### Colección: `captures`
Guarda todas las atrapadas de la comunidad. Cada vez que alguien atrapa un insecto, se crea un documento nuevo y único aquí.

* **ID del Documento:** `[Generado al azar por Firebase]` *(Ej: `Xk8sP9Lz...`)*
  * `userId` *(Texto)*: **Conecta la captura con el jugador**. Corresponde al código único de quien lo atrapó.
  * `imageUrl` *(Texto)*: El link de la foto tomada al bicho.
  * `insectName` *(Texto)*: Nombre del bicho que detectó la cámara.
  * `scientificName` *(Texto)*: Nombre científico.
  * `probability` *(Número)*: Qué tan segura está la IA de ser ese bicho (Ej: 0.98 equivale a un 98% de confianza).
  * `dangerLevel` *(Texto)*: Nivel de peligro (Ej: "Inofensivo", "Peligroso").
  * `latitude` / `longitude` *(Número)*: Coordenadas del mapa en caso de querer mostrar dónde lo atrapó.
  * `capturedAt` *(Número)*: La fecha y hora exacta en la que se atrapó.
  * `xpAwarded` *(Número)*: Los puntos que le dio ese bicho al momento de la captura.

---

## 3. ¿Cómo hacemos las consultas? (Relaciones)

Como esta base de datos no tiene la función "JOIN" de las bases de datos rígidias, la aplicación conecta las cosas lógicamente:

* **Para mostrar la Pokédex de alguien:** La app va a `captures` y pide: *"Tráeme todas las fotos que el **userId** sea el de esta persona"*.
* **Para el Ranking:** La app va a `users` y pide: *"Pásame los primeros 50 usuarios pero **ordénalos por la variable XP** del más grande al más chico"*.
