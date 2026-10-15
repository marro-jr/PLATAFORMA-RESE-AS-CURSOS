import React, { useState, useEffect, useCallback } from 'react';
import { Curso, Resena } from './types';
import { listarCursos } from './services/api';
import { Navbar } from './components/Navbar';
import { ListaCursos } from './components/CourseList';
import { DetalleCurso } from './components/CourseDetail';
import { MisResenas } from './components/MyReviews';
import { PanelModeracion } from './components/ModerationPanel';
import { ModalResena } from './components/ReviewModal';
import { ModalLogin } from './components/LoginModal';
import { ModalRegistro } from './components/RegisterModal';

export const App: React.FC = () => {
  // Estados de Navegación
  const [vista, setVista] = useState<string>('cursos');
  const [idCursoSeleccionado, setIdCursoSeleccionado] = useState<number | null>(null);

  // Estados de Datos de Cursos
  const [cursos, setCursos] = useState<Curso[]>([]);
  const [cargandoCursos, setCargandoCursos] = useState<boolean>(true);
  const [errorCursos, setErrorCursos] = useState<string | null>(null);

  // Estados de Modales
  const [modalLoginVisible, setModalLoginVisible] = useState<boolean>(false);
  const [modalRegistroVisible, setModalRegistroVisible] = useState<boolean>(false);
  const [modalResenaVisible, setModalResenaVisible] = useState<boolean>(false);
  const [cursoParaResena, setCursoParaResena] = useState<Curso | null>(null);
  const [resenaParaEditar, setResenaParaEditar] = useState<Resena | null>(null);

  const cargarCursos = useCallback(async () => {
    setCargandoCursos(true);
    setErrorCursos(null);
    try {
      const datos = await listarCursos();
      setCursos(datos);
    } catch (err: unknown) {
      if (err instanceof Error) {
        setErrorCursos(err.message);
      } else {
        setErrorCursos('Error de conexión con el backend.');
      }
    } finally {
      setCargandoCursos(false);
    }
  }, []);

  useEffect(() => {
    cargarCursos();
  }, [cargarCursos]);

  const seleccionarCurso = (id: number) => {
    setIdCursoSeleccionado(id);
    setVista('detalle-curso');
  };

  const abrirNuevaResena = (curso: Curso) => {
    setCursoParaResena(curso);
    setResenaParaEditar(null);
    setModalResenaVisible(true);
  };

  const abrirEditarResena = (resena: Resena) => {
    setResenaParaEditar(resena);
    setCursoParaResena(null);
    setModalResenaVisible(true);
  };

  const guardarResenaExitoso = () => {
    cargarCursos();
  };

  return (
    <div className="d-flex flex-column min-vh-100 bg-light">
      <Navbar
        vistaActual={vista}
        alCambiarVista={(nuevaVista) => {
          setVista(nuevaVista);
          if (nuevaVista === 'cursos') {
            setIdCursoSeleccionado(null);
          }
        }}
        alAbrirLogin={() => setModalLoginVisible(true)}
        alAbrirRegistro={() => setModalRegistroVisible(true)}
      />

      <main className="container py-4 flex-grow-1">
        {vista === 'cursos' && (
          <ListaCursos
            cursos={cursos}
            cargando={cargandoCursos}
            error={errorCursos}
            alSeleccionarCurso={seleccionarCurso}
            alRecargar={cargarCursos}
          />
        )}

        {vista === 'detalle-curso' && idCursoSeleccionado && (
          <DetalleCurso
            idCurso={idCursoSeleccionado}
            alVolver={() => setVista('cursos')}
            alAbrirNuevaResena={abrirNuevaResena}
            alAbrirLogin={() => setModalLoginVisible(true)}
          />
        )}

        {vista === 'mis-resenas' && (
          <MisResenas
            alEditarResena={abrirEditarResena}
            alIrACursos={() => setVista('cursos')}
          />
        )}

        {vista === 'moderacion' && <PanelModeracion />}
      </main>

      {/* Modales de Autenticación y Reseña */}
      <ModalLogin
        visible={modalLoginVisible}
        alCerrar={() => setModalLoginVisible(false)}
        alIrARegistro={() => setModalRegistroVisible(true)}
        alLoginExitoso={() => {
          cargarCursos();
        }}
      />

      <ModalRegistro
        visible={modalRegistroVisible}
        alCerrar={() => setModalRegistroVisible(false)}
        alIrALogin={() => setModalLoginVisible(true)}
      />

      <ModalResena
        visible={modalResenaVisible}
        alCerrar={() => {
          setModalResenaVisible(false);
          setResenaParaEditar(null);
          setCursoParaResena(null);
        }}
        curso={cursoParaResena}
        resenaParaEditar={resenaParaEditar}
        alGuardarExitoso={guardarResenaExitoso}
      />

      <footer className="bg-white border-top py-3 mt-auto">
        <div className="container text-center text-muted small">
          <p className="mb-1 fw-semibold">
            Plataforma de Reseñas de Cursos — Portafolio de Ingeniería de Software
          </p>
          <p className="mb-0 text-secondary">
            Arquitectura en Capas: React → Controller → Service → Repository → MySQL | Spring Boot & Spring Security con JWT
          </p>
        </div>
      </footer>
    </div>
  );
};
