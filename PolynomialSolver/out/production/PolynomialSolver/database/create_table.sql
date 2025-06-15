CREATE TABLE IF NOT EXISTS solved_polynomials (
    id INT AUTO_INCREMENT PRIMARY KEY,
    polynomial VARCHAR(255) NOT NULL,
    solution TEXT NOT NULL,
    coefficients VARCHAR(255),
    degree INT,
    solved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 