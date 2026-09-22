resource "aws_instance" "e_com_ubuntu" {
  ami = "ami-0199ac7c9fbf9ed83"
  instance_type = "m7i-flex.large"
  region = "ap-south-2"
  key_name = "HYD-KP"

  root_block_device {
    volume_size = 20
    volume_type = "gp3"
  }

  tags = {
    Name = "e-com-ubuntu"
  }
}
