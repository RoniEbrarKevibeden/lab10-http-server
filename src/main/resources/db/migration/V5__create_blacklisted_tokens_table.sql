-- Create blacklisted_tokens table for storing invalidated access tokens
CREATE TABLE IF NOT EXISTS blacklisted_tokens (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    token VARCHAR(500) NOT NULL UNIQUE,
    blacklisted_at TIMESTAMP NOT NULL,
    expiry_date TIMESTAMP NOT NULL
);

-- Create index for faster token lookup
CREATE INDEX IF NOT EXISTS idx_blacklisted_tokens_token ON blacklisted_tokens(token);
