# 📊 iBichos Dashboard Web

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
2. Para acceder, el identificador único (UID) del usuario debe existir en la colección `admins` de Firestore con el estado `activo`.
3. Los administradores actuales pueden elevar privilegios a otros usuarios directamente desde el módulo **Gestión de Usuarios** haciendo clic en "Hacer Admin".

## 🌟 Funcionalidades Principales

*   **1. Inicio (Daily Active Usage):** Un resumen dinámico que filtra la base de datos para mostrarte lo que está ocurriendo **hoy**. (Nuevos registros, fotos subidas hoy y alertas).
*   **2. Analítica Avanzada:**
    *   Tarjetas KPI de totales acumulados.
    *   **Gráfico de Barras:** Usuarios divididos por su Liga de Gamificación (Casual, Explorador, etc.).
    *   **Gráfico Circular:** Distribución de niveles de peligrosidad (Inofensivo, Venenoso).
    *   **Línea de Tiempo:** Gráfico de "Retención Diaria" que suma los minutos jugados en la app (Screen Time) por fecha.
*   **3. Galería de Moderación:** Muestra el historial completo de fotografías subidas. Filtra por estado `PENDING` para que el equipo pueda "Aprobar" o "Rechazar" manualmente identificaciones de la IA.
*   **4. Sistema de RRHH Automático:** Identifica visualmente (Insignia STAFF) a los trabajadores dentro de la tabla de usuarios y permite contratar/despedir moderadores en tiempo real.

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
