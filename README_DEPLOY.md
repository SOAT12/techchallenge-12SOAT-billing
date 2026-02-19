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

### Webhooks (Mercado Pago) when running in containers

Mercado Pago sends payment notifications to a **public URL**. When running locally with Docker, your app is only reachable at `localhost`, so the webhook callback must go through a tunnel.

**Option A – ngrok in the stack (recommended for local + containers)**

1. Create a [free ngrok account](https://ngrok.com/) and copy your [authtoken](https://dashboard.ngrok.com/get-started/your-authtoken).
2. Create a `.env` file in the project root (same folder as `docker-compose.yml`):
   ```env
   NGROK_AUTHTOKEN=your_ngrok_authtoken_here
   MERCADO_PAGO_ACCESS_TOKEN=your_mercado_pago_token
   ```
3. Start the stack (including ngrok):
   ```bash
   docker-compose up -d
   ```
4. Get the public URL: open **http://localhost:4040** (ngrok web inspector). Copy the HTTPS URL (e.g. `https://abc123.ngrok-free.app`).
5. Set the webhook URL and restart the billing service so it sends this URL to Mercado Pago when creating preferences:
   ```bash
   set MERCADOPAGO_NOTIFICATION_URL=https://YOUR_NGROK_URL/api/v1/webhooks/mercadopago
   docker-compose up -d --force-recreate billing-service
   ```
   On Linux/Mac use `export` instead of `set`, or put `MERCADOPAGO_NOTIFICATION_URL=https://YOUR_NGROK_URL/api/v1/webhooks/mercadopago` in your `.env` and run:
   ```bash
   docker-compose up -d --force-recreate billing-service
   ```

**Option B – ngrok on the host**

If you prefer to run ngrok on your machine (not in Docker):

1. Start your stack: `docker-compose up -d` (you can omit the `ngrok` service or leave it stopped).
2. Run ngrok pointing at the exposed port: `ngrok http 8080`.
3. Set `MERCADOPAGO_NOTIFICATION_URL` to `https://YOUR_NGROK_URL/api/v1/webhooks/mercadopago` (in `.env` or env) and restart the billing service as in step 5 above.

Without one of these, Mercado Pago has no public URL to call and the webhook will not work.

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
