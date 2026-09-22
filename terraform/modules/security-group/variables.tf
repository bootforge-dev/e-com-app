variable "vpc_id" {
  description = "VPC ID"
  type        = string
}

variable "project_name" {
  description = "project name"
  type        = string
}

variable "custom_tcp_port" {
  description = "custom tcp port"
  type        = number
  default     = 22
}

variable "allowed_cidr_block" {
  description = "CIDR blocks allowed for inbound traffic"
  type        = list(string)
  default = [
    "0.0.0.0/0"
  ]
}