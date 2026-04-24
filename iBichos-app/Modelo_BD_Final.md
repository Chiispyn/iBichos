# Esquema de Base de Datos Firestore - iBichos (Actualizado)

El modelo de datos NoSQL de iBichos está diseñado para optimizar lecturas (optimizadas para rankings y dashboards) y delegar la lógica de gamificación al backend, reduciendo los costos operativos de la nube.

---

## 1. Colección `users`
Almacena el perfil del usuario, métricas base y su progreso de gamificación (denormalizado y anidado).

**Ruta:** `users/{uid}` (El ID del documento coincide con el UID de Firebase Auth)

| Campo | Tipo | Obligatorio | Origen | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| **`uid`** | **String** | **Sí** | **Firebase Auth** | **[ID DEL DOCUMENTO] Identificador único del usuario.** |
| `displayName` | String | Sí | Input Usuario | Nombre o alias del cazador. |
| `email` | String | Sí | Firebase Auth | Correo electrónico registrado. |
| `region` | String | Sí | Input Usuario | Región de Chile (ej: "Biobío"). |
| `city` | String | Sí | Input Usuario | Comuna de residencia (ej: "Lota"). |
| `birthDate` | String | Sí | Input Usuario | Fecha de nacimiento en formato `dd/MM/yyyy`. |
| `gender` | String | Sí | Input Usuario | Enum: `MALE`, `FEMALE`, `OTHER`, `PREFER_NOT_TO_SAY`, `UNSPECIFIED`. |
| `avatarUrl` | String | No | Input Usuario | URL de la imagen de perfil subida a Cloudinary. `null` por defecto. |
| `totalCaptures` | Number | Automático | Backend | Cantidad total de capturas realizadas (histórico). |
| `createdAt` | Timestamp| Automático | Backend | Fecha y hora exacta del registro en la app. |
| `strikes` | Number | Automático | Backend/Admin | Faltas cometidas por moderación. Inicia en 0. |
| `isShadowBanned`| Boolean | Automático | Backend/Admin | Si es `true`, el usuario no aparece en rankings ni feeds. |
| `xp` | Number | Automático | Backend | **[Denormalizado]** Copia de la XP en la raíz para indexación de Firebase. |
| `uniqueInsectsCount`| Number | Automático | Backend | **[Denormalizado]** Cantidad de especies para indexación de Firebase. |
| `medalsCount` | Number | Automático | Backend | **[Denormalizado]** Cantidad de medallas para indexación de Firebase. |

### Sub-objeto: `gamification` (Map)
Agrupa todo el estado del motor de gamificación. 
> **Nota de Arquitectura NoSQL:** Este no es un documento separado, es un objeto de tipo *Map* (diccionario) que vive **dentro** del documento del usuario. Por lo tanto, no requiere un `uid` ni llave foránea, ya que inherentemente le pertenece al usuario dueño de ese documento `users/{uid}`.

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `xp` | Number | Puntos de experiencia actuales reales (Motor central). |
| `level` | String | Enum: `CASUAL`, `AMATEUR`, `EXPLORER`, `ENTOMOLOGIST`, `BUG_MASTER`. |
| `uniqueInsectsCount` | Number | Especies únicas descubiertas. |
| `categoryCounts` | Map | Diccionario `<CategoríaTaxonómica, Cantidad>` (ej: `{"HYMENOPTERA": 1}`). |
| `medals` | Array[String]| Lista de IDs de logros desbloqueados (ej: `["FIRST_CAPTURE"]`). |
| `medalsEarnedAt` | Map | Diccionario de timestamps por medalla (ej: `{"FIRST_CAPTURE": 1776973760726}`). |
| `levelUpAt` | Map | Diccionario de timestamps por nivel alcanzado (ej: `{"AMATEUR": 1776973759399}`). |

---

## 2. Colección `captures`
Registro de cada insecto identificado mediante la cámara y validado por la Inteligencia Artificial.

**Ruta:** `captures/{auto-id}`

