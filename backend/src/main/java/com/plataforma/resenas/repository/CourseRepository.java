package com.plataforma.resenas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.plataforma.resenas.entity.Course;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByActiveTrue();

    Optional<Course> findByIdAndActiveTrue(Long idCurso);
}
