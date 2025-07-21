-- E2E Test Data Initialization for Guardianes de Gaia
-- This file contains comprehensive test data for all E2E test scenarios

-- ============================================
-- Guardian Profiles Test Data
-- ============================================

-- Test Guardian 1: Complete profile for user journey tests
INSERT INTO guardians (id, name, email, age, level, experience_points, total_energy, available_energy, total_steps, created_at, updated_at) 
VALUES (1, 'Ana Martínez', 'ana.martinez@test.com', 8, 3, 2500, 1500, 850, 15000, NOW(), NOW());

-- Test Guardian 2: New guardian for simple journey tests
INSERT INTO guardians (id, name, email, age, level, experience_points, total_energy, available_energy, total_steps, created_at, updated_at) 
VALUES (2, 'Carlos López', 'carlos.lopez@test.com', 10, 1, 250, 300, 300, 3000, NOW(), NOW());

-- Test Guardian 3: Advanced guardian for complex tests
INSERT INTO guardians (id, name, email, age, level, experience_points, total_energy, available_energy, total_steps, created_at, updated_at) 
VALUES (3, 'Sofía García', 'sofia.garcia@test.com', 12, 5, 8750, 3200, 1200, 35000, NOW(), NOW());

-- Test Guardian 4: For team/pacto functionality tests
INSERT INTO guardians (id, name, email, age, level, experience_points, total_energy, available_energy, total_steps, created_at, updated_at) 
VALUES (4, 'Diego Rodríguez', 'diego.rodriguez@test.com', 9, 2, 1200, 800, 400, 8000, NOW(), NOW());

-- ============================================
-- Pacto (Family Groups) Test Data
-- ============================================

-- Test Pacto 1: Active family group
INSERT INTO pactos (id, name, description, guia_id, max_members, total_energy, created_at, updated_at) 
VALUES (1, 'Los Guardianes Verdes', 'Familia comprometida con el medio ambiente', 1, 6, 4600, NOW(), NOW());

-- Test Pacto 2: Small family group
INSERT INTO pactos (id, name, description, guia_id, max_members, total_energy, created_at, updated_at) 
VALUES (2, 'Aventureros Urbanos', 'Exploradores de la ciudad', 3, 4, 4000, NOW(), NOW());

-- Pacto memberships
INSERT INTO pacto_memberships (pacto_id, guardian_id, joined_at, is_active) 
VALUES (1, 1, NOW(), true), (1, 2, NOW(), true), (1, 4, NOW(), true);

INSERT INTO pacto_memberships (pacto_id, guardian_id, joined_at, is_active) 
VALUES (2, 3, NOW(), true);

-- ============================================
-- Cards Collection Test Data
-- ============================================

-- Base Cards (12 per element)
-- Earth Element Cards
INSERT INTO cards (id, name, element, rarity, energy_cost, attack_power, defense_power, special_ability, description, image_url, is_nfc_enabled) VALUES
(1, 'Guardián del Bosque', 'EARTH', 'COMMON', 3, 4, 6, 'Regeneración', 'Protector ancestral de los bosques milenarios', '/cards/earth/guardian_bosque.png', false),
(2, 'Espíritu del Árbol', 'EARTH', 'COMMON', 2, 3, 4, 'Crecimiento', 'Alma viviente de los árboles centenarios', '/cards/earth/espiritu_arbol.png', false),
(3, 'Oso de las Montañas', 'EARTH', 'COMMON', 4, 6, 5, 'Fuerza Bruta', 'Majestuoso habitante de las cumbres', '/cards/earth/oso_montanas.png', false),
(4, 'Druida de Gaia', 'EARTH', 'RARE', 5, 5, 7, 'Sanación Natural', 'Sabio conocedor de los secretos de la tierra', '/cards/earth/druida_gaia.png', false),
(5, 'Lobo Alpha', 'EARTH', 'RARE', 4, 7, 4, 'Liderazgo', 'Líder de la manada, feroz y leal', '/cards/earth/lobo_alpha.png', false),
(6, 'Tótem de Piedra', 'EARTH', 'RARE', 3, 2, 9, 'Barrera Pétrea', 'Guardián milenario de roca ancestral', '/cards/earth/totem_piedra.png', false),
(7, 'Dragón de las Raíces', 'EARTH', 'EPIC', 7, 9, 8, 'Terremoto', 'Serpiente colosal que habita bajo tierra', '/cards/earth/dragon_raices.png', false),
(8, 'Titan de Granito', 'EARTH', 'EPIC', 8, 10, 10, 'Avalancha', 'Gigante de piedra que camina entre montañas', '/cards/earth/titan_granito.png', false),
(9, 'Señor de la Selva', 'EARTH', 'EPIC', 6, 8, 7, 'Llamada Salvaje', 'Emperador de todas las criaturas del bosque', '/cards/earth/senor_selva.png', false),
(10, 'Madre Naturaleza', 'EARTH', 'LEGENDARY', 10, 12, 12, 'Renovación Total', 'Esencia pura de la vida terrestre', '/cards/earth/madre_naturaleza.png', true),
(11, 'Gaia Despertada', 'EARTH', 'LEGENDARY', 12, 15, 15, 'Renacimiento', 'La propia tierra cobra vida para defenderse', '/cards/earth/gaia_despertada.png', true),
(12, 'Corazón del Mundo', 'EARTH', 'LEGENDARY', 9, 8, 16, 'Vida Eterna', 'Núcleo viviente que late en el centro del planeta', '/cards/earth/corazon_mundo.png', true);

