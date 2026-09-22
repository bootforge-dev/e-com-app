output "vpc_id" {
  value = module.vpc.vpc_id
}

output "public_subnet_ids" {
  value = module.vpc.public_subnet_ids
}

output "internet_gateway_id" {
  value = module.internet_gateway.internet_gateway_id
}

output "public_route_table_id" {
  value = module.public_route_table.route_table_id
}

output "security_group_id" {
  value = module.security_group.security_group_id
}

output "security_group_name" {
  value = module.security_group.security_group_name
}

output "ec2_instance_id" {
  value = module.ec2.instance_id
}

output "ec2_public_ip" {
  value = module.ec2.instance_public_ip
}

output "ec2_private_ip" {
  value = module.ec2.instance_private_ip
}

output "ecr_repository_name" {
  value = module.ecr.repository_name
}

output "ecr_repository_url" {
  value = module.ecr.repository_url
}

output "ecr_repository_arn" {
  value = module.ecr.repository_arn
}

# output "eks_cluster_name" {
#   value = module.eks_cluster.cluster_name
# }
#
# output "eks_cluster_endpoint" {
#   value = module.eks_cluster.cluster_endpoint
# }
#
# output "eks_cluster_arn" {
#   value = module.eks_cluster.cluster_arn
# }
#
# output "eks_node_group_name" {
#   value = module.eks_node_group.node_group_name
# }