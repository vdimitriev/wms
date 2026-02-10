# PostgreSQL & Docker Setup - Quick Reference

## What Was Changed

### 1. **PostgreSQL Configuration**
   - ✅ Updated `application.properties` to use PostgreSQL instead of H2
   - ✅ Added PostgreSQL R2DBC and JDBC drivers to `pom.xml`
   - ✅ Configured environment variables for flexible deployment

### 2. **Docker Setup**
   - ✅ Updated `Dockerfile` with multi-stage build
   - ✅ Created comprehensive `docker-compose.yml`
   - ✅ Added `.dockerignore` for efficient builds
   - ✅ Added health checks and monitoring

### 3. **Dependencies Added**
   - PostgreSQL driver (`postgresql`)
   - R2DBC PostgreSQL driver (`r2dbc-postgresql`)
   - Flyway PostgreSQL support (`flyway-database-postgresql`)
   - Spring Boot Actuator (for health checks)

## Quick Start with Docker

### Option 1: Docker Compose (Recommended)

```bash
# Start all services (PostgreSQL + Application)
docker-compose up -d

# View logs
docker-compose logs -f

# Check health
curl http://localhost:8080/actuator/health

# Test sensor data
echo "sensor_id=zone_a; value=25.0" | nc -u localhost 3344

# Stop services
docker-compose down
```

### Option 2: Local Development (with Docker PostgreSQL)

```bash
# Start only PostgreSQL
docker-compose up -d postgres

# Run application locally
./mvnw spring-boot:run

# Test the application
echo "sensor_id=zone_a; value=25.0" | nc -u localhost 3344
```

### Option 3: Build Docker Image Only

```bash
# Build image
docker build -t wms-app:latest .

# Run with external PostgreSQL
docker run -d \
  -e DB_HOST=your-postgres-host \
  -e DB_PORT=5432 \
  -e DB_NAME=wmsdb \
  -e DB_USER=postgres \
  -e DB_PASSWORD=postgres \
  -p 8080:8080 \
  -p 3344:3344/udp \
  -p 3355:3355/udp \
  wms-app:latest
```

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | localhost | PostgreSQL hostname |
| `DB_PORT` | 5432 | PostgreSQL port |
| `DB_NAME` | wmsdb | Database name |
| `DB_USER` | postgres | Database username |
| `DB_PASSWORD` | postgres | Database password |

### Ports

| Port | Protocol | Purpose |
|------|----------|---------|
| 8080 | TCP | Web UI and REST API |
| 3344 | UDP | Temperature sensor data |
| 3355 | UDP | Humidity sensor data |
| 5432 | TCP | PostgreSQL (dev only) |

## Database Migration

Flyway automatically runs migrations on startup. The schema is created from:
- `src/main/resources/db/migration/V1__initial_schema.sql`

### Tables Created

1. **sensor_measurements** - Stores all sensor readings
2. **alarm_events** - Stores alarm/threshold violations

## Testing

### Run Tests (uses H2 in-memory)
```bash
./mvnw test
```

### Run Tests with PostgreSQL
```bash
# Start PostgreSQL
docker-compose up -d postgres

# Run tests
./mvnw test -Dspring.profiles.active=test
```

## Architecture

```
┌─────────────────────────────────────────────────┐
│              Docker Compose                     │
├─────────────────────────────────────────────────┤
│                                                 │
│  ┌──────────────────┐      ┌───────────────┐   │
│  │   PostgreSQL     │◄─────┤   WMS App     │   │
│  │   Container      │      │   Container   │   │
│  │                  │      │               │   │
│  │ • Database       │      │ • Spring Boot │   │
│  │ • Port 5432      │      │ • R2DBC       │   │
│  │ • Persistent vol │      │ • Flyway      │   │
│  └──────────────────┘      └───────┬───────┘   │
│                                    │           │
└────────────────────────────────────┼───────────┘
                                     │
                  ┌──────────────────┼──────────────────┐
                  │                  │                  │
            Port 8080          Port 3344          Port 3355
            (Web UI)        (Temperature)       (Humidity)
```

## Production Deployment

### Security Checklist

- [ ] Change default database password
- [ ] Use environment variables (never hardcode credentials)
- [ ] Enable HTTPS (use reverse proxy like nginx)
- [ ] Limit database port exposure
- [ ] Configure firewall rules
- [ ] Set up monitoring and alerting
- [ ] Configure automated backups

### Backup Database

```bash
# Backup
docker exec wms-postgres pg_dump -U postgres wmsdb > backup.sql

# Restore
docker exec -i wms-postgres psql -U postgres wmsdb < backup.sql
```

## Troubleshooting

### Application won't start
```bash
# Check logs
docker-compose logs wms-app

# Check database connection
docker exec wms-app env | grep DB_
```

### Database connection failed
```bash
# Verify PostgreSQL is running
docker-compose ps postgres

# Test connection
docker exec -it wms-postgres psql -U postgres -d wmsdb
```

### UDP ports not receiving data
```bash
# Check ports are exposed
docker-compose ps

# Test UDP
echo "sensor_id=test; value=25.0" | nc -u localhost 3344

# Check logs
docker-compose logs -f wms-app
```

## Files Created/Modified

### New Files
- ✅ `docker-compose.yml` - Complete Docker Compose configuration
- ✅ `.dockerignore` - Docker build optimization
- ✅ `.env.example` - Environment variables template
- ✅ `DOCKER_DEPLOYMENT.md` - Comprehensive deployment guide
- ✅ `DOCKER_POSTGRES_SETUP.md` - This quick reference

### Modified Files
- ✅ `Dockerfile` - Multi-stage build with security
- ✅ `application.properties` - PostgreSQL configuration
- ✅ `pom.xml` - Added PostgreSQL dependencies

## Next Steps

1. **Start the application:**
   ```bash
   docker-compose up -d
   ```

2. **Verify it's working:**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

3. **Send test data:**
   ```bash
   echo "sensor_id=zone_a; value=25.0" | nc -u localhost 3344
   ```

4. **Open web dashboard:**
   ```
   http://localhost:8080
   ```

5. **Read full deployment guide:**
   ```
   See DOCKER_DEPLOYMENT.md for details
   ```

## Additional Resources

- 📖 Full Deployment Guide: `DOCKER_DEPLOYMENT.md`
- 📖 Application README: `README.md`
- 🐛 Database Schema: `src/main/resources/db/migration/V1__initial_schema.sql`
- ⚙️ Configuration: `src/main/resources/application.properties`

---

**Status:** ✅ Ready for deployment with Docker and PostgreSQL

