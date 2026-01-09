# AWS Infrastructure Setup Guide

E-commerce 프로젝트를 AWS에 배포하기 위한 인프라 구성 가이드입니다.

## Architecture Overview

```
                    ┌─────────────────┐
                    │   Route 53      │
                    │   (DNS)         │
                    └────────┬────────┘
                             │
         ┌───────────────────┴───────────────────┐
         │                                       │
         ▼                                       ▼
┌─────────────────┐                   ┌─────────────────┐
│   CloudFront    │                   │   EC2 Instance  │
│   + S3 Bucket   │                   │   (Backend)     │
│   (Frontend)    │                   │                 │
└─────────────────┘                   └────────┬────────┘
                                               │
                                      ┌────────┴────────┐
                                      │   RDS MySQL     │
                                      │   (Database)    │
                                      └─────────────────┘
```

## Prerequisites

- AWS 계정 (Free Tier 사용 가능)
- AWS CLI 설치 및 구성
- 도메인 (선택사항)

---

## Step 1: VPC & Security Groups

### 1.1 Default VPC 사용 (권장 - 간단)

Free Tier에서는 Default VPC를 그대로 사용합니다.

### 1.2 Security Groups 생성

#### Backend Security Group (`ecommerce-backend-sg`)

| Type | Port | Source | Description |
|------|------|--------|-------------|
| SSH | 22 | My IP | SSH 접속 |
| HTTP | 80 | 0.0.0.0/0 | HTTP 접속 |
| HTTPS | 443 | 0.0.0.0/0 | HTTPS 접속 |
| Custom TCP | 8080 | 0.0.0.0/0 | Spring Boot |

```bash
aws ec2 create-security-group \
  --group-name ecommerce-backend-sg \
  --description "Security group for E-commerce backend"

# SSH
aws ec2 authorize-security-group-ingress \
  --group-name ecommerce-backend-sg \
  --protocol tcp --port 22 --cidr $(curl -s ifconfig.me)/32

# HTTP/HTTPS
aws ec2 authorize-security-group-ingress \
  --group-name ecommerce-backend-sg \
  --protocol tcp --port 80 --cidr 0.0.0.0/0

aws ec2 authorize-security-group-ingress \
  --group-name ecommerce-backend-sg \
  --protocol tcp --port 443 --cidr 0.0.0.0/0

# Spring Boot
aws ec2 authorize-security-group-ingress \
  --group-name ecommerce-backend-sg \
  --protocol tcp --port 8080 --cidr 0.0.0.0/0
```

#### Database Security Group (`ecommerce-db-sg`)

| Type | Port | Source | Description |
|------|------|--------|-------------|
| MySQL | 3306 | ecommerce-backend-sg | Backend에서만 접근 |

```bash
aws ec2 create-security-group \
  --group-name ecommerce-db-sg \
  --description "Security group for E-commerce database"

# Get backend security group ID
BACKEND_SG_ID=$(aws ec2 describe-security-groups \
  --group-names ecommerce-backend-sg \
  --query 'SecurityGroups[0].GroupId' --output text)

# Allow MySQL from backend only
aws ec2 authorize-security-group-ingress \
  --group-name ecommerce-db-sg \
  --protocol tcp --port 3306 --source-group $BACKEND_SG_ID
```

---

## Step 2: RDS MySQL Setup

### 2.1 RDS 인스턴스 생성

```bash
aws rds create-db-instance \
  --db-instance-identifier ecommerce-db \
  --db-instance-class db.t3.micro \
  --engine mysql \
  --engine-version 8.0 \
  --master-username admin \
  --master-user-password <YOUR_PASSWORD> \
  --allocated-storage 20 \
  --storage-type gp2 \
  --vpc-security-group-ids <DB_SG_ID> \
  --publicly-accessible \
  --backup-retention-period 7 \
  --no-multi-az
```

### 2.2 Console에서 생성 (권장)

1. **RDS Console** → Create database
2. **Engine**: MySQL 8.0
3. **Template**: Free tier
4. **DB Instance Identifier**: `ecommerce-db`
5. **Master username**: `admin`
6. **Master password**: 안전한 비밀번호 설정
7. **Instance class**: `db.t3.micro` (Free Tier)
8. **Storage**: 20 GB gp2
9. **VPC Security Group**: `ecommerce-db-sg`
10. **Initial database name**: `ecommerce`

### 2.3 연결 정보 확인

```bash
aws rds describe-db-instances \
  --db-instance-identifier ecommerce-db \
  --query 'DBInstances[0].Endpoint'
```

---

## Step 3: EC2 Instance Setup

### 3.1 Key Pair 생성

```bash
aws ec2 create-key-pair \
  --key-name ecommerce-key \
  --query 'KeyMaterial' \
  --output text > ~/.ssh/ecommerce-key.pem

chmod 400 ~/.ssh/ecommerce-key.pem
```

### 3.2 EC2 인스턴스 생성

```bash
aws ec2 run-instances \
  --image-id ami-0c55b159cbfafe1f0 \
  --instance-type t2.micro \
  --key-name ecommerce-key \
  --security-group-ids <BACKEND_SG_ID> \
  --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=ecommerce-backend}]'
```

### 3.3 Console에서 생성 (권장)

