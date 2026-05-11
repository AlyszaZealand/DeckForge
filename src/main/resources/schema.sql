-- CREATE DATABASE IF NOT EXISTS deckforge_db;
-- USE deckforge_db;

-- 1. Slå referencetjek fra midlertidigt
SET FOREIGN_KEY_CHECKS = 0;

-- 2. Slet alle tabeller, hvis de findes (Sørg for at alle jeres tabeller står her)
DROP TABLE IF EXISTS collection_items;
DROP TABLE IF EXISTS tradecollection_items;
DROP TABLE IF EXISTS wishcollection_items;
DROP TABLE IF EXISTS deck_items;
DROP TABLE IF EXISTS event_registrations;
DROP TABLE IF EXISTS trade_items;
DROP TABLE IF EXISTS trades;
DROP TABLE IF EXISTS decks;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS collections;
DROP TABLE IF EXISTS tradecollections;
DROP TABLE IF EXISTS wishcollections;
DROP TABLE IF EXISTS cards;
DROP TABLE IF EXISTS formats;
DROP TABLE IF EXISTS users;

-- 3. Slå referencetjek til igen! (Meget vigtigt)
SET FOREIGN_KEY_CHECKS = 1;

-- ==========================================
-- 1. IDENTITET & KATALOG
-- ==========================================

CREATE TABLE users (
                       user_id INT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       user_role ENUM('MEMBER', 'ADMIN', 'ORGANIZER') DEFAULT 'MEMBER'
);

CREATE TABLE cards (
                       card_id INT AUTO_INCREMENT PRIMARY KEY,
                       card_name VARCHAR(255) NOT NULL,
                       card_set VARCHAR(100),
                       card_type ENUM('ARTIFACT', 'CREATURE', 'ENCHANTMENT', 'LAND', 'INSTANT', 'SORCERY', 'PLANESWALKER'),
                       card_rarity ENUM('COMMON', 'UNCOMMON', 'RARE', 'MYTHICRARE'),
                       mana_cost VARCHAR(50),
                       color_identity VARCHAR(20),
                       power INT,
                       health INT,
                       description TEXT
);

-- ==========================================
-- 2. FORMATER & REGLER (NY!)
-- ==========================================
CREATE TABLE formats (
                         format_id INT AUTO_INCREMENT PRIMARY KEY,
                         format_name VARCHAR(50) NOT NULL,
                         min_deck_size INT NOT NULL,
                         max_deck_size INT NOT NULL,
                         max_copies_of_card INT NOT NULL,
                         requires_commander BOOLEAN NOT NULL DEFAULT FALSE,
                         allowed_rarities VARCHAR(100) NOT NULL DEFAULT 'ALL'
);

-- ==========================================
-- 3. SAMLINGER & LISTER
-- ==========================================

CREATE TABLE collections (
                        collection_id INT AUTO_INCREMENT PRIMARY KEY,
                        user_id INT NOT NULL UNIQUE, -- UNIQUE sikrer 1-til-1 relationen mellem User og Collection
                        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE collection_items (
                        item_id INT AUTO_INCREMENT PRIMARY KEY,
                        collection_id INT NOT NULL,
                        card_id INT NOT NULL,
                        quantity INT DEFAULT 1,
                        FOREIGN KEY (collection_id) REFERENCES collections(collection_id) ON DELETE CASCADE,
                        FOREIGN KEY (card_id) REFERENCES cards(card_id)
);

CREATE TABLE wishcollections (
                        wishcollection_id INT AUTO_INCREMENT PRIMARY KEY,
                        user_id INT NOT NULL UNIQUE,
                        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE wishcollection_items (
                        item_id INT AUTO_INCREMENT PRIMARY KEY,
                        wishcollection_id INT NOT NULL,
                        card_id INT NOT NULL,
                        FOREIGN KEY (wishcollection_id) REFERENCES wishcollections(wishcollection_id) ON DELETE CASCADE,
                        FOREIGN KEY (card_id) REFERENCES cards(card_id)
);

CREATE TABLE tradecollections (
                        tradecollection_id INT AUTO_INCREMENT PRIMARY KEY,
                        user_id INT NOT NULL UNIQUE, -- UNIQUE sikrer 1-til-1 relationen mellem User og Collection
                        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE tradecollection_items (
                        item_id INT AUTO_INCREMENT PRIMARY KEY,
                        tradecollection_id INT NOT NULL,
                        card_id INT NOT NULL,
                        quantity INT DEFAULT 1,
                        FOREIGN KEY (tradecollection_id) REFERENCES tradecollections(tradecollection_id) ON DELETE CASCADE,
                        FOREIGN KEY (card_id) REFERENCES cards(card_id)
);

-- ==========================================
-- 4. DECKBUILDING
-- ==========================================

CREATE TABLE decks (
                       deck_id INT AUTO_INCREMENT PRIMARY KEY,
                       user_id INT NOT NULL,
                       format_id INT NOT NULL,
                       deck_name VARCHAR(100) NOT NULL,
                       deck_format VARCHAR(50) NOT NULL,
                       commander_card_id INT NULL, -- Kun relevant hvis format = COMMANDER
                       FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                       FOREIGN KEY (format_id) REFERENCES formats(format_id),
                       FOREIGN KEY (commander_card_id) REFERENCES cards(card_id)
);

CREATE TABLE deck_items (
                        item_id INT AUTO_INCREMENT PRIMARY KEY,
                        deck_id INT NOT NULL,
                        card_id INT NOT NULL,
                        quantity INT DEFAULT 1,
                        FOREIGN KEY (deck_id) REFERENCES decks(deck_id) ON DELETE CASCADE,
                        FOREIGN KEY (card_id) REFERENCES cards(card_id),
                        UNIQUE(deck_id, card_id)
);

-- ==========================================
-- 5. EVENTS & TRADING
-- ==========================================

CREATE TABLE events (
                        event_id INT AUTO_INCREMENT PRIMARY KEY,
                        event_name VARCHAR(100),
                        event_format VARCHAR(100),
                        event_status ENUM('PLANNED', 'ACTIVE', 'COMPLETED', 'CANCELLED') DEFAULT 'PLANNED',
                        event_size INT NOT NULL,
                        event_date DATETIME NOT NULL,
                        event_description VARCHAR(500)
);

CREATE TABLE event_registrations (
                        event_id INT NOT NULL,
                        user_id INT NOT NULL,
                        PRIMARY KEY (event_id, user_id),
                        FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE,
                        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE trades (
                        trade_id INT AUTO_INCREMENT PRIMARY KEY,
                        initiator_user_id INT NOT NULL,
                        receiver_user_id INT NOT NULL,
                        trade_status ENUM('PENDING', 'ACCEPTED', 'DECLINED', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
                        trade_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                        completed_date DATETIME NULL,
                        FOREIGN KEY (initiator_user_id) REFERENCES users(user_id),
                        FOREIGN KEY (receiver_user_id) REFERENCES users(user_id)
);

CREATE TABLE trade_items (
                        item_id INT AUTO_INCREMENT PRIMARY KEY,
                        trade_id INT NOT NULL,
                        card_id INT NOT NULL,
                        quantity INT DEFAULT 1,
                        is_offered_by_initiator BOOLEAN NOT NULL, -- True = Initiator giver, False = Receiver giver
                        FOREIGN KEY (trade_id) REFERENCES trades(trade_id) ON DELETE CASCADE,
                        FOREIGN KEY (card_id) REFERENCES cards(card_id)
);