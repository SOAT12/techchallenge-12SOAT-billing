# --- SQS Queues ---

resource "aws_sqs_queue" "payment_notifications" {
  name                      = "payment-notifications-queue"
  delay_seconds             = 0
  max_message_size          = 262144
  message_retention_seconds = 86400
  receive_wait_time_seconds = 20 # Enable long polling

  tags = {
    Environment = "dev"
    Project     = "techchallenge-billing"
  }
}

# --- SNS Topics ---

resource "aws_sns_topic" "payment_approved" {
  name = "payment-approved-topic"

  tags = {
    Environment = "dev"
    Project     = "techchallenge-billing"
  }
}

resource "aws_sns_topic" "payment_failed" {
  name = "payment-failed-topic"

  tags = {
    Environment = "dev"
    Project     = "techchallenge-billing"
  }
}
