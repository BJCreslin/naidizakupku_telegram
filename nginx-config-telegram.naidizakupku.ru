server {
    server_name telegram.naidizakupku.ru;

    # для ACME challenge
    location /.well-known/acme-challenge/ {
        root /var/www/letsencrypt;
        try_files $uri =404;
    }

    # Proxy на backend API (Spring Boot)
    # Должен быть перед location / для правильного приоритета
    location /api/ {
        proxy_pass          http://127.0.0.1:8080/api/;
        proxy_set_header    Host $host;
        proxy_set_header    X-Real-IP $remote_addr;
        proxy_set_header    X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header    X-Forwarded-Proto $scheme;
        proxy_http_version  1.1;
        proxy_set_header    Upgrade $http_upgrade;
        proxy_set_header    Connection "upgrade";
        proxy_read_timeout  360;
        
        # Увеличиваем размер буфера для больших запросов
        proxy_buffering off;
        proxy_request_buffering off;
    }

    # Админка (SPA) проксируется на Spring Boot
    # Должен быть перед location / для правильного приоритета
    location /admin/ {
        proxy_pass          http://127.0.0.1:8080/admin/;
        proxy_set_header    Host $host;
        proxy_set_header    X-Real-IP $remote_addr;
        proxy_set_header    X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header    X-Forwarded-Proto $scheme;
        proxy_http_version  1.1;
        proxy_set_header    Upgrade $http_upgrade;
        proxy_set_header    Connection "upgrade";
        proxy_read_timeout  360;
    }
    
    # Редирект /admin на /admin/ для корректной обработки
    location = /admin {
        return 301 /admin/;
    }

    # Kafka UI (Kafdrop) проксируется на локальный порт 9000 и защищён basic auth
    # Должен быть перед location / для правильного приоритета
    location /kafka/ {
        auth_basic "Kafka UI";
        auth_basic_user_file /etc/nginx/.htpasswd;
        proxy_pass          http://127.0.0.1:9000/;
        proxy_set_header    Host $host;
        proxy_set_header    X-Real-IP $remote_addr;
        proxy_set_header    X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header    X-Forwarded-Proto $scheme;
        proxy_http_version  1.1;
        proxy_set_header    Upgrade $http_upgrade;
        proxy_set_header    Connection "upgrade";
        proxy_read_timeout  360;
    }

    # Статические файлы с кешированием (JS, CSS, изображения и т.д.)
    # Должен быть перед location / для правильного приоритета
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot|map)$ {
        root /var/www/telegram;
        expires 1y;
        add_header Cache-Control "public, immutable";
        access_log off;
    }

    # Не кешируем HTML файлы для обновлений
    location = /index.html {
        root /var/www/telegram;
        add_header Cache-Control "no-cache, no-store, must-revalidate";
        add_header Pragma "no-cache";
        add_header Expires "0";
    }

    # Статические файлы админ-панели (SPA)
    # Должен быть последним, так как это общий путь
    location / {
        root /var/www/telegram;
        index index.html;
        
        # Для SPA: если файл не найден, отдаем index.html
        # Это позволяет React Router обрабатывать маршруты на клиенте
        try_files $uri $uri/ /index.html;
    }

    listen 443 ssl; # managed by Certbot
    ssl_certificate /etc/letsencrypt/live/telegram.naidizakupku.ru/fullchain.pem; # managed by Certbot
    ssl_certificate_key /etc/letsencrypt/live/telegram.naidizakupku.ru/privkey.pem; # managed by Certbot
    include /etc/letsencrypt/options-ssl-nginx.conf; # managed by Certbot
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem; # managed by Certbot
}

server {
    if ($host = telegram.naidizakupku.ru) {
        return 301 https://$host$request_uri;
    } # managed by Certbot

    listen 80;
    server_name telegram.naidizakupku.ru;
    return 404; # managed by Certbot
}

