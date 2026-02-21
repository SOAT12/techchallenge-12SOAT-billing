# Creates an HTTP API Gateway to act as an HTTPS proxy for the Kubernetes Load Balancer
resource "aws_apigatewayv2_api" "webhook_proxy" {
  name          = "billing-webhook-proxy"
  protocol_type = "HTTP"
  description   = "Proxy for Mercado Pago Webhooks to Kubernetes ELB"
}

# Configures the integration to forward requests to the Classic Load Balancer
resource "aws_apigatewayv2_integration" "elb_integration" {
  api_id             = aws_apigatewayv2_api.webhook_proxy.id
  integration_type   = "HTTP_PROXY"
  # We use the {proxy} path variable to forward the exact path requested
  integration_uri    = "http://ad81070b45c5849cbb28a1991611f389-523620946.us-east-1.elb.amazonaws.com/{proxy}"
  integration_method = "ANY"
  connection_type    = "INTERNET"
}

# Creates a catch-all route that sends all traffic to the integration
resource "aws_apigatewayv2_route" "default_route" {
  api_id    = aws_apigatewayv2_api.webhook_proxy.id
  route_key = "ANY /{proxy+}"
  target    = "integrations/${aws_apigatewayv2_integration.elb_integration.id}"
}

# Creates a default stage that automatically deploys changes
resource "aws_apigatewayv2_stage" "default_stage" {
  api_id      = aws_apigatewayv2_api.webhook_proxy.id
  name        = "$default"
  auto_deploy = true
}

# Outputs the final Webhook URL to use in Mercado Pago
output "api_gateway_webhook_url" {
  description = "The HTTPS URL to provide to Mercado Pago"
  value       = "${aws_apigatewayv2_api.webhook_proxy.api_endpoint}/api/v1/webhooks/mercadopago"
}
