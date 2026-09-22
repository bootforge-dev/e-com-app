output "vpc_id" {
  value = aws_vpc.public.id
}

output "vpc_cidr" {
  value = aws_vpc.public.cidr_block
}

output "public_subnet_ids" {
  value = aws_subnet.public[*].id
}