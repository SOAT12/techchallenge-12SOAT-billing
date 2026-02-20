# IAM Role for Service Accounts (IRSA)
# Allows the Kubernetes Pods to assume this IAM role and access AWS SQS/SNS

module "billing_irsa_role" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.0"

  role_name = "billing-api-irsa"

  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["default:billing-sa"]
    }
  }

  tags = {
    Environment = "dev"
    Project     = "techchallenge-billing"
  }
}

# Policy allowing the Billing API to interact with specific SQS queues and SNS topics
resource "aws_iam_policy" "billing_messaging_policy" {
  name        = "BillingMessagingPolicy"
  description = "Allows Billing API to publish to SNS and consume from SQS"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "sqs:ReceiveMessage",
          "sqs:DeleteMessage",
          "sqs:GetQueueAttributes",
          "sqs:SendMessage"
        ],
        Resource = aws_sqs_queue.payment_notifications.arn
      },
      {
        Effect = "Allow",
        Action = [
          "sns:Publish"
        ],
        Resource = [
          aws_sns_topic.payment_approved.arn,
          aws_sns_topic.payment_failed.arn
        ]
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "billing_irsa_messaging" {
  role       = module.billing_irsa_role.iam_role_name
  policy_arn = aws_iam_policy.billing_messaging_policy.arn
}
