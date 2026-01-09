#!/bin/bash
set -e

# =============================================================================
# EC2 Initial Setup Script for E-commerce Backend
# Run this script on a fresh EC2 instance (Amazon Linux 2023)
# =============================================================================

echo "=========================================="
echo "E-commerce Backend - EC2 Initial Setup"
echo "=========================================="

# Update system
echo "[1/6] Updating system packages..."
sudo yum update -y

# Install Docker
echo "[2/6] Installing Docker..."
sudo yum install -y docker

# Start and enable Docker
echo "[3/6] Starting Docker service..."
sudo systemctl start docker
sudo systemctl enable docker

# Add current user to docker group
echo "[4/6] Adding user to docker group..."
sudo usermod -aG docker $USER

# Install useful tools
echo "[5/6] Installing additional tools..."
sudo yum install -y htop curl wget jq

# Create application directory
echo "[6/6] Creating application directory..."
mkdir -p ~/ecommerce
cd ~/ecommerce

# Create environment file template
cat > ~/ecommerce/.env.template << 'EOF'
# Database Configuration
DB_HOST=your-rds-endpoint.region.rds.amazonaws.com
DB_NAME=ecommerce
DB_USERNAME=admin
DB_PASSWORD=your-password

# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-must-be-at-least-256-bits-long

# Docker Configuration
DOCKER_USERNAME=your-dockerhub-username
EOF

echo ""
echo "=========================================="
echo "Setup Complete!"
echo "=========================================="
echo ""
echo "Next steps:"
echo "1. Log out and log back in (for docker group to take effect)"
echo "2. Copy .env.template to .env and fill in your values:"
echo "   cp ~/ecommerce/.env.template ~/ecommerce/.env"
echo "   nano ~/ecommerce/.env"
echo ""
echo "3. Source the environment file:"
echo "   source ~/ecommerce/.env"
echo ""
echo "4. Run the deployment script:"
echo "   ./deploy.sh"
echo ""
