# iBichos - Ecosistema Completo 🐞

Este es el monorepositorio oficial del proyecto **iBichos**. Aquí encontrarás todo el código fuente organizado en sus respectivos módulos.

## 📂 Estructura del Proyecto

El repositorio está dividido en dos partes principales:

### 📱 [1. Aplicación Android (iBichos-app)](./iBichos-app/README.md)
Aplicación móvil nativa desarrollada en Kotlin con Jetpack Compose. Permite a los usuarios registrarse, capturar insectos usando Inteligencia Artificial, ganar experiencia, desbloquear medallas y competir en un ranking global.
- **Tecnologías:** Kotlin, Jetpack Compose, Firebase (Auth, Firestore, Storage), MVVM, Clean Architecture.
- *Entrar a la carpeta `iBichos-app` para ver las instrucciones de instalación.*

### 💻 [2. Panel de Administración Web (ibichos-dashboard)](./ibichos-dashboard/README.md)
Panel de control web para administrar los datos de la aplicación, revisar analíticas de usuarios y ver estadísticas generales de los insectos descubiertos.
- **Demo en producción:** [ibichos.vercel.app](https://ibichos.vercel.app)
- **Tecnologías:** React, TypeScript, Vite.
- *Entrar a la carpeta `ibichos-dashboard` para ver las instrucciones de ejecución local.*

### 📦 [3. APK Lista para Instalar (carpeta /apk)](./apk)
Contiene la versión final compilada e instalable para dispositivos Android (`iBichos.apk`). Esto permite a los evaluadores/profesores instalar y probar el ecosistema iBichos inmediatamente en un dispositivo físico o emulador sin necesidad de compilar el código.

---
