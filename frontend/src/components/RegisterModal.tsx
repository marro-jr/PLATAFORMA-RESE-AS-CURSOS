import React, { useState } from 'react';
import { registrarUsuario, autenticarUsuario } from '../services/api';
import { usarAutenticacion } from '../context/AuthContext';

interface PropiedadesModalRegistro {
  visible: boolean;
  alCerrar: () => void;
  alIrALogin: () => void;
}

export const ModalRegistro: React.FC<PropiedadesModalRegistro> = ({
  visible,
  alCerrar,
  alIrALogin,
}) => {
  const { iniciarSesion } = usarAutenticacion();
  const [nombre, setNombre] = useState<string>('');
  const [correo, setCorreo] = useState<string>('');
  const [clave, setClave] = useState<string>('');
  const [cargando, setCargando] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  if (!visible) return null;

  const manejarEnvio = async (evento: React.FormEvent) => {
    evento.preventDefault();
    if (!nombre.trim() || !correo.trim() || !clave.trim()) {
      setError('Por favor completa todos los campos obligatorios.');
      return;
    }

    if (clave.length < 6) {
      setError('La contraseña debe tener un mínimo de 6 caracteres.');
      return;
    }

    setCargando(true);
    setError(null);

    try {
      await registrarUsuario({
        nombre: nombre.trim(),
        correo: correo.trim(),
        clave: clave.trim(),
      });

      // Inicio de sesión automático tras registro exitoso
      const sesion = await autenticarUsuario({
        correo: correo.trim(),
        clave: clave.trim(),
      });

      iniciarSesion(sesion);
      alCerrar();
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('Error al registrar usuario.');
      }
    } finally {
      setCargando(false);
    }
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
            <h5 className="modal-title fw-bold">Crear Cuenta de Estudiante</h5>
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

              <div className="alert alert-primary py-2 small mb-3">
                El registro público siempre asigna el rol <strong>USER</strong> para garantizar la seguridad de la plataforma.
              </div>

              <div className="mb-3">
                <label htmlFor="regNombre" className="form-label fw-semibold small">
                  Nombre Completo
                </label>
                <input
                  id="regNombre"
                  type="text"
                  className="form-control"
                  placeholder="Ej. Rodrigo Fernández"
                  value={nombre}
                  onChange={(e) => setNombre(e.target.value)}
                  required
                />
              </div>

              <div className="mb-3">
                <label htmlFor="regCorreo" className="form-label fw-semibold small">
                  Correo Electrónico
                </label>
                <input
                  id="regCorreo"
                  type="email"
                  className="form-control"
                  placeholder="ejemplo@correo.com"
                  value={correo}
                  onChange={(e) => setCorreo(e.target.value)}
                  required
                />
              </div>

              <div className="mb-3">
                <label htmlFor="regClave" className="form-label fw-semibold small">
                  Contraseña (mínimo 6 caracteres)
                </label>
                <input
                  id="regClave"
                  type="password"
                  className="form-control"
                  placeholder="Crea una contraseña segura"
                  value={clave}
                  onChange={(e) => setClave(e.target.value)}
                  required
                />
              </div>

              <div className="text-center small text-muted">
                ¿Ya posees una cuenta?{' '}
                <button
                  type="button"
                  onClick={() => {
                    alCerrar();
                    alIrALogin();
                  }}
                  className="btn btn-link p-0 small text-decoration-none"
                >
                  Inicia sesión aquí
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
                {cargando ? 'Registrando...' : 'Completar Registro'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};
