resource "aws_security_group" "this" {
  name        = "${var.project_name}-sg"
  description = "Security group for ${var.project_name}"
  vpc_id      = var.vpc_id

  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = var.allowed_cidr_block
  }

  ingress {
    description = "Jenkins"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = var.allowed_cidr_block
  }

  ingress {
    description = "Sonar"
    from_port   = 9000
    to_port     = 9000
    protocol    = "tcp"
    cidr_blocks = var.allowed_cidr_block
  }

  ingress {
    description = "Product-Service"
    from_port   = 9001
    to_port     = 9001
    protocol    = "tcp"
    cidr_blocks = var.allowed_cidr_block
  }

  ingress {
    description = "Inventory-Service"
    from_port   = 9002
    to_port     = 9002
    protocol    = "tcp"
    cidr_blocks = var.allowed_cidr_block
  }

  ingress {
    description = "Order-Service"
    from_port   = 9003
    to_port     = 9003
    protocol    = "tcp"
    cidr_blocks = var.allowed_cidr_block
  }

  ingress {
    description = "Api-Gateway"
    from_port   = 9000
    to_port     = 9000
    protocol    = "tcp"
    cidr_blocks = var.allowed_cidr_block
  }

  ingress {
    description = "MySQL"
    from_port   = 3306
    to_port     = 3306
    protocol    = "tcp"
    cidr_blocks = var.allowed_cidr_block
  }

  ingress {
    description = "discovery-server"
    from_port   = 8761
    to_port     = 8761
    protocol    = "tcp"
    cidr_blocks = var.allowed_cidr_block
  }

  ingress {
    description = "HTTPS"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = var.allowed_cidr_block
  }

  ingress {
    description = "Custom TCP Port"
    from_port   = var.custom_tcp_port
    to_port     = var.custom_tcp_port
    protocol    = "tcp"
    cidr_blocks = var.allowed_cidr_block
  }

  egress {
    description = "Allowed all outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-sg"
  }

}