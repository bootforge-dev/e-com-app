variable "vpc_cidr" {
  description = "The CIDR block for VPC"
  type        = string
}

variable "public_subnet_cidrs" {
  description = "CIDR block for the pulib subnet"
  type        = list(string)

  validation {
    condition     = length(var.public_subnet_cidrs) == 2
    error_message = "Exactly 2 public subnet cidrs provided"
  }
}

variable "availability_zones" {
  description = "The Availability zones"
  type        = list(string)

  validation {
    error_message = "Exactly 2 availability zones must be provided"
    condition     = length(var.availability_zones) == 2
  }
}

variable "project_name" {
  description = "Name of the project"
  type        = string
}