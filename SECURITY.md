# Security Configuration Guide

## JWT Configuration

### Generate a Secure JWT Secret

For production deployment, you must generate a secure JWT secret key:

```bash
openssl rand -base64 32
```

### Environment Variables

Set the following environment variables in production:

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| `JWT_SECRET` | JWT signing key (min 32 characters) | **Yes** | None |
| `JWT_EXPIRATION` | Token expiration time in milliseconds | No | 86400000 (24h) |
| `TIDB_HOST` | TiDB host address | Yes | localhost |
| `TIDB_PORT` | TiDB port | No | 4000 |
| `TIDB_USERNAME` | Database username | Yes | root |
| `TIDB_PASSWORD` | Database password | Yes | (empty) |
| `CORS_ORIGINS` | Comma-separated allowed origins | Yes | http://localhost:5173 |

### Development Setup

For development, the application will use default values but JWT validation will fail if no secret is provided.

Set a development secret:

```bash
export JWT_SECRET="dev-secret-key-for-testing-only-min-32-chars"
```

Or create `application-dev.yml`:

```yaml
app:
  jwt:
    secret: "dev-secret-key-for-testing-only-min-32-chars"
    expiration: 86400000
```

### Production Deployment

1. Generate a secure JWT secret
2. Configure all required environment variables
3. Use `application-prod.yml` as a template
4. Never commit secrets to version control

Example production startup:

```bash
export JWT_SECRET="$(openssl rand -base64 32)"
export TIDB_HOST="your-tidb-host"
export TIDB_USERNAME="your-db-user"
export TIDB_PASSWORD="your-db-password"
export CORS_ORIGINS="https://your-domain.com"

java -jar backend.jar --spring.profiles.active=prod
```

### Security Best Practices

1. **Rotate JWT secrets regularly** (e.g., every 90 days)
2. **Use strong, randomly generated secrets**
3. **Enable HTTPS in production**
4. **Set appropriate CORS origins**
5. **Monitor for suspicious activity**
6. **Implement rate limiting**
7. **Use short token expiration for sensitive operations**
8. **Consider refresh token mechanism**
