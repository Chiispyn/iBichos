# 📊 iBichos Dashboard Web

> [!TIP]
> **Acceso en producción:** Puedes entrar directamente a la versión en vivo del panel en [ibichos.vercel.app](https://ibichos.vercel.app) sin necesidad de correrlo localmente.

Bienvenido al Centro de Comando de iBichos. Este panel de control web (SPA) permite administrar la base de datos de la aplicación móvil de manera visual, gestionar usuarios y moderar los descubrimientos realizados por la Inteligencia Artificial.


## 🚀 Tecnologías y Stack
*   **Frontend:** React 19 + TypeScript + Vite.
*   **Estilos:** Bootstrap 5 (Responsive Design) e íconos de `lucide-react`.
*   **Visualización de Datos:** `recharts` (Gráficos vectoriales e interactivos).
*   **Backend (BaaS):** Firebase (Authentication & Cloud Firestore).
*   **Enrutamiento:** React Router DOM (Manejo de Rutas Privadas).

## 🔑 Acceso y Seguridad (Roles STAFF)
El Dashboard posee una capa de seguridad estricta basada en roles:
1. Ningún usuario de la app móvil puede ingresar por defecto.
2. Para acceder, el identificador único (UID) del usuario debe existir en la colección `admins` de Firestore con el estado `active`.
3. Los administradores actuales pueden elevar privilegios a otros usuarios directamente desde el módulo **Gestión de Usuarios** haciendo clic en "Ascender".
4. Cuenta con un sistema seguro de "Recordar sesión" local y recuperación de contraseña oficial vía Firebase.

## 🌟 Funcionalidades Principales

*   **1. Inicio (Daily Active Usage):** Un resumen dinámico que filtra la base de datos para mostrarte lo que está ocurriendo **hoy**. (Nuevos registros, fotos subidas hoy y alertas).
*   **2. Analítica Avanzada:**
    *   **Autoguardado Histórico:** Mensualmente se realiza un *snapshot* automático de las estadísticas en la colección `historical_reports`.
    *   **Exportación de Datos:** Botón para generar reportes en `.CSV` en un solo clic.
    *   **Embudo de Activación:** Gráfico de conversión (Registro → Primer Avistamiento).
    *   **Gráfico Circular:** Distribución de niveles de peligrosidad (Salud Comunitaria vs Salud Pública).
    *   **Auditoría de Tiempo:** Rastreo de Sesiones Abiertas y Minutos de Retención.
*   **3. Galería de Moderación:** Muestra el historial completo de fotografías. Permite realizar "Borrado Lógico" ocultando las fotos maliciosas y dejando un registro automático de auditoría de qué administrador ejecutó la acción en `moderation_logs`.
*   **4. Sistema de Gestión de Usuarios y Sanciones:**
    *   Filtros avanzados por roles y Ligas de Gamificación (Casual, Amateur, etc.).
    *   Permite nombrar/revocar roles de Administrador.
    *   Botón para aplicar **Shadowban** a usuarios tóxicos con un solo clic.

## ⚙️ Instalación y Uso Local

1. Clona el repositorio e ingresa a la carpeta del dashboard:
   ```bash
   cd ibichos-dashboard
   ```
2. Instala las dependencias:
   ```bash
   npm install
   ```
3. Levanta el servidor de desarrollo:
   ```bash
   npm run dev
   ```
4. Abre tu navegador en `http://localhost:5173`. Ingresa con tu cuenta de correo autorizada como administrador en Firebase.

*(Para despliegues a producción en Vercel o Netlify, utilizar el comando `npm run build` para generar el empaquetado optimizado).*

---

## 🧪 Pruebas Unitarias (Vitest)

El Dashboard incluye una suite de pruebas para verificar las vistas y flujos de negocio (Autenticación, Catálogo de Especies, Filtros de Moderación, Analíticas y Auditoría de Logs).

Para ejecutar las pruebas desde la terminal:
```bash
npm run test
```

* **Reporte HTML Interactivo:** Para ver los resultados en un panel web interactivo, una vez finalizadas las pruebas ejecuta:
  ```bash
  npx vite preview --outDir test-results
  ```
  Y abre `http://localhost:4173` en tu navegador.

Las pruebas se encuentran organizadas en espejo con las carpetas reales de producción en `src/__tests__/pages/` utilizando **Vitest**, **jsdom** (para simulación de DOM en node) y **React Testing Library** para testear el estado reactivo de hooks y componentes.

