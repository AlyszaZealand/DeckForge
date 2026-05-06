-- ==========================================
-- TEST DATA FOR DECKFORGE
-- ==========================================

-- 1. Opret Brugere
INSERT INTO users (username, email, password_hash, user_role) VALUES
                            ('AdminAnders', 'anders@deckforge.dk', 'admin123', 'ADMIN'),
                            ('EventEva', 'eva@deckforge.dk', 'event123', 'ORGANIZER'),
                            ('SpillerSøren', 'søren@deckforge.dk', 'spiller123', 'MEMBER'),
                            ('KortKaj', 'kaj@deckforge.dk', 'kaj123', 'MEMBER');


-- 2. Opret Kort (Kataloget)
INSERT INTO cards (card_id, card_name, card_set, card_type, card_rarity, mana_cost, color_identity, power, health, description) VALUES
                             (1, 'Black Lotus', 'Alpha', 'ARTIFACT', 'MYTHICRARE', '{0}', '', NULL, NULL, 'Adds 3 mana of any single color.'),
                             (2, 'Shivan Dragon', 'Alpha', 'CREATURE', 'RARE', '{4}{R}{R}', 'R', 5, 5, 'Flying, {R}: +1/+0 until end of turn.'),
                             (3, 'Lightning Bolt', 'Alpha', 'INSTANT', 'COMMON', '{R}', 'R', NULL, NULL, 'Lightning Bolt deals 3 damage to any target.'),
                             (4, 'Sol Ring', 'Alpha', 'ARTIFACT', 'UNCOMMON', '{1}', '', NULL, NULL, '{T}: Add {C}{C}.'),
                             (5, 'Forest', 'Zendikar', 'LAND', 'COMMON', '', 'G', NULL, NULL, '{T}: Add {G}.');

-- 3. Opret Formater (Regelbøgerne) -- NY!
INSERT INTO formats (format_name, min_deck_size, max_deck_size, max_copies_of_card, requires_commander, allowed_rarities) VALUES
                             ('Standard', 60, 250, 4, FALSE, 'ALL'),               -- Får format_id = 1
                             ('Commander', 100, 100, 1, TRUE, 'ALL'),              -- Får format_id = 2
                             ('Pauper', 60, 250, 4, FALSE, 'COMMON'),              -- Får format_id = 3
                             ('Artisan Commander', 100, 100, 1, TRUE, 'COMMON,UNCOMMON'); -- Får format_id = 4


-- 4. Opret Samlinger
-- Søren og Kaj får en samling
INSERT INTO collections (user_id) VALUES
                            (3), -- Samling ID 1 tilhører SpillerSøren
                            (4); -- Samling ID 2 tilhører KortKaj


-- 5. Fyld kort i samlingerne (Collection Items)
INSERT INTO collection_items (collection_id, card_id, quantity, card_condition) VALUES
                            (1, 2, 2), -- 2x Shivan Dragon
                            (1, 3, 4), -- 4x Lightning Bolt
                            (1, 4, 1); -- 1x Sol Ring


INSERT INTO collection_items (collection_id, card_id, quantity, card_condition) VALUES
                            (2, 1, 1),  -- 1x Black Lotus
                            (2, 5, 20); -- 20x Forest


-- 6. Opret Decks
-- Vi bruger 'deck_format' som discriminator-kolonnen
INSERT INTO decks (user_id, format_id, deck_name, commander_card_id) VALUES
                            (3, 1, 'Sørens Burn Deck', NULL), -- Format 1 er Standard
                            (3, 2, 'Dragon Commander', 2);    -- Format 2 er Commander (Shivan Dragon som commander)


-- 7. Opret Events
-- Et event der er planlagt, og et der er aktivt
INSERT INTO events (event_format, event_status, event_size, event_date) VALUES
                            ('Standard FNM', 'PLANNED', 16, '2026-06-01 18:00:00'),
                            ('Commander Night', 'ACTIVE', 32, '2026-05-08 19:00:00');


-- 8. Tilmeld brugere til Events (Event Registrations)
-- Søren og Kaj skal med til Commander Night
INSERT INTO event_registrations (event_id, user_id, status) VALUES
                            (2, 3),
                            (2, 4);


-- 9. Opret en Byttehandel (Trade)
-- Søren prøver at bytte sig til Kajs Black Lotus
INSERT INTO trades (initiator_user_id, receiver_user_id, trade_status, trade_date) VALUES
                            (3, 4, 'PENDING', '2026-05-05 13:00:00');

-- Tilføj kort til byttehandlen
INSERT INTO trade_items (trade_id, card_id, quantity, is_offered_by_initiator) VALUES
                            (1, 2, 1, TRUE),  -- Søren tilbyder 1x Shivan Dragon
                            (1, 1, 1, FALSE); -- Kaj forventes at give 1x Black Lotus