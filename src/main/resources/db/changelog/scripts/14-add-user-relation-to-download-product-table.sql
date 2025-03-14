ALTER TABLE public.download_products
    ADD CONSTRAINT fk_download_products_user
        FOREIGN KEY (user_id)
            REFERENCES public.users (user_id)
            ON DELETE CASCADE;
