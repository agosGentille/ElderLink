INSERT INTO Paises (id_pais, nombre) VALUES (1, 'argentina');
INSERT INTO Paises (id_pais, nombre) VALUES (2, 'brasil');

-- Inserción de Provincias
INSERT INTO Provincias (id_provincia, nombre, fk_pais) VALUES (1, 'Entre Rios', 1);
INSERT INTO Provincias (id_provincia, nombre, fk_pais) VALUES (2, 'Santa Fe', 1);
INSERT INTO Provincias (id_provincia, nombre, fk_pais) VALUES (3, 'Buenos Aires', 1);

-- Inserción de Ciudades en Entre Ríos
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (1, 'Paraná', 1);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (2, 'Concordia', 1);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (3, 'Gualeguaychú', 1);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (4, 'Gualeguay', 1);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (5, 'Villaguay', 1);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (6, 'Colón', 1);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (7, 'Federación', 1);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (8, 'La Paz', 1);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (9, 'Victoria', 1);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (10, 'San José', 1);

-- Inserción de Ciudades en Santa Fe
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (11, 'Santa Fe', 2);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (12, 'Rosario', 2);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (13, 'Rafaela', 2);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (14, 'Venado Tuerto', 2);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (15, 'Villa Gobernador Gálvez', 2);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (16, 'Reconquista', 2);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (17, 'San Lorenzo', 2);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (18, 'Cañada de Gómez', 2);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (19, 'Esperanza', 2);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (20, 'Casilda', 2);

-- Inserción de Ciudades en Buenos Aires
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (21, 'La Plata', 3);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (22, 'Mar del Plata', 3);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (23, 'Bahía Blanca', 3);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (24, 'Tandil', 3);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (25, 'Quilmes', 3);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (26, 'Avellaneda', 3);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (27, 'Lomas de Zamora', 3);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (28, 'Morón', 3);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (29, 'San Nicolás de los Arroyos', 3);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (30, 'Tigre', 3);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (31, 'Zárate', 3);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (32, 'San Isidro', 3);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (33, 'Olavarría', 3);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (34, 'Pergamino', 3);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (35, 'Junín', 3);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (36, 'Necochea', 3);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (37, 'Chivilcoy', 3);
INSERT INTO Ciudades (id_ciudad, nombre, fk_provincia) VALUES (38, 'Tres Arroyos', 3);
