<div align="center">
  <img src="app/src/main/res/drawable/logo_oficial.png" width="150" alt="iBichos Logo">
  <h1>🦟 iBichos</h1>
  <p><strong>Caza, Colecciona y Explora tu Ecosistema</strong></p>
</div>

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Firebase](https://img.shields.io/badge/firebase-%23039BE5.svg?style=for-the-badge&logo=firebase)

**iBichos** es una aplicación móvil nativa para Android. Fusiona el registro y catalogación de insectos con dinámicas de gamificación, utilizando Inteligencia Artificial para identificar especies a través de la cámara del teléfono celular.

---

## 🎯 Características Principales

1. **Reconocimiento con IA (Kindwise API):** Analiza la foto para determinar la especie del insecto y su "Nivel de Peligro" (Inofensivo, Venenoso, Plaga).
2. **Colección Personalizada:** Inventario en la nube donde el usuario guarda todas sus capturas ordenadas cronológicamente con su información biológica.
3. **Mapa del Ecosistema:** Mapeo geolocalizado con pines exactos donde se realizaron las capturas.
4. **Ranking y Medallas:** Sistema de experiencia (XP) con condecoraciones taxonómicas y un Top 50 global en tiempo real.

## 🛠 Arquitectura

El proyecto está construido bajo estándares modernos de desarrollo Android:
- **Clean Architecture & MVVM.**
- **UI:** 100% desarrollada en **Jetpack Compose**.
- **Backend:** Firebase (Auth, Firestore) para base de datos y **Cloudinary** para el almacenamiento gratuito en la nube de avatares y fotografías de insectos.

## ⚙️ Instalación y Uso (Evaluadores)

Para evaluar y probar el proyecto, **no es necesario** configurar credenciales ni compilar el código fuente, ya que la aplicación se entrega preconfigurada.

1. Descarga el archivo binario **`app-debug.apk`** directamente desde [GitHub Releases](https://github.com/Chiispyn/iBichos/releases/tag/1.0.0).
2. Transfiere el APK a tu dispositivo móvil físico o arrástralo a un Emulador de Android.
3. Permite la instalación desde orígenes desconocidos si el teléfono lo solicita.
4. La aplicación ya contiene las llaves de acceso a **Firebase** y a la **API de Inteligencia Artificial (Kindwise)** integradas en la compilación, por lo que está lista para funcionar inmediatamente.

---

## 🧪 Pruebas Unitarias

La aplicación móvil cuenta con pruebas unitarias para validar las reglas de negocio (XP, asignación de medallas, ordenamiento del ranking y validaciones matemáticas de coordenadas en mapas) de manera local.

Para ejecutar las pruebas desde la terminal:
```bash
./gradlew test
```
O en Windows (Command Prompt / PowerShell):
```powershell
.\gradlew.bat test
```

Las pruebas se ubican en `app/src/test/java/com/cetecom/ibichos/presentation/` y utilizan **JUnit 4**, **MockK** (para interceptar el SDK de Firebase en memoria) y **Robolectric** (para simular componentes del framework de Android sin emuladores).

