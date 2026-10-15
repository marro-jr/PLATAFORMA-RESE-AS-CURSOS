import React, { useState } from 'react';
import { autenticarUsuario } from '../services/api';
import { usarAutenticacion } from '../context/AuthContext';

interface PropiedadesModalLogin {
  visible: boolean;
  alCerrar: () => void;
  alIrARegistro: () => void;
  alLoginExitoso?: () => void;
}

export const ModalLogin: React.FC<PropiedadesModalLogin> = ({
  visible,
  alCerrar,
  alIrARegistro,
  alLoginExitoso,
}) => {
  const { iniciarSesion } = usarAutenticacion();
  const [correo, setCorreo] = useState<string>('');
  const [clave, setClave] = useState<string>('');
  const [cargando, setCargando] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  if (!visible) return null;

  const manejarEnvio = async (evento: React.FormEvent) => {
    evento.preventDefault();
    if (!correo.trim() || !clave.trim()) {
      setError('Por favor completa todos los campos.');
      return;
    }

    setCargando(true);
    setError(null);

    try {
      const respuesta = await autenticarUsuario({
        correo: correo.trim(),
        clave: clave.trim(),
      });
      iniciarSesion(respuesta);
      if (alLoginExitoso) alLoginExitoso();
      alCerrar();
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('Error al autenticar credenciales.');
      }
    } finally {
      setCargando(false);
    }
  };

  const rellenarCredencialesDemo = (correoDemo: string) => {
    setCorreo(correoDemo);
    setClave('Password123!');
    setError(null);
  };

  return (
    <div
      className="modal fade show d-block"
      tabIndex={-1}
      style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}
    >
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content border-0 shadow">
          <div className="modal-header">
            <h5 className="modal-title fw-bold">Iniciar Sesión</h5>
            <button
              type="button"
              className="btn-close"
              onClick={alCerrar}
              disabled={cargando}
              aria-label="Cerrar"
            ></button>
          </div>

          <form onSubmit={manejarEnvio}>
            <div className="modal-body">
              {error && (
                <div className="alert alert-danger py-2 small" role="alert">
                  {error}
                </div>
              )}

              {/* Botones de Acceso Rápido para Demostración */}
              <div className="bg-light p-3 rounded-2 border mb-3">
                <div className="small fw-bold text-secondary mb-2">
                  Cuentas de prueba preconfiguradas:
                </div>
                <div className="d-flex flex-wrap gap-2">
                  <button
                    type="button"
                    onClick={() => rellenarCredencialesDemo('juan.perez@correo.com')}
                    className="btn btn-outline-primary btn-sm"
                  >
                    Usuario (USER)
                  </button>
                  <button
                    type="button"
                    onClick={() => rellenarCredencialesDemo('moderador@correo.com')}
                    className="btn btn-outline-warning text-dark btn-sm"
                  >
                    Moderador (MODERATOR)
                  </button>
                </div>
              </div>

              <div className="mb-3">
                <label htmlFor="loginCorreo" className="form-label fw-semibold small">
                  Correo Electrónico
                </label>
                <input
                  id="loginCorreo"
                  type="email"
                  className="form-control"
                  placeholder="ejemplo@correo.com"
                  value={correo}
                  onChange={(e) => setCorreo(e.target.value)}
                  required
                />
              </div>

              <div className="mb-3">
                <label htmlFor="loginClave" className="form-label fw-semibold small">
                  Contraseña
                </label>
                <input
                  id="loginClave"
                  type="password"
                  className="form-control"
                  placeholder="Tu contraseña"
                  value={clave}
                  onChange={(e) => setClave(e.target.value)}
                  required
                />
              </div>

              <div className="text-center small text-muted">
                ¿No tienes una cuenta aún?{' '}
                <button
                  type="button"
                  onClick={() => {
                    alCerrar();
                    alIrARegistro();
                  }}
                  className="btn btn-link p-0 small text-decoration-none"
                >
                  Regístrate aquí
                </button>
              </div>
            </div>

            <div className="modal-footer">
              <button
                type="button"
                className="btn btn-outline-secondary btn-sm"
                onClick={alCerrar}
                disabled={cargando}
              >
                Cancelar
              </button>
              <button
                type="submit"
                className="btn btn-primary btn-sm px-4"
                disabled={cargando}
              >
                {cargando ? 'Verificando...' : 'Entrar'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};
