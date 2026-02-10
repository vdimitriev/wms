# Docker Deployment Guide

## Prerequisites

- Docker Engine 20.10+
- Docker Compose 2.0+
- At least 2GB free disk space

## Quick Start

### 1. Build and Start Services

```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f wms-app
docker-compose logs -f postgres
```

### 2. Verify Services

```bash
# Check service status
docker-compose ps

# Check application health
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}
```

### 3. Send Test Sensor Data

```bash
# Temperature sensor (UDP port 3344)
echo "sensor_id=zone_a; value=25.0" | nc -u localhost 3344

# Humidity sensor (UDP port 3355)
echo "sensor_id=zone_b; value=45.0" | nc -u localhost 3355
```

### 4. Access Web Dashboard

Open your browser to:
- Web UI: http://localhost:8080
- Health Check: http://localhost:8080/actuator/health

## Configuration

### Environment Variables

Copy the example environment file and customize:

```bash
cp .env.example .env
nano .env
```

Available environment variables:

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | postgres | PostgreSQL host |
| `DB_PORT` | 5432 | PostgreSQL port |
| `DB_NAME` | wmsdb | Database name |
| `DB_USER` | postgres | Database user |
| `DB_PASSWORD` | postgres | Database password |
| `SENSOR_TEMPERATURE_PORT` | 3344 | UDP port for temperature sensors |
| `SENSOR_HUMIDITY_PORT` | 3355 | UDP port for humidity sensors |
| `MONITORING_TEMPERATURE_THRESHOLD` | 35.0 | Temperature alarm threshold (°C) |
| `MONITORING_HUMIDITY_THRESHOLD` | 50.0 | Humidity alarm threshold (%) |

### Using Custom Environment File

```bash
docker-compose --env-file .env up -d
```

## Production Deployment

### Security Recommendations

1. **Change default credentials**:
   ```bash
   DB_USER=wms_prod_user
   DB_PASSWORD=strong_random_password_here
   ```

2. **Use secrets management** (Docker Swarm or Kubernetes):
   ```bash
   echo "strong_password" | docker secret create db_password -
   ```

3. **Limit exposed ports**:
   - Remove PostgreSQL port exposure if not needed externally
   - Use a reverse proxy (nginx) for HTTPS

### Performance Tuning

Edit `docker-compose.yml` to add resource limits:

```yaml
services:
  wms-app:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
        reservations:
          cpus: '1'
          memory: 1G
```

### Database Backups

```bash
# Create backup
docker exec wms-postgres pg_dump -U postgres wmsdb > backup_$(date +%Y%m%d).sql

# Restore backup
docker exec -i wms-postgres psql -U postgres wmsdb < backup_20260210.sql
```

## Service Management

### Start Services
```bash
docker-compose up -d
```

### Stop Services
```bash
docker-compose down
```

### Restart Services
```bash
docker-compose restart
```

### Stop and Remove All Data
```bash
docker-compose down -v
```

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f wms-app

# Last 100 lines
docker-compose logs --tail=100 wms-app
```

### Scale Application (if needed)
```bash
docker-compose up -d --scale wms-app=3
```

## Troubleshooting

### Application won't start

1. Check logs:
   ```bash
   docker-compose logs wms-app
   ```

2. Verify PostgreSQL is running:
   ```bash
   docker-compose ps postgres
   docker-compose logs postgres
   ```

3. Check database connection:
   ```bash
   docker exec -it wms-postgres psql -U postgres -d wmsdb
   ```

### Database connection errors

1. Verify network connectivity:
   ```bash
   docker exec wms-app ping postgres
   ```

2. Check environment variables:
   ```bash
   docker exec wms-app env | grep DB_
   ```

### UDP ports not working

1. Verify ports are exposed:
   ```bash
   docker-compose ps
   netstat -uln | grep -E "3344|3355"
   ```

2. Test UDP connectivity:
   ```bash
   # From host machine
   echo "sensor_id=test; value=25.0" | nc -u localhost 3344
   
   # Check application logs for received message
   docker-compose logs -f wms-app
   ```

### Reset Everything

```bash
# Stop all services
docker-compose down -v

# Remove images
docker-compose down --rmi all

# Rebuild from scratch
docker-compose build --no-cache
docker-compose up -d
```

## Monitoring

### Check Resource Usage

```bash
# All containers
docker stats

# Specific container
docker stats wms-app
```

### Database Connections

```bash
docker exec -it wms-postgres psql -U postgres -d wmsdb -c \
  "SELECT count(*) FROM pg_stat_activity WHERE datname = 'wmsdb';"
```

### Application Metrics

Access metrics at:
- Health: http://localhost:8080/actuator/health
- Info: http://localhost:8080/actuator/info

## Development vs Production

### Development Mode
```bash
# Use H2 in-memory database
export SPRING_PROFILES_ACTIVE=dev
./mvnw spring-boot:run
```

### Production Mode with Docker
```bash
# Use PostgreSQL with Docker Compose
docker-compose up -d
```

## Network Architecture

```
┌─────────────────────────────────────────┐
│         Docker Network (wms-network)     │
├─────────────────────────────────────────┤
│                                         │
│  ┌──────────────┐    ┌──────────────┐  │
│  │   postgres   │    │   wms-app    │  │
│  │  (internal)  │◄───┤  (service)   │  │
│  └──────────────┘    └──────┬───────┘  │
│                             │          │
└─────────────────────────────┼──────────┘
                              │
         ┌────────────────────┼────────────────────┐
         │                    │                    │
    Port 8080 (HTTP)    Port 3344 (UDP)    Port 3355 (UDP)
    Web Dashboard       Temperature         Humidity
                       Sensors              Sensors
```

## Additional Resources

- Application logs: `/app/logs/` (inside container)
- PostgreSQL data: Docker volume `postgres_data`
- Configuration: `application.properties`

## Support

For issues or questions:
- Check logs: `docker-compose logs`
- Review documentation: `README.md`
- Database schema: `src/main/resources/db/migration/`

