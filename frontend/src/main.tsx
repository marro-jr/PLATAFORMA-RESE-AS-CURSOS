import React from 'react';
import ReactDOM from 'react-dom/client';
import { App } from './App';
import { ProveedorAutenticacion } from './context/AuthContext';
import 'bootstrap/dist/css/bootstrap.min.css';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ProveedorAutenticacion>
      <App />
    </ProveedorAutenticacion>
  </React.StrictMode>
);