-- Water Element Cards
INSERT INTO cards (id, name, element, rarity, energy_cost, attack_power, defense_power, special_ability, description, image_url, is_nfc_enabled) VALUES
(13, 'Ninfa del Arroyo', 'WATER', 'COMMON', 2, 3, 3, 'Fluidez', 'Espíritu juguetón de las aguas cristalinas', '/cards/water/ninfa_arroyo.png', false),
(14, 'Pez de las Profundidades', 'WATER', 'COMMON', 3, 4, 4, 'Navegación', 'Habitante de los abismos marinos', '/cards/water/pez_profundidades.png', false),
(15, 'Delfín Guardián', 'WATER', 'COMMON', 3, 5, 3, 'Ecolocalización', 'Protector inteligente de los océanos', '/cards/water/delfin_guardian.png', false),
(16, 'Sirena del Coral', 'WATER', 'RARE', 4, 4, 6, 'Canto Curativo', 'Hermosa guardiana de los arrecifes', '/cards/water/sirena_coral.png', false),
(17, 'Pulpo Gigante', 'WATER', 'RARE', 5, 7, 5, 'Tentáculos', 'Majestuoso molusco de las fosas marinas', '/cards/water/pulpo_gigante.png', false),
(18, 'Espíritu del Glaciar', 'WATER', 'RARE', 4, 3, 8, 'Congelación', 'Alma ancestral del hielo eterno', '/cards/water/espiritu_glaciar.png', false),
(19, 'Ballena Ancestral', 'WATER', 'EPIC', 7, 8, 9, 'Canto Oceánico', 'Sabia gigante de los mares profundos', '/cards/water/ballena_ancestral.png', false),
(20, 'Dragón de Hielo', 'WATER', 'EPIC', 8, 10, 8, 'Aliento Glacial', 'Bestia legendaria de las tierras heladas', '/cards/water/dragon_hielo.png', false),
(21, 'Kraken Despertado', 'WATER', 'EPIC', 9, 12, 7, 'Maremoto', 'Terror colosal de las profundidades', '/cards/water/kraken_despertado.png', false),
(22, 'Poseidón Menor', 'WATER', 'LEGENDARY', 10, 11, 11, 'Control Oceánico', 'Señor divino de todos los mares', '/cards/water/poseidon_menor.png', true),
(23, 'Leviatán Primordial', 'WATER', 'LEGENDARY', 12, 14, 12, 'Tsunami', 'Primera criatura nacida de las aguas', '/cards/water/leviatan_primordial.png', true),
(24, 'Océano Viviente', 'WATER', 'LEGENDARY', 11, 9, 18, 'Marea Infinita', 'Todos los mares del mundo unidos en uno', '/cards/water/oceano_viviente.png', true);

