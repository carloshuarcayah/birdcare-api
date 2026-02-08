-- Data for testing

INSERT INTO categories (name, description, active) VALUES
('Live Birds', 'Exotic and domestic feathered friends', true),
('Bird Food', 'High-quality seeds and supplements', true),
('Toys & Accessories', 'Swings, mirrors, and bells', true);


INSERT INTO products (category_id, name, description, price, stock, active) VALUES
(1, 'Lovebird', 'Small, colorful, and social parrot.', 45.00, 10, true),
(1, 'Cockatiel', 'Friendly bird with a distinctive crest.', 85.00, 5, true),
(2, 'Premium Mix 1kg', 'Nutritious seed blend for all birds.', 12.50, 50, true),
(2, 'Calcium Block', 'Essential minerals for beak health.', 4.50, 100, true),
(3, 'Wooden Swing', 'Natural wood swing for exercise.', 7.99, 25, true),
(3, 'Travel Cage', 'Safe and portable carrier.', 60.00, 3, true);

INSERT INTO users (email, password, name, last_name, phone, role, active) VALUES
('testuser@example.com', 'Encrypted_password123', 'John', 'Doe', '987654321', 'CUSTOMER', true);