| Campo | Tipo | Obligatorio | Origen | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| **`captureId`** | **String** | **Sí** | **Firestore** | **[ID DEL DOCUMENTO] Identificador único de la captura.** |
| **`userId`** | **String** | **Sí** | **Backend** | **[LLAVE FORÁNEA] Relaciona la captura con un `users/{uid}`.** |
| `imageUrl` | String | Sí | Cloudinary | URL pública de la imagen procesada en la nube. |
| `insectName` | String | Sí | Kindwise IA | Nombre común del insecto (si está disponible, o científico). |
| `scientificName` | String | Sí | Kindwise IA | Nombre taxonómico. Usado fuertemente para lógica de "especies únicas". |
| `category` | String | Sí | Backend | Enum Taxonómico (ej: `HYMENOPTERA`, `ARACHNIDA`). |
| `dangerLevel` | String | Sí | Backend/IA | Enum: `HARMLESS`, `CAUTION`, `VENOMOUS`, `UNKNOWN`. |
| `probability` | Number | Sí | Kindwise IA | Grado de confianza de la IA en su clasificación (0.0 a 1.0). |
| `latitude` | Number | No | Sensor GPS | Latitud de la captura. |
| `longitude` | Number | No | Sensor GPS | Longitud de la captura. |
| `timestamp` | Timestamp| Automático | Backend | Fecha y hora exacta del descubrimiento. |
| `xpAwarded` | Number | Automático | Backend | Puntos otorgados por esta captura específica a la cuenta del usuario. |
| `description` | String | Sí | Kindwise IA | Resumen enciclopédico de la especie biológica. |
| `needsReview` | Boolean | Automático | Backend | `true` si la IA tiene baja confianza. Default: `false`. |
| `validationStatus`| String | Automático | Backend | Enum: `APPROVED`, `REJECTED`, `PENDING_REVIEW`. |

---

## 3. Colección `events` (Bitácora)
Bitácora inmutable de eventos. Construida usando un patrón _Append-Only_. Usada exclusivamente por el Dashboard para métricas analíticas y machine learning.

**Ruta:** `events/{auto-id}`

| Campo | Tipo | Obligatorio | Descripción |
| :--- | :--- | :---: | :--- |
| **`eventId`** | **String** | **Sí** | **[ID DEL DOCUMENTO] Identificador único del evento.** |
| **`userId`** | **String** | **Sí** | **[LLAVE FORÁNEA] UID del usuario que detonó el evento.** |
| `eventType` | String | Sí | `USER_REGISTERED`, `LEVEL_UP`, `MEDAL_UNLOCKED`, `SPECIES_DISCOVERED`. |
| `occurredAt` | Timestamp| Sí | Momento exacto en el que ocurrió la acción. |
| `xpAtEvent` | Number | No | XP del usuario en el momento exacto de la acción (Contexto histórico). |
| `newLevel` / `previousLevel` | String | Condicional | [Solo LEVEL_UP] Niveles involucrados en la promoción de rango. |
| `medalId` | String | Condicional | [Solo MEDAL_UNLOCKED] ID del logro obtenido. |
| `insectName` / `scientificName` / `category` | String | Condicional | [Solo SPECIES_DISCOVERED] Datos taxonómicos de la especie avistada. |

---

## 4. Colección `sessions` (Retención de Usuario)
Registro de retención y "Screen Time" para analíticas de uso, gestionado por AuthStateListener en el flujo principal.

**Ruta:** `sessions/{sessionId}`

| Campo | Tipo | Obligatorio | Descripción |
| :--- | :--- | :---: | :--- |
| **`sessionId`**| **String** | **Sí** | **[ID DEL DOCUMENTO] Identificador único de la sesión.** |
| **`userId`** | **String** | **Sí** | **[LLAVE FORÁNEA] UID del usuario activo en la sesión.** |
| `startedAt` | Timestamp| Sí | Timestamp de cuando la app pasó a *Foreground* o el usuario inició sesión. |
| `endedAt` | Timestamp| No | Timestamp de cuando la app pasó a *Background* o se deslogueó. `null` si sigue activa. |
| `durationMinutes` | Number | Sí | Duración total de la sesión calculada en minutos. Inicia en `0`. |
| `deviceOS` | String | Sí | Plataforma cliente (Por defecto `"Android"`). |

---

## 5. Colección `admins`
Gestiona los accesos al Dashboard Web de iBichos. Solo los usuarios listados aquí pueden entrar al panel de administración para moderar el contenido.

**Ruta:** `admins/{uid}`

| Campo | Tipo | Obligatorio | Descripción |
| :--- | :--- | :---: | :--- |
| **`uid`** | **String** | **Sí** | **[ID DEL DOCUMENTO] Identificador único del administrador (UID de Firebase Auth).** |
| `email` | String | Sí | Correo electrónico del administrador. |
| `estado` | String | Sí | Estado del acceso (Ej: `"activo"`). |
| `rol` | String | Sí | Nivel de permisos (Ej: `"admin"`). |

