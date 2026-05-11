-- ==========================================
-- TEST DATA FOR DECKFORGE (CORRECTED)
-- ==========================================

-- 1. Opret Brugere
INSERT INTO users (username, email, password_hash, user_role) VALUES
                                                                  ('AdminAnders', 'anders@deckforge.dk', '$2a$10$ba3ML9Sh/vaILC6oywLzEOEI1DY2VrDeGSUVUCZZqVB0fWxUTQy/6', 'ADMIN'),
                                                                  ('EventEva', 'eva@deckforge.dk', 'event123', 'ORGANIZER'),
                                                                  ('SpillerSøren', 'soren@deckforge.dk', '$2a$10$mwgqBHg3o8QeP1dxEhfEfej9/jdbf2ZMhWhNnv5AD/FSD3wMOHkP6', 'MEMBER'), -- kode spiller123
                                                                  ('KortKaj', 'kaj@deckforge.dk', '$2a$10$wS63DEbMVNo4PvYjJs4YXebI.QCraVJV5GzJGsWBkcdf1Kvz76WIi', 'MEMBER');



-- 2. Opret Kort (Kataloget)
INSERT INTO cards (card_id, card_name, card_set, card_type, card_rarity, mana_cost, color_identity, power, health, description) VALUES
                                                                (1, 'Black Lotus', 'Alpha', 'ARTIFACT', 'MYTHICRARE', '{0}', '', NULL, NULL, 'Adds 3 mana of any single color.'),
                                                                (2, 'Shivan Dragon', 'Alpha', 'CREATURE', 'RARE', '{4}{R}{R}', 'R', 5, 5, 'Flying, {R}: +1/+0 until end of turn.'),
                                                                (3, 'Lightning Bolt', 'Alpha', 'INSTANT', 'COMMON', '{R}', 'R', NULL, NULL, 'Lightning Bolt deals 3 damage to any target.'),
                                                                (4, 'Sol Ring', 'Alpha', 'ARTIFACT', 'UNCOMMON', '{1}', '', NULL, NULL, '{T}: Add {C}{C}.'),
                                                                (5, 'Forest', 'Zendikar', 'LAND', 'COMMON', '', 'G', NULL, NULL, '{T}: Add {G}.');

-- 3. Opret Formater (Regelbøgerne)
INSERT INTO formats (format_name, min_deck_size, max_deck_size, max_copies_of_card, requires_commander, allowed_rarities) VALUES
                                                                ('Standard', 60, 250, 4, FALSE, 'ALL'),               -- Får format_id = 1
                                                                ('Commander', 100, 100, 1, TRUE, 'ALL'),              -- Får format_id = 2
                                                                ('Pauper', 60, 250, 4, FALSE, 'COMMON'),              -- Får format_id = 3
                                                                ('Artisan Commander', 100, 100, 1, TRUE, 'COMMON,UNCOMMON'); -- Får format_id = 4

-- 4. Opret Samlinger
INSERT INTO collections (user_id) VALUES
                                      (3), -- Samling ID 1 tilhører SpillerSøren
                                      (4); -- Samling ID 2 tilhører KortKaj

-- Opret Byttelister (TILFØJ DISSE)
INSERT INTO tradecollections (user_id) VALUES
                                           (3),
                                           (4);

-- Opret Ønskelister (TILFØJ DISSE)
INSERT INTO wishcollections (user_id) VALUES
                                          (3),
                                          (4);

-- 5. Fyld kort i samlingerne (Fjernet 'card_condition' da det ikke findes i tabellen)
INSERT INTO collection_items (collection_id, card_id, quantity) VALUES
                                                                    (1, 2, 2), -- 2x Shivan Dragon
                                                                    (1, 3, 4), -- 4x Lightning Bolt
                                                                    (1, 4, 1); -- 1x Sol Ring

INSERT INTO collection_items (collection_id, card_id, quantity) VALUES
                                                                    (2, 1, 1),  -- 1x Black Lotus
                                                                    (2, 5, 20); -- 20x Forest

-- 6. Opret Decks (Tilføjet 'deck_format' for at matche NOT NULL-kolonnen)
INSERT INTO decks (user_id, format_id, deck_name, deck_format, commander_card_id) VALUES
                                                                 (3, 1, 'Sørens Burn Deck', 'Standard', NULL),
                                                                 (3, 2, 'Dragon Commander', 'Commander', 2);

-- 7. Opret Events (Tilføjet 'event_name' og justeret values)
INSERT INTO events (event_name, event_format, event_status, event_size, event_date, event_description) VALUES
                                                                ('Standard FNM', 'Standard', 'PLANNED', 16, '2026-06-01 18:00:00', 'Friday Night Magic turnering for alle.'),
                                                                ('Commander Night', 'Commander', 'ACTIVE', 32, '2026-05-08 19:00:00', 'Hygge og casual Commander spil.');

-- 8. Tilmeld brugere til Events (Fjernet 'status' da det ikke findes i event_registrations tabellen)
INSERT INTO event_registrations (event_id, user_id) VALUES
                                                        (2, 3),
                                                        (2, 4);

-- 9. Opret en Byttehandel (Trade)
INSERT INTO trades (initiator_user_id, receiver_user_id, trade_status, trade_date) VALUES
    (3, 4, 'PENDING', '2026-05-05 13:00:00');

-- 10. Tilføj kort til byttehandlen
INSERT INTO trade_items (trade_id, card_id, quantity, is_offered_by_initiator) VALUES
                                                                                   (1, 2, 1, TRUE),  -- Søren tilbyder 1x Shivan Dragon
                                                                                   (1, 1, 1, FALSE); -- Kaj forventes at give 1x Black Lotus