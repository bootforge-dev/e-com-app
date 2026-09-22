variable "vpc_id" {
  description = "VPC ID"
  type        = string
}

variable "public_subnet_ids" {
  description = "public subnet cidrs"
  type        = list(string)
}

variable "internet_gateway_id" {
  description = "Internet Gateway Id"
  type        = string
}

variable "project_name" {
  description = "Project Name"
  type        = string
}