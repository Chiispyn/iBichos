<div align="center">
  <img src="app/src/main/res/drawable/logo_oficial.png" width="150" alt="iBichos Logo">
  <h1>🦟 iBichos</h1>
  <p><strong>Caza, Colecciona y Explora tu Ecosistema</strong></p>
</div>

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Firebase](https://img.shields.io/badge/firebase-%23039BE5.svg?style=for-the-badge&logo=firebase)

**iBichos** es una aplicación móvil nativa para Android. Fusiona la educación ambiental con dinámicas de gamificación, utilizando Inteligencia Artificial para identificar insectos a través de la cámara del teléfono celular.

---

## 🎯 Características Principales

1. **Reconocimiento con IA (Kindwise API):** Analiza la foto para determinar la especie del insecto y su "Nivel de Peligro" (Inofensivo, Venenoso, Plaga).
2. **Pokedex Personalizada:** Inventario en la nube donde el usuario guarda todas sus capturas ordenadas cronológicamente con su información biológica.
3. **Mapa del Ecosistema:** Mapeo geolocalizado con pines exactos donde se realizaron las capturas.
4. **Ranking y Medallas:** Sistema de experiencia (XP) con condecoraciones taxonómicas y un Top 50 global en tiempo real.

## 🛠 Arquitectura

El proyecto está construido bajo estándares modernos de desarrollo Android:
- **Clean Architecture & MVVM.**
- **UI:** 100% desarrollada en **Jetpack Compose**.
- **Backend:** Firebase (Auth, Firestore). Optimizado guardando las imágenes localmente para no incurrir en gastos de servidor (Storage).

## ⚙️ Correr el Proyecto

Para compilar y correr localmente:
1. Abrir el proyecto en **Android Studio**.
2. Agregar el archivo de credenciales `google-services.json` dentro de la carpeta `/app`.
3. *(Opcional)* Configurar tu llave de la API *Kindwise* en el código para obtener análisis reales, de lo contrario la app funcionará en modo de prueba local.
4. Ejecutar en un emulador o dispositivo físico.
