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
- EKS Cluster running

### 1. Secrets
**Important:** Update `k8s/billing-secret.yaml` with your Base64 encoded secrets before applying.
```bash
echo -n "your-mercado-pago-token" | base64
```

### 2. Deploy
Apply the manifests in the following order:

```bash
# Deploy MongoDB (For testing purposes)
kubectl apply -f k8s/mongo-deployment.yaml
kubectl apply -f k8s/mongo-service.yaml

# Deploy API
kubectl apply -f k8s/billing-secret.yaml
kubectl apply -f k8s/billing-deployment.yaml
kubectl apply -f k8s/billing-service.yaml

# Apply Autoscaling
kubectl apply -f k8s/billing-hpa.yaml
```

### 3. Verify
Get the external IP (LoadBalancer) to access the API:
```bash
kubectl get svc billing-api-service
```
