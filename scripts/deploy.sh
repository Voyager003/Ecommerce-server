#!/bin/bash
set -e

# =============================================================================
# E-commerce Backend Deployment Script
# Usage: ./deploy.sh [OPTIONS]
# =============================================================================

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
DOCKER_IMAGE="${DOCKER_USERNAME:-ecommerce}/ecommerce-backend"
CONTAINER_NAME="ecommerce-backend"
PORT="8080"

# Functions
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_env() {
    log_info "Checking environment variables..."

    required_vars=("DB_HOST" "DB_NAME" "DB_USERNAME" "DB_PASSWORD" "JWT_SECRET")

    for var in "${required_vars[@]}"; do
        if [ -z "${!var}" ]; then
            log_error "Required environment variable $var is not set"
            exit 1
        fi
    done

    log_info "All required environment variables are set"
}

pull_image() {
    log_info "Pulling latest Docker image: $DOCKER_IMAGE:latest"
    docker pull "$DOCKER_IMAGE:latest"
}

stop_container() {
    if docker ps -q -f name="$CONTAINER_NAME" | grep -q .; then
        log_info "Stopping existing container: $CONTAINER_NAME"
        docker stop "$CONTAINER_NAME"
    fi

    if docker ps -aq -f name="$CONTAINER_NAME" | grep -q .; then
        log_info "Removing existing container: $CONTAINER_NAME"
        docker rm "$CONTAINER_NAME"
    fi
}

start_container() {
    log_info "Starting new container: $CONTAINER_NAME"

    docker run -d \
        --name "$CONTAINER_NAME" \
        --restart unless-stopped \
        -p "$PORT:8080" \
        -e SPRING_PROFILES_ACTIVE=prod \
        -e DB_HOST="$DB_HOST" \
        -e DB_NAME="$DB_NAME" \
        -e DB_USERNAME="$DB_USERNAME" \
        -e DB_PASSWORD="$DB_PASSWORD" \
        -e JWT_SECRET="$JWT_SECRET" \
        "$DOCKER_IMAGE:latest"

    log_info "Container started successfully"
}

health_check() {
    log_info "Waiting for application to start..."

    max_attempts=30
    attempt=1

    while [ $attempt -le $max_attempts ]; do
        if curl -sf "http://localhost:$PORT/actuator/health" > /dev/null 2>&1; then
            log_info "Application is healthy!"
            return 0
        fi

        log_warn "Attempt $attempt/$max_attempts - Application not ready yet..."
        sleep 5
        attempt=$((attempt + 1))
    done

    log_error "Application failed to start within expected time"
    docker logs "$CONTAINER_NAME" --tail 50
    return 1
}

cleanup() {
    log_info "Cleaning up old Docker images..."
    docker image prune -f
}

show_status() {
    log_info "Container Status:"
    docker ps -f name="$CONTAINER_NAME"

    log_info "Recent Logs:"
    docker logs "$CONTAINER_NAME" --tail 20
}

# Main
main() {
    log_info "Starting deployment..."

    check_env
    pull_image
    stop_container
    start_container
    health_check
    cleanup
    show_status

    log_info "Deployment completed successfully!"
}

# Parse arguments
case "${1:-deploy}" in
    deploy)
        main
        ;;
    stop)
        stop_container
        ;;
    status)
        show_status
        ;;
    logs)
        docker logs "$CONTAINER_NAME" -f
        ;;
    health)
        health_check
        ;;
    *)
        echo "Usage: $0 {deploy|stop|status|logs|health}"
        exit 1
        ;;
esac
