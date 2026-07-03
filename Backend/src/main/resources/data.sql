
INSERT IGNORE INTO bundles (
    id_bundle, name_bundle, destination_bundle, description_bundle, 
    start_date_bundle, end_date_bundle, duration_bundle, price_bundle, 
    available_slots_bundle, included_services, `conditions`, restrictions, 
    season_type, category_type, state_bundle, promo_start_date, promo_end_date, promo_discount_percent
) VALUES (
    100, 'Caribe Todo Incluido', 'Punta Cana', 'Disfruta de las mejores playas en un resort 5 estrellas.',
    '2026-12-01', '2026-12-10', 9, 850000, 
    20, 'Vuelos, Hotel 5 estrellas, Traslados, Alimentacion completa', 'Sujeto a disponibilidad', 'Mayores de 18 anios para bebidas alcoholicas',
    'SUMMER', 'PREMIUM', 'AVAILABLE', NULL, NULL, NULL
);

INSERT IGNORE INTO bundles (
    id_bundle, name_bundle, destination_bundle, description_bundle, 
    start_date_bundle, end_date_bundle, duration_bundle, price_bundle, 
    available_slots_bundle, included_services, `conditions`, restrictions, 
    season_type, category_type, state_bundle, promo_start_date, promo_end_date, promo_discount_percent
) VALUES (
    101, 'Aventura en la Patagonia', 'Torres del Paine', 'Trekking por los senderos mas hermosos de Chile.',
    '2026-11-15', '2026-11-22', 7, 600000, 
    15, 'Vuelos, Refugios, Guia certificado, Comidas durante el trekking', 'Buena condicion fisica requerida', 'No recomendado para menores de 12 anios',
    'SPRING', 'STANDARD', 'AVAILABLE', '2026-10-01', '2026-10-31', 0.15
);

-- Insert experience type tags for the new bundles
INSERT IGNORE INTO bundle_experience_types (bundle_id, experience_type) VALUES (100, 'RELAX');
INSERT IGNORE INTO bundle_experience_types (bundle_id, experience_type) VALUES (100, 'ROMANTIC');
INSERT IGNORE INTO bundle_experience_types (bundle_id, experience_type) VALUES (101, 'ADVENTURE');
INSERT IGNORE INTO bundle_experience_types (bundle_id, experience_type) VALUES (101, 'NATURE');
