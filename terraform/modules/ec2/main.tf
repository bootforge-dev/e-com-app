resource "aws_instance" "this" {
  ami                         = var.ami_id
  instance_type               = var.instance_type
  subnet_id                   = var.subnet_id
  vpc_security_group_ids      = [var.security_group_id]
  associate_public_ip_address = true
  key_name                    = var.key_name
  user_data_base64 = base64gzip(
    file("${path.module}/softwares.sh")
  )

  root_block_device {
    volume_size = 25
    volume_type = "gp3"
  }
  tags = {
    Name = "${var.project_name}-ec2"
  }
}