-- Fire Element Cards
INSERT INTO cards (id, name, element, rarity, energy_cost, attack_power, defense_power, special_ability, description, image_url, is_nfc_enabled) VALUES
(25, 'Salamandra Ígnea', 'FIRE', 'COMMON', 2, 4, 2, 'Ignición', 'Pequeña criatura de las llamas danzantes', '/cards/fire/salamandra_ignea.png', false),
(26, 'Lobo de Fuego', 'FIRE', 'COMMON', 3, 5, 3, 'Mordida Flamígera', 'Depredador salvaje envuelto en llamas', '/cards/fire/lobo_fuego.png', false),
(27, 'Espíritu del Volcán', 'FIRE', 'COMMON', 3, 4, 4, 'Erupción Menor', 'Alma ardiente de los volcanes activos', '/cards/fire/espiritu_volcan.png', false),
(28, 'Fénix Joven', 'FIRE', 'RARE', 5, 6, 4, 'Renacimiento', 'Ave mítica en sus primeros vuelos', '/cards/fire/fenix_joven.png', false),
(29, 'Golem de Lava', 'FIRE', 'RARE', 6, 7, 6, 'Armadura Fundida', 'Guerrero forjado en magma ardiente', '/cards/fire/golem_lava.png', false),
(30, 'Demonio del Infierno', 'FIRE', 'RARE', 4, 8, 3, 'Fuego Infernal', 'Bestia siniestra de las profundidades', '/cards/fire/demonio_infierno.png', false),
(31, 'Dragón Rojo', 'FIRE', 'EPIC', 8, 11, 7, 'Aliento de Fuego', 'Majestuoso señor de las llamas', '/cards/fire/dragon_rojo.png', false),
(32, 'Fénix Emperador', 'FIRE', 'EPIC', 7, 9, 8, 'Resurrección', 'Ave sagrada de poder inmortal', '/cards/fire/fenix_emperador.png', false),
(33, 'Ifrit Conquistador', 'FIRE', 'EPIC', 9, 12, 6, 'Tormenta Ígnea', 'Señor de los djinn del fuego', '/cards/fire/ifrit_conquistador.png', false),
(34, 'Sol Viviente', 'FIRE', 'LEGENDARY', 11, 13, 10, 'Corona Solar', 'Estrella que camina sobre la tierra', '/cards/fire/sol_viviente.png', true),
(35, 'Ragnarök', 'FIRE', 'LEGENDARY', 12, 16, 8, 'Fin del Mundo', 'Fuego que consume todo lo existente', '/cards/fire/ragnarok.png', true),
(36, 'Núcleo de Fuego', 'FIRE', 'LEGENDARY', 10, 10, 14, 'Calor Eterno', 'Corazón ardiente del planeta', '/cards/fire/nucleo_fuego.png', true);

-- Air Element Cards
INSERT INTO cards (id, name, element, rarity, energy_cost, attack_power, defense_power, special_ability, description, image_url, is_nfc_enabled) VALUES
(37, 'Hada del Viento', 'AIR', 'COMMON', 2, 3, 3, 'Vuelo Rápido', 'Criatura etérea de las brisas suaves', '/cards/air/hada_viento.png', false),
(38, 'Águila Dorada', 'AIR', 'COMMON', 3, 5, 2, 'Vista Águila', 'Rapaz majestuosa de los cielos', '/cards/air/aguila_dorada.png', false),
(39, 'Sylph Danzante', 'AIR', 'COMMON', 2, 2, 5, 'Esquiva Aérea', 'Espíritu grácil de las corrientes', '/cards/air/sylph_danzante.png', false),
(40, 'Grifo Real', 'AIR', 'RARE', 5, 7, 5, 'Vuelo Majestuoso', 'Guardián alado de tesoros antiguos', '/cards/air/grifo_real.png', false),
(41, 'Roc Gigante', 'AIR', 'RARE', 6, 8, 4, 'Garra Devastadora', 'Ave colosal de las cumbres nevadas', '/cards/air/roc_gigante.png', false),
(42, 'Señor de las Tormentas', 'AIR', 'RARE', 4, 6, 6, 'Rayo', 'Maestro de los vientos tempestuosos', '/cards/air/senor_tormentas.png', false),
(43, 'Dragón de las Nubes', 'AIR', 'EPIC', 7, 9, 8, 'Aliento de Viento', 'Serpiente etérea que vuela entre nubes', '/cards/air/dragon_nubes.png', false),
(44, 'Tifón Primordial', 'AIR', 'EPIC', 8, 11, 7, 'Huracán', 'Tormenta ancestral con conciencia', '/cards/air/tifon_primordial.png', false),
(45, 'Quetzalcóatl', 'AIR', 'EPIC', 9, 10, 9, 'Serpiente Emplumada', 'Dios antiguo de los vientos', '/cards/air/quetzalcoatl.png', false),
(46, 'Señor del Olimpo', 'AIR', 'LEGENDARY', 10, 12, 11, 'Rayo Divino', 'Soberano divino de los cielos', '/cards/air/senor_olimpo.png', true),
(47, 'Viento Eterno', 'AIR', 'LEGENDARY', 11, 8, 16, 'Brisa Infinita', 'Aire primordial que nunca cesa', '/cards/air/viento_eterno.png', true),
(48, 'Cielo Viviente', 'AIR', 'LEGENDARY', 12, 14, 12, 'Dominio Celestial', 'Toda la atmósfera hecha conciencia', '/cards/air/cielo_viviente.png', true);