1. **EC2 Console** → Launch Instance
2. **Name**: `ecommerce-backend`
3. **AMI**: Amazon Linux 2023 AMI
4. **Instance type**: `t2.micro` (Free Tier)
5. **Key pair**: `ecommerce-key`
6. **Security group**: `ecommerce-backend-sg`
7. **Storage**: 30 GB gp3

### 3.4 Elastic IP 할당 (선택)

```bash
# Elastic IP 생성
ALLOCATION_ID=$(aws ec2 allocate-address --query 'AllocationId' --output text)

# EC2에 연결
aws ec2 associate-address \
  --instance-id <INSTANCE_ID> \
  --allocation-id $ALLOCATION_ID
```

### 3.5 EC2 초기 설정

```bash
# SSH 접속
ssh -i ~/.ssh/ecommerce-key.pem ec2-user@<EC2_PUBLIC_IP>

# Docker 설치
sudo yum update -y
sudo yum install -y docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ec2-user

# 재접속 후 확인
docker --version
```

---

## Step 4: S3 + CloudFront (Frontend)

### 4.1 S3 버킷 생성

```bash
aws s3 mb s3://ecommerce-frontend-<UNIQUE_ID> --region ap-northeast-2

# 정적 웹 호스팅 비활성화 (CloudFront OAI 사용)
```

### 4.2 CloudFront Distribution 생성

1. **CloudFront Console** → Create Distribution
2. **Origin domain**: S3 버킷 선택
3. **Origin access**: Origin access control settings (recommended)
4. **Create new OAC**: 새 OAC 생성
5. **Viewer protocol policy**: Redirect HTTP to HTTPS
6. **Cache policy**: CachingOptimized
7. **Default root object**: `index.html`

### 4.3 S3 버킷 정책 업데이트

CloudFront에서 제공하는 정책을 S3 버킷에 적용합니다.

### 4.4 Error Pages 설정 (SPA용)

| HTTP Error Code | Response Page Path | HTTP Response Code |
|-----------------|-------------------|-------------------|
| 403 | /index.html | 200 |
| 404 | /index.html | 200 |

---

## Step 5: Route 53 (Optional)

도메인이 있는 경우:

### 5.1 Hosted Zone 생성

```bash
aws route53 create-hosted-zone --name example.com --caller-reference $(date +%s)
```

### 5.2 레코드 설정

| Name | Type | Value |
|------|------|-------|
| example.com | A | CloudFront Alias |
| api.example.com | A | EC2 Elastic IP |

---

## Step 6: SSL Certificate (ACM)

### 6.1 인증서 요청

```bash
aws acm request-certificate \
  --domain-name example.com \
  --subject-alternative-names "*.example.com" \
  --validation-method DNS \
  --region us-east-1  # CloudFront용은 us-east-1 필수
```

### 6.2 DNS 검증

Route 53에서 자동 검증 레코드를 생성합니다.

---

## Cost Estimation (Free Tier)

| Service | Free Tier | After Free Tier |
|---------|-----------|-----------------|
| EC2 t2.micro | 750 hours/month | ~$8-10/month |
| RDS db.t3.micro | 750 hours/month | ~$15-20/month |
| S3 | 5 GB | ~$0.02/GB |
| CloudFront | 1 TB/month | ~$0.085/GB |
| Route 53 | - | $0.50/zone |
| **Total** | **$0** | **~$25-35/month** |

---

## Environment Variables for GitHub Actions

### Backend Repository Secrets

| Secret Name | Description |
|-------------|-------------|
| `DOCKER_USERNAME` | Docker Hub 사용자명 |
| `DOCKER_PASSWORD` | Docker Hub 토큰 |
| `EC2_HOST` | EC2 Public IP 또는 도메인 |
| `EC2_USERNAME` | `ec2-user` |
| `EC2_SSH_KEY` | SSH 프라이빗 키 (PEM 내용) |
| `DB_HOST` | RDS Endpoint |
| `DB_NAME` | `ecommerce` |
| `DB_USERNAME` | RDS 사용자명 |
| `DB_PASSWORD` | RDS 비밀번호 |
| `JWT_SECRET` | JWT 시크릿 (256비트 이상) |

### Frontend Repository Secrets & Variables

| Name | Type | Description |
|------|------|-------------|
| `AWS_ACCESS_KEY_ID` | Secret | IAM Access Key |
| `AWS_SECRET_ACCESS_KEY` | Secret | IAM Secret Key |
| `AWS_REGION` | Variable | `ap-northeast-2` |
| `S3_BUCKET_NAME` | Variable | S3 버킷 이름 |
| `CLOUDFRONT_DISTRIBUTION_ID` | Variable | CloudFront ID |
| `VITE_API_URL` | Variable | `https://api.example.com` |

---

## Troubleshooting

### EC2에서 Docker 권한 오류

```bash
sudo usermod -aG docker $USER
# 재접속 필요
```

### RDS 연결 실패

1. Security Group에서 EC2 → RDS 허용 확인
2. RDS가 Publicly Accessible인지 확인
3. VPC 서브넷 라우팅 테이블 확인

### CloudFront 403 오류

1. S3 버킷 정책에 CloudFront OAI 허용 확인
2. S3 객체가 업로드되었는지 확인
3. Default Root Object 설정 확인
