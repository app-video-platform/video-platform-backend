CREATE TABLE IF NOT EXISTS social_media_links (
                                                  id UUID PRIMARY KEY,
                                                  user_id UUID NOT NULL,
                                                  platform varchar NOT NULL,
                                                  url TEXT NOT NULL,
                                                  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                                  CONSTRAINT fk_social_user
                                                      FOREIGN KEY(user_id)
                                                          REFERENCES users(user_id)
                                                          ON DELETE CASCADE
);