-- ============================================
-- Guardian Card Collections
-- ============================================

-- Ana's collection (Guardian 1) - Experienced player
INSERT INTO guardian_cards (guardian_id, card_id, quantity, obtained_at) VALUES
-- Earth cards
(1, 1, 3, NOW()), (1, 2, 2, NOW()), (1, 3, 2, NOW()), (1, 4, 1, NOW()), (1, 5, 1, NOW()), (1, 10, 1, NOW()),
-- Water cards
(1, 13, 2, NOW()), (1, 14, 2, NOW()), (1, 16, 1, NOW()), (1, 17, 1, NOW()),
-- Fire cards
(1, 25, 1, NOW()), (1, 26, 2, NOW()), (1, 28, 1, NOW()),
-- Air cards
(1, 37, 2, NOW()), (1, 38, 1, NOW()), (1, 40, 1, NOW());

-- Carlos's collection (Guardian 2) - New player
INSERT INTO guardian_cards (guardian_id, card_id, quantity, obtained_at) VALUES
(2, 1, 1, NOW()), (2, 2, 1, NOW()), (2, 13, 1, NOW()), (2, 25, 1, NOW()), (2, 37, 1, NOW());

-- Sofia's collection (Guardian 3) - Advanced player
INSERT INTO guardian_cards (guardian_id, card_id, quantity, obtained_at) VALUES
-- Complete collection with legendary cards
(3, 1, 2, NOW()), (3, 4, 2, NOW()), (3, 7, 1, NOW()), (3, 10, 1, NOW()), (3, 11, 1, NOW()),
(3, 16, 2, NOW()), (3, 19, 1, NOW()), (3, 22, 1, NOW()),
(3, 28, 2, NOW()), (3, 31, 1, NOW()), (3, 34, 1, NOW()),
(3, 40, 2, NOW()), (3, 43, 1, NOW()), (3, 46, 1, NOW());

-- ============================================
-- Step Records Test Data
-- ============================================

-- Recent step records for testing
INSERT INTO step_records (guardian_id, steps_count, energy_generated, recorded_date, location_name, route_id, created_at) VALUES
(1, 1500, 150, CURDATE(), 'Parque Central', null, NOW()),
(1, 2200, 220, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'Colegio San José', null, NOW()),
(1, 1800, 180, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'Plaza Mayor', null, NOW()),
(2, 800, 80, CURDATE(), 'Colegio San José', null, NOW()),
(2, 1200, 120, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'Casa - Colegio', null, NOW()),
(3, 2500, 250, CURDATE(), 'Ruta del Bosque', 1, NOW()),
(3, 3000, 300, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'Sendero Verde', 2, NOW()),
(4, 1000, 100, CURDATE(), 'Barrio Nuevo', null, NOW());

-- ============================================
-- Daily Challenges Test Data
-- ============================================

INSERT INTO daily_challenges (id, name, description, challenge_type, target_value, reward_energy, reward_experience, active_date, created_at) VALUES
(1, 'Camina 2000 pasos', 'Completa 2000 pasos hoy para ganar energía extra', 'STEPS', 2000, 200, 100, CURDATE(), NOW()),
(2, 'Colecciona 3 cartas', 'Encuentra y escanea 3 cartas QR diferentes', 'CARD_COLLECTION', 3, 150, 150, CURDATE(), NOW()),
(3, 'Gana 2 batallas', 'Vence a otros guardianes en combate', 'BATTLE_WINS', 2, 300, 200, CURDATE(), NOW()),
(4, 'Camina en equipo', 'Completa una ruta con tu Pacto', 'TEAM_ACTIVITY', 1, 250, 175, CURDATE(), NOW()),
(5, 'Energía del día', 'Genera 500 puntos de energía caminando', 'ENERGY_GENERATION', 500, 100, 50, CURDATE(), NOW());

-- Challenge progress for testing
INSERT INTO guardian_challenge_progress (guardian_id, challenge_id, current_progress, completed, completed_at) VALUES
(1, 1, 1500, false, null),
(1, 2, 2, false, null),
(1, 5, 350, false, null),
(2, 1, 800, false, null),
(3, 1, 2500, true, NOW()),
(3, 3, 1, false, null),
(3, 5, 550, true, NOW());

