-- phpMyAdmin SQL Dump
-- version 5.0.4deb2+deb11u1
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Generation Time: Oct 04, 2024 at 07:15 PM
-- Server version: 10.5.26-MariaDB-0+deb11u2
-- PHP Version: 7.4.33

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `sistema_cursos4`
--

-- --------------------------------------------------------

--
-- Table structure for table `Autoevaluaciones`
--

CREATE TABLE `Autoevaluaciones` (
  `id_autoevaluacion` int(11) NOT NULL,
  `fk_curso` int(11) DEFAULT NULL,
  `estado` BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `Ciudades`
--

CREATE TABLE `Ciudades` (
  `id_ciudad` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `fk_provincia` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `Cursos`
--

CREATE TABLE `Cursos` (
  `id_curso` int(11) NOT NULL,
  `nombre` varchar(200) NOT NULL,
  `descripcion` text NOT NULL,
  `fecha_creacion` date NOT NULL,
  `fk_estado_curso` int(11) DEFAULT NULL,
  `fecha_actualizacion` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `observacion` text DEFAULT NULL,
  `fk_usuario` int(11) DEFAULT NULL,
  `activo` BOOLEAN DEFAULT TRUE,
  `thumbnailURL` varchar(150) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `CursoXEtiqueta`
--

CREATE TABLE `CursoXEtiqueta` (
  `fk_curso` int(11) NOT NULL,
  `fk_etiqueta` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `Denuncias`
--

CREATE TABLE `Denuncias` (
  `id_denuncia` int(11) NOT NULL,
  `fk_usuario` int(11) DEFAULT NULL,
  `fk_curso` int(11) DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `razon` VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `Direcciones`
--

CREATE TABLE `Direcciones` (
  `id_direccion` int(11) NOT NULL,
  `calle` varchar(100) NOT NULL,
  `numero` varchar(10) NOT NULL,
  `fk_ciudad` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `EstadoCurso`
--

CREATE TABLE `EstadoCurso` (
  `id_estado_curso` int(11) NOT NULL,
  `nombre` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `Etapas`
--

CREATE TABLE `Etapas` (
  `id_etapa` int(11) NOT NULL,
  `titulo` varchar(200) DEFAULT NULL,
  `contenido` text DEFAULT NULL,
  `activo` tinyint(1) DEFAULT NULL,
  `fk_curso` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `Etiquetas`
--

CREATE TABLE `Etiquetas` (
  `id_etiqueta` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `Eventos`
--

CREATE TABLE `Eventos` (
  `id_evento` int(11) NOT NULL,
  `titulo` varchar(200) DEFAULT NULL,
  `descripcion` text DEFAULT NULL,
  `url_imagen` varchar(255) DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `activo` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `EventoXEtiqueta`
--

CREATE TABLE `EventoXEtiqueta` (
  `fk_evento` int(11) NOT NULL,
  `fk_etiqueta` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `Intereses`
--

CREATE TABLE `Intereses` (
  `fk_etiqueta` int(11) NOT NULL,
  `fk_usuario` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `NotasXUsuario`
--

CREATE TABLE `NotasXUsuario` (
  `id_nota` int(11) NOT NULL,
  `fk_autoevalulacion` int(11) DEFAULT NULL,
  `fk_usuario` int(11) DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `Calificacion` int(11) DEFAULT NULL,
  `Nro_Intento` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `Paises`
--

CREATE TABLE `Paises` (
  `id_pais` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `Preguntas`
--

CREATE TABLE `Preguntas` (
  `id_pregunta` int(11) NOT NULL,
  `fk_autoevaluaciones` int(11) DEFAULT NULL,
  `Pregunta` text DEFAULT NULL,
  `RespuestaCorrecta` text DEFAULT NULL,
  `RespuestaIncorrecta1` text DEFAULT NULL,
  `RespuestaIncorrecta2` text DEFAULT NULL,
  `RespuestaIncorrecta3` text DEFAULT NULL,
  `estado` BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `Provincias`
--

CREATE TABLE `Provincias` (
  `id_provincia` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `fk_pais` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `Roles`
--

CREATE TABLE `Roles` (
  `id_rol` int(11) NOT NULL,
  `descripcion` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `RolesXUsuarios`
--

CREATE TABLE `RolesXUsuarios` (
  `fk_rol` int(11) NOT NULL,
  `fk_usuario` int(11) NOT NULL,
  `estado` VARCHAR(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `Usuarios`
--

CREATE TABLE `Usuarios` (
  `id_usuario` int(11) NOT NULL,
  `DNI_Us` varchar(20) NOT NULL,
  `Nombre_Us` varchar(100) NOT NULL,
  `Apellido_Us` varchar(100) NOT NULL,
  `NombreUsuario_Us` varchar(100) NOT NULL,
  `Email_Us` varchar(100) NOT NULL,
  `Contraseña` varchar(100) NOT NULL,
  `fecha_nac` date NOT NULL,
  `id_direc` int(11) DEFAULT NULL,
  `activo` tinyint(1) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `UsuarioXCurso`
--

CREATE TABLE `UsuarioXCurso` (
  `fk_usuario` int(11) NOT NULL,
  `fk_curso` int(11) NOT NULL,
  `fecha_inicio` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `UsuarioXEtapa`
--

CREATE TABLE `UsuarioXEtapa` (
  `fk_usuario` int(11) NOT NULL,
  `fk_etapa` int(11) NOT NULL,
  `estado` tinyint(1) DEFAULT NULL,
  `fecha` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `Valoraciones`
--

CREATE TABLE `Valoraciones` (
  `fk_usuario` int(11) NOT NULL,
  `fk_curso` int(11) NOT NULL,
  `valoracion` int(11) NOT NULL,
  `fecha` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `Autoevaluaciones`
--
ALTER TABLE `Autoevaluaciones`
  ADD PRIMARY KEY (`id_autoevaluacion`),
  ADD KEY `id_Cu_Eval` (`fk_curso`);

--
-- Indexes for table `Ciudades`
--
ALTER TABLE `Ciudades`
  ADD PRIMARY KEY (`id_ciudad`),
  ADD KEY `id_prov` (`fk_provincia`);

--
-- Indexes for table `Cursos`
--
ALTER TABLE `Cursos`
  ADD PRIMARY KEY (`id_curso`),
  ADD KEY `id_Estado_Cu` (`fk_estado_curso`);

--
-- Indexes for table `CursoXEtiqueta`
--
ALTER TABLE `CursoXEtiqueta`
  ADD PRIMARY KEY (`fk_curso`,`fk_etiqueta`),
  ADD KEY `id_Et_CXE` (`fk_etiqueta`);

--
-- Indexes for table `Denuncias`
--
ALTER TABLE `Denuncias`
  ADD PRIMARY KEY (`id_denuncia`),
  ADD KEY `id_Us` (`fk_usuario`),
  ADD KEY `FK_Curso_den` (`fk_curso`);

--
-- Indexes for table `Direcciones`
--
ALTER TABLE `Direcciones`
  ADD PRIMARY KEY (`id_direccion`),
  ADD KEY `id_ciudad` (`fk_ciudad`);

--
-- Indexes for table `EstadoCurso`
--
ALTER TABLE `EstadoCurso`
  ADD PRIMARY KEY (`id_estado_curso`);

--
-- Indexes for table `Etapas`
--
ALTER TABLE `Etapas`
  ADD PRIMARY KEY (`id_etapa`),
  ADD KEY `id_Cu_Eta` (`fk_curso`);

--
-- Indexes for table `Etiquetas`
--
ALTER TABLE `Etiquetas`
  ADD PRIMARY KEY (`id_etiqueta`);

--
-- Indexes for table `Eventos`
--
ALTER TABLE `Eventos`
  ADD PRIMARY KEY (`id_evento`);

--
-- Indexes for table `EventoXEtiqueta`
--
ALTER TABLE `EventoXEtiqueta`
  ADD PRIMARY KEY (`fk_evento`,`fk_etiqueta`),
  ADD KEY `id_Et_EXE` (`fk_etiqueta`);

--
-- Indexes for table `Intereses`
--
ALTER TABLE `Intereses`
  ADD PRIMARY KEY (`fk_etiqueta`,`fk_usuario`),
  ADD KEY `id_Et_UXE` (`fk_usuario`);

--
-- Indexes for table `NotasXUsuario`
--
ALTER TABLE `NotasXUsuario`
  ADD PRIMARY KEY (`id_nota`),
  ADD KEY `id_AutoEval` (`fk_autoevalulacion`),
  ADD KEY `id_usuario` (`fk_usuario`);

--
-- Indexes for table `Paises`
--
ALTER TABLE `Paises`
  ADD PRIMARY KEY (`id_pais`);

--
-- Indexes for table `Preguntas`
--
ALTER TABLE `Preguntas`
  ADD PRIMARY KEY (`id_pregunta`),
  ADD KEY `id_AutoEval_Preg` (`fk_autoevaluaciones`);

--
-- Indexes for table `Provincias`
--
ALTER TABLE `Provincias`
  ADD PRIMARY KEY (`id_provincia`),
  ADD KEY `id_pais` (`fk_pais`);

--
-- Indexes for table `Roles`
--
ALTER TABLE `Roles`
  ADD PRIMARY KEY (`id_rol`);

--
-- Indexes for table `RolesXUsuarios`
--
ALTER TABLE `RolesXUsuarios`
  ADD PRIMARY KEY (`fk_rol`,`fk_usuario`),
  ADD KEY `id_Us` (`fk_usuario`);

--
-- Indexes for table `Usuarios`
--
ALTER TABLE `Usuarios`
  ADD PRIMARY KEY (`id_usuario`),
  ADD UNIQUE KEY `DNI_Us` (`DNI_Us`),
  ADD KEY `id_direc` (`id_direc`);

--
-- Indexes for table `UsuarioXCurso`
--
ALTER TABLE `UsuarioXCurso`
  ADD PRIMARY KEY (`fk_usuario`,`fk_curso`),
  ADD KEY `id_Cu_UXC` (`fk_curso`);

--
-- Indexes for table `UsuarioXEtapa`
--
ALTER TABLE `UsuarioXEtapa`
  ADD PRIMARY KEY (`fk_usuario`,`fk_etapa`),
  ADD KEY `id_eta` (`fk_etapa`);

--
-- Indexes for table `Valoraciones`
--
ALTER TABLE `Valoraciones`
  ADD PRIMARY KEY (`fk_usuario`,`fk_curso`),
  ADD KEY `id_curso` (`fk_curso`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `Autoevaluaciones`
--
ALTER TABLE `Autoevaluaciones`
  MODIFY `id_autoevaluacion` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `Ciudades`
--
ALTER TABLE `Ciudades`
  MODIFY `id_ciudad` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `Cursos`
--
ALTER TABLE `Cursos`
  MODIFY `id_curso` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `Denuncias`
--
ALTER TABLE `Denuncias`
  MODIFY `id_denuncia` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `Direcciones`
--
ALTER TABLE `Direcciones`
  MODIFY `id_direccion` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `EstadoCurso`
--
ALTER TABLE `EstadoCurso`
  MODIFY `id_estado_curso` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `Etapas`
--
ALTER TABLE `Etapas`
  MODIFY `id_etapa` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `Etiquetas`
--
ALTER TABLE `Etiquetas`
  MODIFY `id_etiqueta` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `Eventos`
--
ALTER TABLE `Eventos`
  MODIFY `id_evento` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `NotasXUsuario`
--
ALTER TABLE `NotasXUsuario`
  MODIFY `id_nota` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `Paises`
--
ALTER TABLE `Paises`
  MODIFY `id_pais` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `Preguntas`
--
ALTER TABLE `Preguntas`
  MODIFY `id_pregunta` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `Provincias`
--
ALTER TABLE `Provincias`
  MODIFY `id_provincia` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `Roles`
--
ALTER TABLE `Roles`
  MODIFY `id_rol` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `Usuarios`
--
ALTER TABLE `Usuarios`
  MODIFY `id_usuario` int(11) NOT NULL AUTO_INCREMENT;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `Autoevaluaciones`
--
ALTER TABLE `Autoevaluaciones`
  ADD CONSTRAINT `Autoevaluaciones_ibfk_1` FOREIGN KEY (`fk_curso`) REFERENCES `Cursos` (`id_curso`);

--
-- Constraints for table `Ciudades`
--
ALTER TABLE `Ciudades`
  ADD CONSTRAINT `Ciudades_ibfk_1` FOREIGN KEY (`fk_provincia`) REFERENCES `Provincias` (`id_provincia`);

--
-- Constraints for table `Cursos`
--
ALTER TABLE `Cursos`
  ADD CONSTRAINT `Cursos_ibfk_1` FOREIGN KEY (`fk_estado_curso`) REFERENCES `EstadoCurso` (`id_estado_curso`);

--
-- Constraints for table `CursoXEtiqueta`
--
ALTER TABLE `CursoXEtiqueta`
  ADD CONSTRAINT `CursoXEtiqueta_ibfk_1` FOREIGN KEY (`fk_curso`) REFERENCES `Cursos` (`id_curso`),
  ADD CONSTRAINT `CursoXEtiqueta_ibfk_2` FOREIGN KEY (`fk_etiqueta`) REFERENCES `Etiquetas` (`id_etiqueta`);

--
-- Constraints for table `Denuncias`
--
ALTER TABLE `Denuncias`
  ADD CONSTRAINT `Denuncias_ibfk_1` FOREIGN KEY (`fk_usuario`) REFERENCES `Usuarios` (`id_usuario`),
  ADD CONSTRAINT `Denuncias_ibfk_2` FOREIGN KEY (`fk_curso`) REFERENCES `Cursos` (`id_curso`);

--
-- Constraints for table `Direcciones`
--
ALTER TABLE `Direcciones`
  ADD CONSTRAINT `Direcciones_ibfk_1` FOREIGN KEY (`fk_ciudad`) REFERENCES `Ciudades` (`id_ciudad`);

--
-- Constraints for table `Etapas`
--
ALTER TABLE `Etapas`
  ADD CONSTRAINT `Etapas_ibfk_1` FOREIGN KEY (`fk_curso`) REFERENCES `Cursos` (`id_curso`);

--
-- Constraints for table `EventoXEtiqueta`
--
ALTER TABLE `EventoXEtiqueta`
  ADD CONSTRAINT `EventoXEtiqueta_ibfk_1` FOREIGN KEY (`fk_evento`) REFERENCES `Eventos` (`id_evento`),
  ADD CONSTRAINT `EventoXEtiqueta_ibfk_2` FOREIGN KEY (`fk_etiqueta`) REFERENCES `Etiquetas` (`id_etiqueta`);

--
-- Constraints for table `Intereses`
--
ALTER TABLE `Intereses`
  ADD CONSTRAINT `Intereses_ibfk_1` FOREIGN KEY (`fk_etiqueta`) REFERENCES `Etiquetas` (`id_etiqueta`),
  ADD CONSTRAINT `Intereses_ibfk_2` FOREIGN KEY (`fk_usuario`) REFERENCES `Usuarios` (`id_usuario`);

--
-- Constraints for table `NotasXUsuario`
--
ALTER TABLE `NotasXUsuario`
  ADD CONSTRAINT `NotasXUsuario_ibfk_1` FOREIGN KEY (`fk_autoevalulacion`) REFERENCES `Autoevaluaciones` (`id_autoevaluacion`),
  ADD CONSTRAINT `NotasXUsuario_ibfk_2` FOREIGN KEY (`fk_usuario`) REFERENCES `Usuarios` (`id_usuario`);

--
-- Constraints for table `Preguntas`
--
ALTER TABLE `Preguntas`
  ADD CONSTRAINT `Preguntas_ibfk_1` FOREIGN KEY (`fk_autoevaluaciones`) REFERENCES `Autoevaluaciones` (`id_autoevaluacion`);

--
-- Constraints for table `Provincias`
--
ALTER TABLE `Provincias`
  ADD CONSTRAINT `Provincias_ibfk_1` FOREIGN KEY (`fk_pais`) REFERENCES `Paises` (`id_pais`);

--
-- Constraints for table `RolesXUsuarios`
--
ALTER TABLE `RolesXUsuarios`
  ADD CONSTRAINT `RolesXUsuarios_ibfk_1` FOREIGN KEY (`fk_rol`) REFERENCES `Roles` (`id_rol`),
  ADD CONSTRAINT `RolesXUsuarios_ibfk_2` FOREIGN KEY (`fk_usuario`) REFERENCES `Usuarios` (`id_usuario`);

--
-- Constraints for table `Usuarios`
--
ALTER TABLE `Usuarios`
  ADD CONSTRAINT `Usuarios_ibfk_1` FOREIGN KEY (`id_direc`) REFERENCES `Direcciones` (`id_direccion`);

--
-- Constraints for table `UsuarioXCurso`
--
ALTER TABLE `UsuarioXCurso`
  ADD CONSTRAINT `UsuarioXCurso_ibfk_1` FOREIGN KEY (`fk_usuario`) REFERENCES `Usuarios` (`id_usuario`),
  ADD CONSTRAINT `UsuarioXCurso_ibfk_2` FOREIGN KEY (`fk_curso`) REFERENCES `Cursos` (`id_curso`);

--
-- Constraints for table `UsuarioXEtapa`
--
ALTER TABLE `UsuarioXEtapa`
  ADD CONSTRAINT `UsuarioXEtapa_ibfk_1` FOREIGN KEY (`fk_usuario`) REFERENCES `Usuarios` (`id_usuario`),
  ADD CONSTRAINT `UsuarioXEtapa_ibfk_2` FOREIGN KEY (`fk_etapa`) REFERENCES `Etapas` (`id_etapa`);

--
-- Constraints for table `Valoraciones`
--
ALTER TABLE `Valoraciones`
  ADD CONSTRAINT `Valoraciones_ibfk_1` FOREIGN KEY (`fk_usuario`) REFERENCES `Usuarios` (`id_usuario`),
  ADD CONSTRAINT `Valoraciones_ibfk_2` FOREIGN KEY (`fk_curso`) REFERENCES `Cursos` (`id_curso`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
