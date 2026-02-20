output "cluster_endpoint" {
  description = "The endpoint for your EKS Kubernetes API."
  value       = module.eks.cluster_endpoint
}

output "cluster_security_group_id" {
  description = "Security group ids attached to the cluster control plane."
  value       = module.eks.cluster_security_group_id
}

output "region" {
  description = "AWS region"
  value       = "us-east-1"
}

output "cluster_name" {
  description = "Kubernetes Cluster Name"
  value       = module.eks.cluster_name
}

output "ecr_repository_url" {
  description = "The URL of the ECR repository"
  value       = aws_ecr_repository.billing_api.repository_url
}

output "payment_notifications_queue_url" {
  value = aws_sqs_queue.payment_notifications.url
}

output "payment_approved_topic_arn" {
  value = aws_sns_topic.payment_approved.arn
}

output "payment_failed_topic_arn" {
  value = aws_sns_topic.payment_failed.arn
}

output "billing_irsa_role_arn" {
  value = module.billing_irsa_role.iam_role_arn
}
