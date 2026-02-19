resource "aws_ecr_repository" "billing_api" {
  name                 = "billing-api"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Project     = "techchallenge-billing"
    Environment = "dev"
  }
}