-- ============================================
-- Battle Records Test Data
-- ============================================

INSERT INTO battle_records (id, challenger_id, opponent_id, winner_id, challenger_deck, opponent_deck, battle_log, duration_seconds, created_at) VALUES
(1, 1, 3, 3, '[{"cardId": 1, "position": 1}, {"cardId": 4, "position": 2}]', '[{"cardId": 7, "position": 1}, {"cardId": 10, "position": 2}]', 'Battle log details...', 180, NOW()),
(2, 3, 1, 1, '[{"cardId": 10, "position": 1}, {"cardId": 22, "position": 2}]', '[{"cardId": 1, "position": 1}, {"cardId": 16, "position": 2}]', 'Battle log details...', 240, DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- ============================================
-- Routes Test Data
-- ============================================

INSERT INTO routes (id, name, description, start_location, end_location, distance_meters, estimated_duration_minutes, difficulty_level, bonus_multiplier, is_active, created_by, created_at) VALUES
(1, 'Ruta del Bosque Encantado', 'Sendero mágico entre árboles centenarios', 'Entrada del Parque', 'Mirador del Bosque', 1500, 20, 'EASY', 1.2, true, 1, NOW()),
(2, 'Sendero Verde Urbano', 'Camino ecológico por la ciudad', 'Plaza Central', 'Parque Ecológico', 2200, 35, 'MEDIUM', 1.5, true, 3, NOW()),
(3, 'Desafío de la Montaña', 'Ascenso challenging para guardianes experimentados', 'Base de la Montaña', 'Cumbre Vista', 3500, 60, 'HARD', 2.0, true, 1, NOW());

-- Route completions
INSERT INTO route_completions (guardian_id, route_id, completion_time_seconds, steps_taken, energy_earned, completed_at) VALUES
(1, 1, 1200, 1500, 180, NOW()),
(3, 1, 1050, 1400, 168, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(3, 2, 2100, 2200, 330, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- ============================================
-- QR Code Locations Test Data
-- ============================================

INSERT INTO qr_code_locations (id, card_id, location_name, latitude, longitude, description, is_active, created_at, updated_at) VALUES
(1, 1, 'Parque Central - Árbol Grande', 40.7128, -74.0060, 'QR pegado en el árbol más grande del parque', true, NOW(), NOW()),
(2, 13, 'Fuente de la Plaza', 40.7589, -73.9851, 'QR cerca de la fuente principal', true, NOW(), NOW()),
(3, 25, 'Mural de la Escuela', 40.6892, -74.0445, 'QR en el mural del patio del colegio', true, NOW(), NOW()),
(4, 37, 'Torre del Reloj', 40.7504, -73.9938, 'QR en la base de la torre del reloj', true, NOW(), NOW()),
(5, 4, 'Jardín Botánico', 40.8176, -73.8781, 'QR en la entrada del jardín botánico', true, NOW(), NOW());

-- ============================================
-- System Configuration
-- ============================================

-- Energy conversion rates and game settings
INSERT INTO game_settings (setting_key, setting_value, description, updated_at) VALUES
('steps_per_energy', '10', 'Pasos necesarios para generar 1 punto de energía', NOW()),
('daily_energy_cap', '1000', 'Máximo de energía que se puede generar por día', NOW()),
('card_pack_cost', '100', 'Costo en energía de un pack de cartas', NOW()),
('battle_entry_cost', '50', 'Costo en energía para iniciar una batalla', NOW()),
('xp_per_step', '1', 'Experiencia ganada por cada paso', NOW()),
('level_up_threshold', '1000', 'XP base necesaria para subir de nivel', NOW());

-- ============================================
-- Test Users for Authentication
-- ============================================

-- API test users (for authentication testing)
INSERT INTO api_users (id, username, password_hash, email, role, is_active, created_at) VALUES
(1, 'e2e_admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'admin@e2e.test', 'ADMIN', true, NOW()),
(2, 'e2e_user', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'user@e2e.test', 'USER', true, NOW());

-- Link API users to guardians
UPDATE guardians SET api_user_id = 1 WHERE id = 1;
UPDATE guardians SET api_user_id = 2 WHERE id = 2;

-- ============================================
-- Test Scenarios Completion
-- ============================================

-- Additional data for comprehensive E2E testing scenarios
COMMIT;