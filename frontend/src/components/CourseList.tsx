import React, { useState, useMemo } from 'react';
import { Curso } from '../types';
import { TarjetaCurso } from './CourseCard';

interface PropiedadesListaCursos {
  cursos: Curso[];
  cargando: boolean;
  error: string | null;
  alSeleccionarCurso: (idCurso: number) => void;
  alRecargar: () => void;
}

export const ListaCursos: React.FC<PropiedadesListaCursos> = ({
  cursos,
  cargando,
  error,
  alSeleccionarCurso,
  alRecargar,
}) => {
  const [busqueda, setBusqueda] = useState('');
  const [categoriaSeleccionada, setCategoriaSeleccionada] = useState<string>('TODAS');

  // Extraer categorías únicas
  const categorias = useMemo(() => {
    const conjunto = new Set(cursos.map((c) => c.categoria));
    return ['TODAS', ...Array.from(conjunto)];
  }, [cursos]);

  // Filtrado reactivo en memoria
  const cursosFiltrados = useMemo(() => {
    return cursos.filter((curso) => {
      const coincideCategoria =
        categoriaSeleccionada === 'TODAS' ||
        curso.categoria.toLowerCase() === categoriaSeleccionada.toLowerCase();

      const termino = busqueda.trim().toLowerCase();
      const coincideBusqueda =
        termino === '' ||
        curso.titulo.toLowerCase().includes(termino) ||
        curso.descripcion.toLowerCase().includes(termino);

      return coincideCategoria && coincideBusqueda;
    });
  }, [cursos, categoriaSeleccionada, busqueda]);

  return (
    <div>
      <div className="bg-white p-4 rounded-3 shadow-sm mb-4">
        <div className="row g-3 align-items-center">
          <div className="col-md-6">
            <h2 className="h4 fw-bold mb-1">Catálogo de Cursos</h2>
            <p className="text-muted small mb-0">
              Explora cursos evaluados por la comunidad de estudiantes y practicantes.
            </p>
          </div>
          <div className="col-md-6">
            <input
              type="text"
              className="form-control"
              placeholder="Buscar por título o contenido..."
              value={busqueda}
              onChange={(e) => setBusqueda(e.target.value)}
            />
          </div>
        </div>

        {/* Filtros de Categoría */}
        <div className="d-flex flex-wrap gap-2 mt-3 pt-3 border-top">
          {categorias.map((categoria) => (
            <button
              key={categoria}
              type="button"
              onClick={() => setCategoriaSeleccionada(categoria)}
              className={`btn btn-sm rounded-pill ${
                categoriaSeleccionada === categoria
                  ? 'btn-dark'
                  : 'btn-outline-secondary'
              }`}
            >
              {categoria === 'TODAS' ? 'Todas las Categorías' : categoria}
            </button>
          ))}
        </div>
      </div>

      {cargando && (
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Cargando cursos...</span>
          </div>
          <p className="mt-2 text-muted">Cargando catálogo de cursos...</p>
        </div>
      )}

      {error && !cargando && (
        <div className="alert alert-danger d-flex justify-content-between align-items-center" role="alert">
          <div>{error}</div>
          <button type="button" onClick={alRecargar} className="btn btn-outline-danger btn-sm">
            Reintentar
          </button>
        </div>
      )}

      {!cargando && !error && cursosFiltrados.length === 0 && (
        <div className="text-center py-5 bg-white rounded-3 shadow-sm">
          <h5 className="text-muted">No se encontraron cursos coincidentes</h5>
          <p className="small text-secondary mb-3">Intenta cambiar el criterio de búsqueda o la categoría.</p>
          <button
            type="button"
            onClick={() => {
              setBusqueda('');
              setCategoriaSeleccionada('TODAS');
            }}
            className="btn btn-outline-primary btn-sm"
          >
            Limpiar filtros
          </button>
        </div>
      )}

      {!cargando && !error && cursosFiltrados.length > 0 && (
        <div className="row row-cols-1 row-cols-md-2 row-cols-lg-3 row-cols-xl-4 g-4">
          {cursosFiltrados.map((curso) => (
            <div key={curso.id} className="col">
              <TarjetaCurso curso={curso} alSeleccionarCurso={alSeleccionarCurso} />
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
