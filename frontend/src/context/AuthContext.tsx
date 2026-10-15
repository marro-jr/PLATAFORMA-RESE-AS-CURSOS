import React, { createContext, useContext, useState, ReactNode } from 'react';
import { LoginRespuesta } from '../types';

interface ContextoAutenticacionTipo {
  token: string | null;
  usuario: LoginRespuesta | null;
  estaAutenticado: boolean;
  esModerador: boolean;
  iniciarSesion: (datos: LoginRespuesta) => void;
  cerrarSesion: () => void;
}

const ContextoAutenticacion = createContext<ContextoAutenticacionTipo | undefined>(undefined);

export const ProveedorAutenticacion: React.FC<{ children: ReactNode }> = ({ children }) => {
  // Siguiendo los requerimientos de seguridad para la demo: token en memoria
  const [token, setToken] = useState<string | null>(null);
  const [usuario, setUsuario] = useState<LoginRespuesta | null>(null);

  const iniciarSesion = (datos: LoginRespuesta) => {
    setToken(datos.token);
    setUsuario(datos);
  };

  const cerrarSesion = () => {
    setToken(null);
    setUsuario(null);
  };

  const estaAutenticado = token !== null && usuario !== null;
  const esModerador = usuario?.rol === 'MODERATOR';

  return (
    <ContextoAutenticacion.Provider
      value={{
        token,
        usuario,
        estaAutenticado,
        esModerador,
        iniciarSesion,
        cerrarSesion,
      }}
    >
      {children}
    </ContextoAutenticacion.Provider>
  );
};

export const usarAutenticacion = (): ContextoAutenticacionTipo => {
  const contexto = useContext(ContextoAutenticacion);
  if (!contexto) {
    throw new Error('usarAutenticacion debe usarse dentro de un ProveedorAutenticacion');
  }
  return contexto;
};
