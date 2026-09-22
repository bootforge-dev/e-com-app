variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-south-2"
}

variable "project_name" {
  description = "Name of the project"
  type        = string
  default     = "e-com-app-env"
}

variable "vpc_cidr" {
  description = "VPC CIDR blokc"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidrs" {
  description = "public subnet cidrs"
  type        = list(string)
  default = [
    "10.0.1.0/24",
    "10.0.2.0/24"
  ]
}

variable "availability_zones" {
  description = "availability zones"
  type        = list(string)
  default = [
    "ap-south-2a",
    "ap-south-2b"
  ]
}

variable "custom_tcp_port" {
  description = "custom tcp port"
  type        = number
  default     = 22
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "m7i-flex.large"
}

variable "key_name" {
  description = "SSH key pair name for the EC2 instance"
  type        = string
  default     = "HYD-KP"
}

variable "ami_id" {
  description = "AMI ID"
  type        = string
  default     = "ami-0199ac7c9fbf9ed83"
}

variable "eks_cluster_name" {
  description = "EKS Cluster name"
  type        = string
  default     = "ecom-eks-cluster"
}

variable "eks_cluster_version" {
  description = "EKS Cluster version"
  type        = string
  default     = "1.36"
}

variable "eks_node_group_name" {
  description = "EKS Node Group name"
  type        = string
  default     = "dev-node-group"
}

variable "eks_instance_types" {
  description = "EKS instance types"
  type        = list(string)
  default     = ["t3.small"]
}

variable "eks_desired_size" {
  description = "EKS desired size"
  type        = number
  default     = 2
}

variable "eks_min_size" {
  description = "EKS min size"
  type        = number
  default     = 1
}

variable "eks_max_size" {
  description = "EKS max size"
  type        = number
  default     = 3
}

variable "eks_disk_size" {
  description = "EKS disk size"
  type        = number
  default     = 20
}