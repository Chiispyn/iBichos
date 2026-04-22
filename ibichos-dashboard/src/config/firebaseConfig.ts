import { initializeApp } from "firebase/app";
import { getFirestore } from "firebase/firestore";
import { getAuth } from "firebase/auth";
import { getAnalytics } from "firebase/analytics";

// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries

// Your web app's Firebase configuration
// For Firebase JS SDK v7.20.0 and later, measurementId is optional

const firebaseConfig = {
  apiKey: "AIzaSyDZHVYgDrTQ30eyVja_4mLiBc7q_i4nURk",
  authDomain: "ibichos.firebaseapp.com",
  projectId: "ibichos",
  storageBucket: "ibichos.firebasestorage.app",
  messagingSenderId: "996905357020",
  appId: "1:996905357020:web:9fe7500cc34073fd03ee19",
  measurementId: "G-KZM9LSBP2W"
};

// Inicializamos Firebase
const app = initializeApp(firebaseConfig);
// Exportamos la base de datos para usarla en otros archivos
export const auth = getAuth(app);
export const db = getFirestore(app);

