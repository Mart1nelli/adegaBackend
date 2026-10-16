INSERT INTO categories (name, description)
VALUES
    ('Vinhos', 'Rótulos tintos, brancos e rosés selecionados'),
    ('Espumantes', 'Espumantes para celebrações e ocasiões especiais'),
    ('Destilados', 'Destilados clássicos e contemporâneos'),
    ('Cervejas', 'Cervejas artesanais e especiais')
ON CONFLICT (name) DO NOTHING;

INSERT INTO payment_methods (name, description)
VALUES
    ('Cartão de crédito', 'Pagamento com cartão de crédito em ambiente de teste'),
    ('PIX', 'Pagamento instantâneo via PIX em ambiente de teste'),
    ('Boleto', 'Boleto bancário em ambiente de teste')
ON CONFLICT DO NOTHING;

INSERT INTO suppliers (name, email, phone, address)
VALUES
    ('Lume Selection', 'contato@lume.example', '+55 11 0000-0000', 'São Paulo - SP')
ON CONFLICT DO NOTHING;

INSERT INTO products (name, description, price, stock, image_url, category_id, supplier_id)
SELECT
    seed.name,
    seed.description,
    seed.price,
    seed.stock,
    seed.image_url,
    categories.id,
    suppliers.id
FROM (
    VALUES
        ('Reserva Tinto', 'Vinho tinto encorpado para momentos especiais', 89.90, 24, 'https://images.unsplash.com/photo-1510812431401-41d2bd2722f3'),
        ('Brut Reserve', 'Espumante brut fresco e elegante', 119.90, 18, 'https://images.unsplash.com/photo-1551538827-9c037cb4f32a'),
        ('London Dry Gin', 'Gin clássico com notas botânicas marcantes', 149.90, 12, 'https://images.unsplash.com/photo-1551024709-8f23befc6f87'),
        ('IPA Artesanal', 'Cerveja IPA com amargor equilibrado', 24.90, 36, 'https://images.unsplash.com/photo-1515003197210-e0cd71810b5f')
) AS seed(name, description, price, stock, image_url)
JOIN categories ON categories.name = CASE seed.name
    WHEN 'Reserva Tinto' THEN 'Vinhos'
    WHEN 'Brut Reserve' THEN 'Espumantes'
    WHEN 'London Dry Gin' THEN 'Destilados'
    WHEN 'IPA Artesanal' THEN 'Cervejas'
END
JOIN suppliers ON suppliers.name = 'Lume Selection'
WHERE NOT EXISTS (
    SELECT 1 FROM products existing WHERE existing.name = seed.name
);
