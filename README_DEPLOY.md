# Billing API - Deployment Guide

## Docker

### Build Image
```bash
docker build -t billing-api .
```

### Run Locally (with MongoDB)
```bash
docker-compose up
```
Access the API at `http://localhost:8080`.

### Webhooks (Mercado Pago)
Mercado Pago sends payment notifications to a **public HTTPS URL**. When running locally, you must use a tunnel service (like Serveo, Localtunnel, or a configured ngrok tunnel) to expose your local port 8080 to the internet.

Set the `MERCADOPAGO_NOTIFICATION_URL` environment variable to your public tunnel URL (e.g. `https://your-tunnel.com/api/v1/webhooks/mercadopago`).

## Kubernetes (AWS EKS)

### Prerequisites
- AWS CLI configured
- `kubectl` installed
- EKS Cluster running (Managed via Terraform in `/terraform`)

### 1. Infrastructure Setup (Terraform)
Before deploying the application, you must provision the AWS infrastructure (VPC, EKS Cluster, and ECR Registry) using Terraform.

```bash
cd terraform
terraform init
terraform apply
```
*Note: The provisioning process typically takes 15-20 minutes.*

### 2. Secrets
**Important:** Do not store secrets in files. Create them manually in your cluster using the following command. This will allow the application to connect to MongoDB and receive Mercado Pago webhooks.

Replace `<YOUR_LOAD_BALANCER_DNS>` with the External IP of your service after it's deployed.

```bash
kubectl create secret generic billing-secrets \
  --from-literal=MONGODB_URI="mongodb://mongodb:27017/billing_db" \
  --from-literal=MERCADOPAGO_NOTIFICATION_URL="http://<YOUR_LOAD_BALANCER_DNS>/api/v1/webhooks/mercadopago" \
  --from-literal=MERCADO_PAGO_ACCESS_TOKEN="your-mercado-pago-token"
```

### 3. CI/CD with GitHub Actions
This project is configured with a GitHub Actions workflow for automatic deployment.

**Workflow:** `.github/workflows/deploy.yml`
- **Trigger:** Pushes to the `master` branch.
- **Actions:**
  1. Builds the Java application using Maven.
  2. Builds and pushes a Docker image to **AWS ECR**.
  3. Updates the Kubernetes deployment to use the new image.
  4. Deploys/Updates all manifests in the `k8s/` directory.

**Setup Requirements:**
Ensure the following **GitHub Secrets** are configured in your repository:
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`

> **Note:** The workflow assumes the AWS Region is `us-east-1` and the Cluster Name is `techchallenge-billing`, as defined in the Terraform files. If you change these in Terraform, update the `env` section in `.github/workflows/deploy.yml` accordingly.

### 4. Manual Deploy
If you need to apply manifests manually:

```bash
# Deploy MongoDB (Database)
kubectl apply -f k8s/mongo-deployment.yaml
kubectl apply -f k8s/mongo-service.yaml

# Deploy API
kubectl apply -f k8s/billing-deployment.yaml
kubectl apply -f k8s/billing-service.yaml

# Apply Autoscaling
kubectl apply -f k8s/billing-hpa.yaml
```

### 4. Verify
Get the external IP (LoadBalancer) to access the API:
```bash
kubectl get svc billing-api-service
```
