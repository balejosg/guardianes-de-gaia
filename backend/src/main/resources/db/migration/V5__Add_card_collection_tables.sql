-- Add Card Collection System tables
-- This migration adds tables for the card collection feature

-- Cards table - stores all collectible cards
CREATE TABLE cards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(500) NOT NULL,
    element VARCHAR(20) NOT NULL CHECK (element IN ('FIRE', 'EARTH', 'WATER', 'AIR')),
    rarity VARCHAR(20) NOT NULL CHECK (rarity IN ('COMMON', 'UNCOMMON', 'RARE', 'EPIC', 'LEGENDARY')),
    attack_power INT NOT NULL CHECK (attack_power >= 0 AND attack_power <= 999),
    defense_power INT NOT NULL CHECK (defense_power >= 0 AND defense_power <= 999),
    energy_cost INT NOT NULL CHECK (energy_cost >= 0 AND energy_cost <= 10),
    image_url VARCHAR(500),
    qr_code VARCHAR(16) NOT NULL UNIQUE,
    nfc_code VARCHAR(24) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Card Collections table - one per guardian
CREATE TABLE card_collections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    guardian_id BIGINT NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (guardian_id) REFERENCES guardians(id) ON DELETE CASCADE
);

-- Collected Cards table - junction table with collection metadata
CREATE TABLE collected_cards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    collection_id BIGINT NOT NULL,
    card_id BIGINT NOT NULL,
    count INT NOT NULL DEFAULT 1 CHECK (count > 0),
    first_collected_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_collected_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (collection_id) REFERENCES card_collections(id) ON DELETE CASCADE,
    FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
    UNIQUE KEY unique_collection_card (collection_id, card_id)
);

-- Indexes for performance
CREATE INDEX idx_cards_qr_code ON cards(qr_code);
CREATE INDEX idx_cards_nfc_code ON cards(nfc_code);
CREATE INDEX idx_cards_element ON cards(element);
CREATE INDEX idx_cards_rarity ON cards(rarity);
CREATE INDEX idx_cards_active ON cards(active);

CREATE INDEX idx_card_collections_guardian ON card_collections(guardian_id);

CREATE INDEX idx_collected_cards_collection ON collected_cards(collection_id);
CREATE INDEX idx_collected_cards_card ON collected_cards(card_id);
CREATE INDEX idx_collected_cards_last_collected ON collected_cards(last_collected_at DESC);

-- Insert base set of cards (48 total: 12 per element)
-- FIRE cards (12)
INSERT INTO cards (name, description, element, rarity, attack_power, defense_power, energy_cost, qr_code) VALUES
('Llama Solar', 'Una pequeña llama que brilla con la energía del sol', 'FIRE', 'COMMON', 15, 10, 1, 'GDGF0000000001F'),
('Chispa Ardiente', 'Chispas que bailan en el aire caliente', 'FIRE', 'COMMON', 20, 8, 1, 'GDGF0000000002G'),
('Brasa Valiente', 'Una brasa que nunca se apaga', 'FIRE', 'COMMON', 18, 12, 1, 'GDGF00000000037'),
('Antorcha Mágica', 'Una antorcha que arde sin consumirse', 'FIRE', 'UNCOMMON', 25, 15, 2, 'GDGF0000000004A'),
('Fénix Joven', 'Un pequeño fénix en sus primeros vuelos', 'FIRE', 'UNCOMMON', 30, 20, 2, 'GDGF0000000005B'),
('Volcán Despierto', 'La fuerza de un volcán en erupción', 'FIRE', 'UNCOMMON', 35, 18, 2, 'GDGF0000000006C'),
('Salamandra Real', 'Una salamandra de fuego legendaria', 'FIRE', 'RARE', 45, 25, 3, 'GDGF0000000007D'),
('Dragón de Magma', 'Un pequeño dragón nacido del magma', 'FIRE', 'RARE', 50, 30, 3, 'GDGF0000000008E'),
('Meteorito Ardiente', 'Un fragmento ardiente del espacio', 'FIRE', 'RARE', 40, 35, 3, 'GDGF0000000009F'),
('Espíritu del Sol', 'El guardián espiritual del elemento fuego', 'FIRE', 'EPIC', 60, 40, 4, 'GDGF0000000010G'),
('Titán de Fuego', 'Un titán ancestral del elemento fuego', 'FIRE', 'EPIC', 70, 35, 4, 'GDGF00000000117'),
('Rey Ifrit', 'El soberano supremo del reino del fuego', 'FIRE', 'LEGENDARY', 90, 50, 5, 'GDGF0000000012H');

