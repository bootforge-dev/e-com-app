module "vpc" {
  source              = "./modules/vpc"
  project_name        = var.project_name
  vpc_cidr            = var.vpc_cidr
  public_subnet_cidrs = var.public_subnet_cidrs
  availability_zones  = var.availability_zones
}

module "public_route_table" {
  source              = "./modules/public-route-table"
  vpc_id              = module.vpc.vpc_id
  project_name        = var.project_name
  internet_gateway_id = module.internet_gateway.internet_gateway_id
  public_subnet_ids   = module.vpc.public_subnet_ids
}

module "security_group" {
  source             = "./modules/security-group"
  project_name       = var.project_name
  vpc_id             = module.vpc.vpc_id
  custom_tcp_port    = var.custom_tcp_port
  allowed_cidr_block = ["0.0.0.0/0"]
}

module "internet_gateway" {
  source       = "./modules/internet-gateway"
  vpc_id       = module.vpc.vpc_id
  project_name = var.project_name
}

module "ec2" {
  source            = "./modules/ec2"
  project_name      = var.project_name
  ami_id            = var.ami_id
  instance_type     = var.instance_type
  subnet_id         = module.vpc.public_subnet_ids[0]
  security_group_id = module.security_group.security_group_id
  key_name          = var.key_name
}

module "ecr" {
  source               = "./modules/ecr"
  repository_name      = "e-comm-app"
  image_tag_mutability = "MUTABLE"
  scan_on_push         = true
  encryption_type      = "AES256"
}