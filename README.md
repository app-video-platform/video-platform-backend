# Video Platform Backend

A **Spring Boot** application deployed via **Dokku** on a DigitalOcean Droplet, using **PostgreSQL** as the database.

## Table of Contents
1. [Overview](#overview)  
2. [Prerequisites](#prerequisites)  
3. [Droplet Setup](#droplet-setup)  
4. [Install Dokku](#install-dokku)  
5. [Configure PostgreSQL](#configure-postgresql)  
6. [Deploying the App](#deploying-the-app)  
7. [Port Mapping & Nginx](#port-mapping--nginx)  
8. [Common Issues](#common-issues)  
9. [Local Development](#local-development)  
10. [License](#license)

---

## Overview
This project is a **Spring Boot** application designed to handle video platform functionality. It’s deployed to a **DigitalOcean Droplet** using **Dokku**, which provides a Heroku-like experience. **PostgreSQL** is used as the primary database.

---

## Prerequisites
- **DigitalOcean account**  
- **Git installed** locally  
- **SSH key** added to DigitalOcean (for secure deployments)  
- Basic knowledge of **Linux CLI**

---

## Droplet Setup
1. DigitalOcean with **Ubuntu 22.04 LTS**.
2. **1GB RAM** (2GB+ is better if you run PostgreSQL on the same machine).
3. Once created, **SSH into your Droplet**:
   ```bash
   ssh root@YOUR_DROPLET_IP

---

## Install Dokku
1. Download and run Dokku’s installer (replace v0.35.15 with the latest version if needed):
  ```bash
  wget https://raw.githubusercontent.com/dokku/dokku/v0.35.15/bootstrap.sh
  sudo DOKKU_TAG=v0.35.15 bash bootstrap.sh
  ```
2. Follow on-screen instructions. If prompted for a domain, you can set it later or skip.
3. Check Dokku version:
  ```bash
  dokku version
  ```

---

## Configure PostgreSQL
1. Install the Postgres plugin (if not already installed):
  ```bash
  dokku plugin:install https://github.com/dokku/dokku-postgres.git
  ```
3. Create a Postgres database:
  ```bash
  dokku postgres:create video-platform-db
  ```
3. Link the database to your app:
  ```bash
  dokku postgres:link video-platform-db video-platform
  ```

---

## Deploying the App
1. Create a Dokku app:
  ```bash
  dokku apps:create video-platform
  ```
2. On your local machine, add Dokku as a remote:
  ```bash
  git remote add dokku dokku@YOUR_DROPLET_IP:video-platform
  ```
3. Push your code to deploy:
  ```bash
  git push dokku main
  ```
4. Check logs to ensure it started:
  ```bash
  dokku logs video-platform -t
  ```
5. (Optional) Set Java version in system.properties (to avoid auto-upgrading to latest JDK):
  ```bash
  # system.properties
  java.runtime.version=17
  ```

---

## Port Mapping & Nginx
Dokku uses the host’s Nginx to route traffic to your Docker containers.
1. Ensure system Nginx is running:
  ```bash
  sudo systemctl enable nginx
  sudo systemctl start nginx
  ```
2. Disable the default Nginx site if you see the “Welcome to nginx!” page:
  ```bash
  rm /etc/nginx/sites-enabled/default
  systemctl reload nginx
  ```
3. Map port 80 → 8080 (Spring Boot defaults to 8080):
  ```bash
  dokku ports:set video-platform http:80:8080
  dokku ps:restart video-platform
  ```
4. Visit http://YOUR_DROPLET_IP to see your app.


## Common Issues
1. Nginx default page:
  - Make sure system Nginx is running and the default site is disabled.
  - Dokku will generate its own Nginx configs and reload them.
2. Whitelabel Error Page (404)
  - Spring Boot is running but no endpoint is mapped to /.
  - Create a controller for "/" or use a different path.
3. Out of Memory
  - If you have a small Droplet (1GB), consider creating a swap file:
  ```bash
  fallocate -l 1G /swapfile
  chmod 600 /swapfile
  mkswap /swapfile
  swapon /swapfile
  echo '/swapfile none swap sw 0 0' | tee -a /etc/fstab
  ```
  - Optimize your PostgreSQL memory settings or upgrade your Droplet.
4. Java version changes
  - Set java.runtime.version=17 in system.properties to avoid Dokku auto-upgrading to JDK 21 or newer.
5. Port not mapped
  - Verify with:
  ```bash
  dokku proxy:report video-platform
  ```
  - If missing, run:
  ```bash
  dokku ports:set video-platform http:80:8080
  ```

---

## Local Development
1. Run locally with:
  ```bash
  ./mvnw spring-boot:run
  ```
2. Access at http://localhost:8080.

---

## License
Copyright (c) 2025 Tamas Tyukodi

---
## Contact
tyukoditamas1995@gmail.com