-- EARTH cards (12)  
INSERT INTO cards (name, description, element, rarity, attack_power, defense_power, energy_cost, qr_code) VALUES
('Semilla Vital', 'Una pequeña semilla llena de potencial', 'EARTH', 'COMMON', 10, 20, 1, 'GDGE0000000013I'),
('Raíz Protectora', 'Raíces que protegen el suelo sagrado', 'EARTH', 'COMMON', 12, 22, 1, 'GDGE0000000014J'),
('Hoja Sanadora', 'Una hoja con poderes curativos', 'EARTH', 'COMMON', 8, 25, 1, 'GDGE0000000015K'),
('Árbol Guardián', 'Un árbol joven que protege el bosque', 'EARTH', 'UNCOMMON', 20, 30, 2, 'GDGE0000000016L'),
('Oso de Piedra', 'Un oso hecho de roca sólida', 'EARTH', 'UNCOMMON', 25, 35, 2, 'GDGE0000000017M'),
('Lobo del Bosque', 'Un lobo guardián del bosque ancestral', 'EARTH', 'UNCOMMON', 30, 25, 2, 'GDGE0000000018N'),
('Golem de Jade', 'Un golem tallado en jade puro', 'EARTH', 'RARE', 35, 45, 3, 'GDGE0000000019O'),
('Druida Ancestral', 'Un antiguo protector de la naturaleza', 'EARTH', 'RARE', 40, 40, 3, 'GDGE0000000020P'),
('Montaña Viviente', 'Una montaña que cobra vida', 'EARTH', 'RARE', 45, 50, 3, 'GDGE0000000021Q'),
('Madre Naturaleza', 'La esencia misma de la tierra', 'EARTH', 'EPIC', 50, 60, 4, 'GDGE0000000022R'),
('Coloso de Gaia', 'Un titán gigante de la madre tierra', 'EARTH', 'EPIC', 65, 55, 4, 'GDGE0000000023S'),
('Señor del Mundo', 'El soberano de todos los elementos terrestres', 'EARTH', 'LEGENDARY', 80, 70, 5, 'GDGE0000000024T');

-- WATER cards (12)
INSERT INTO cards (name, description, element, rarity, attack_power, defense_power, energy_cost, qr_code) VALUES
('Gota de Rocío', 'Una gota de rocío matutino puro', 'WATER', 'COMMON', 12, 18, 1, 'GDGW0000000025U'),
('Burbuja Danzante', 'Burbujas que danzan en el agua', 'WATER', 'COMMON', 14, 16, 1, 'GDGW0000000026V'),
('Arroyo Cantarín', 'Un pequeño arroyo con voz melódica', 'WATER', 'COMMON', 16, 14, 1, 'GDGW0000000027W'),
('Pez Plateado', 'Un pez que brilla como la plata', 'WATER', 'UNCOMMON', 22, 20, 2, 'GDGW0000000028X'),
('Cascada Cristalina', 'Una cascada de aguas cristalinas', 'WATER', 'UNCOMMON', 28, 25, 2, 'GDGW0000000029Y'),
('Delfín Sabio', 'Un delfín con sabiduría ancestral', 'WATER', 'UNCOMMON', 26, 28, 2, 'GDGW0000000030Z'),
('Pulpo Gigante', 'Un pulpo de las profundidades', 'WATER', 'RARE', 38, 32, 3, 'GDGW00000000310'),
('Sirena del Lago', 'Una sirena guardiana del lago sagrado', 'WATER', 'RARE', 42, 38, 3, 'GDGW00000000321'),
('Ballena Ancestral', 'Una ballena milenaria de los océanos', 'WATER', 'RARE', 48, 42, 3, 'GDGW00000000332'),
('Tritón Real', 'El rey de las criaturas marinas', 'WATER', 'EPIC', 55, 45, 4, 'GDGW00000000343'),
('Kraken Legendario', 'El mítico kraken de las profundidades', 'WATER', 'EPIC', 68, 52, 4, 'GDGW00000000354'),
('Poseidón Niño', 'El joven dios de los mares', 'WATER', 'LEGENDARY', 85, 65, 5, 'GDGW00000000365');

-- AIR cards (12)
INSERT INTO cards (name, description, element, rarity, attack_power, defense_power, energy_cost, qr_code) VALUES
('Brisa Suave', 'Una brisa gentil de primavera', 'AIR', 'COMMON', 18, 12, 1, 'GDGA00000000376'),
('Pluma Voladora', 'Una pluma que vuela sin viento', 'AIR', 'COMMON', 20, 10, 1, 'GDGA00000000387'),
('Remolino Juguetón', 'Un pequeño remolino travieso', 'AIR', 'COMMON', 22, 8, 1, 'GDGA00000000398'),
('Colibrí Veloz', 'Un colibrí más rápido que el viento', 'AIR', 'UNCOMMON', 32, 18, 2, 'GDGA00000000409'),
('Viento del Norte', 'El viento frío y poderoso del norte', 'AIR', 'UNCOMMON', 35, 20, 2, 'GDGA000000004110'),
('Águila Dorada', 'Un águila con plumas de oro puro', 'AIR', 'UNCOMMON', 38, 22, 2, 'GDGA000000004211'),
('Halcón Tormenta', 'Un halcón que controla las tormentas', 'AIR', 'RARE', 48, 28, 3, 'GDGA000000004312'),
('Fénix del Viento', 'Un fénix hecho de viento puro', 'AIR', 'RARE', 52, 32, 3, 'GDGA000000004413'),
('Dragón de Nubes', 'Un dragón que vive entre las nubes', 'AIR', 'RARE', 55, 35, 3, 'GDGA000000004514'),
('Señor de los Cielos', 'El guardián supremo de los cielos', 'AIR', 'EPIC', 65, 40, 4, 'GDGA000000004615'),
('Tifón Ancestral', 'Un tifón de poder inconmensurable', 'AIR', 'EPIC', 75, 45, 4, 'GDGA000000004716'),
('Zeus Joven', 'El joven dios del rayo y el trueno', 'AIR', 'LEGENDARY', 95, 55, 5, 'GDGA000000004817');