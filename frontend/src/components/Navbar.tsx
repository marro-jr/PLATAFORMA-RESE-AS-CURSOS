import React from 'react';
import { usarAutenticacion } from '../context/AuthContext';

interface PropiedadesNavbar {
  vistaActual: string;
  alCambiarVista: (vista: string) => void;
  alAbrirLogin: () => void;
  alAbrirRegistro: () => void;
}

export const Navbar: React.FC<PropiedadesNavbar> = ({
  vistaActual,
  alCambiarVista,
  alAbrirLogin,
  alAbrirRegistro,
}) => {
  const { usuario, estaAutenticado, esModerador, cerrarSesion } = usarAutenticacion();

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark shadow-sm sticky-top">
      <div className="container">
        <a
          href="#cursos"
          onClick={(evento) => {
            evento.preventDefault();
            alCambiarVista('cursos');
          }}
          className="navbar-brand fw-bold text-uppercase letter-spacing-1"
        >
          Reseñas de Cursos
        </a>

        <button
          className="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navegacionPrincipal"
          aria-controls="navegacionPrincipal"
          aria-expanded="false"
          aria-label="Alternar navegación"
        >
          <span className="navbar-toggler-icon"></span>
        </button>

        <div className="collapse navbar-collapse" id="navegacionPrincipal">
          <ul className="navbar-nav me-auto mb-2 mb-lg-0">
            <li className="nav-item">
              <button
                type="button"
                onClick={() => alCambiarVista('cursos')}
                className={`nav-link btn btn-link text-decoration-none ${
                  vistaActual === 'cursos' ? 'active fw-bold text-white' : ''
                }`}
              >
                Catálogo de Cursos
              </button>
            </li>

            {estaAutenticado && !esModerador && (
              <li className="nav-item">
                <button
                  type="button"
                  onClick={() => alCambiarVista('mis-resenas')}
                  className={`nav-link btn btn-link text-decoration-none ${
                    vistaActual === 'mis-resenas' ? 'active fw-bold text-white' : ''
                  }`}
                >
                  Mis Reseñas
                </button>
              </li>
            )}

            {estaAutenticado && esModerador && (
              <li className="nav-item">
                <button
                  type="button"
                  onClick={() => alCambiarVista('moderacion')}
                  className={`nav-link btn btn-link text-decoration-none ${
                    vistaActual === 'moderacion' ? 'active fw-bold text-warning' : 'text-warning'
                  }`}
                >
                  Panel de Moderación
                </button>
              </li>
            )}
          </ul>

          <div className="d-flex align-items-center gap-2">
            {estaAutenticado && usuario ? (
              <div className="d-flex align-items-center gap-3">
                <div className="text-end text-light small d-none d-md-block">
                  <div className="fw-semibold">{usuario.nombre}</div>
                  <span
                    className={`badge ${
                      esModerador ? 'bg-warning text-dark' : 'bg-primary'
                    }`}
                  >
                    {usuario.rol}
                  </span>
                </div>
                <button
                  type="button"
                  onClick={cerrarSesion}
                  className="btn btn-outline-light btn-sm"
                >
                  Cerrar Sesión
                </button>
              </div>
            ) : (
              <div className="d-flex gap-2">
                <button
                  type="button"
                  onClick={alAbrirLogin}
                  className="btn btn-outline-light btn-sm"
                >
                  Iniciar Sesión
                </button>
                <button
                  type="button"
                  onClick={alAbrirRegistro}
                  className="btn btn-primary btn-sm"
                >
                  Registrarse
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
};
