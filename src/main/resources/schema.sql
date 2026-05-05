CREATE DATABASE IF NOT EXISTS deckforge_db;
USE deckforge_db;

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
                       card_id INT PRIMARY KEY,
                       card_name VARCHAR(255) NOT NULL,
                       card_set VARCHAR(100),
                       card_type ENUM('ARTIFACT', 'CREATURE', 'ENCHANTMENT', 'LAND', 'INSTANT', 'SORCERY', 'PLANESWALKER'),
                       card_rarity ENUM('COMMON', 'UNCOMMON', 'RARE', 'MYTHICRARE'),
                       mana_cost VARCHAR(50),
                       power INT,
                       health INT,
                       description TEXT
);

-- ==========================================
-- 2. SAMLINGER & LISTER
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

CREATE TABLE wishlists (
                        wishlist_id INT AUTO_INCREMENT PRIMARY KEY,
                        user_id INT NOT NULL UNIQUE,
                        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE wishlist_items (
                        item_id INT AUTO_INCREMENT PRIMARY KEY,
                        wishlist_id INT NOT NULL,
                        card_id INT NOT NULL,
                        FOREIGN KEY (wishlist_id) REFERENCES wishlists(wishlist_id) ON DELETE CASCADE,
                        FOREIGN KEY (card_id) REFERENCES cards(card_id)
);

-- ==========================================
-- 3. DECKBUILDING
-- ==========================================

CREATE TABLE decks (
                       deck_id INT AUTO_INCREMENT PRIMARY KEY,
                       user_id INT NOT NULL,
                       deck_name VARCHAR(100) NOT NULL,
                       commander_card_id INT NULL, -- Kun relevant hvis format = COMMANDER
                       FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                       FOREIGN KEY (commander_card_id) REFERENCES cards(card_id)
);

CREATE TABLE deck_items (
                        item_id INT AUTO_INCREMENT PRIMARY KEY,
                        deck_id INT NOT NULL,
                        card_id INT NOT NULL,
                        quantity INT DEFAULT 1,
                        FOREIGN KEY (deck_id) REFERENCES decks(deck_id) ON DELETE CASCADE,
                        FOREIGN KEY (card_id) REFERENCES cards(card_id)
);

-- ==========================================
-- 4. EVENTS & TRADING
-- ==========================================

CREATE TABLE events (
                        event_id INT AUTO_INCREMENT PRIMARY KEY,
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