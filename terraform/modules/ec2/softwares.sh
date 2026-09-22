#!/bin/bash

set -euxo pipefail

# =========================================================
# LOGGING
# =========================================================

exec > >(tee -a /var/log/user-data-install.log | logger -t user-data -s 2>/dev/console) 2>&1

echo "================================================="
echo "EC2 SOFTWARE INSTALLATION STARTED"
echo "Date: $(date)"
echo "================================================="

# =========================================================
# UPDATE OS
# =========================================================

echo "===== Updating apt ====="

export DEBIAN_FRONTEND=noninteractive

apt-get update -y

# Do NOT run full apt upgrade during cloud-init
# apt-get upgrade -y

# =========================================================
# BASIC PACKAGES
# =========================================================

echo "===== Installing basic packages ====="

apt-get install -y \
    tree \
    curl \
    wget \
    unzip \
    ca-certificates \
    gnupg \
    lsb-release \
    fontconfig \
    apt-transport-https

# =========================================================
# JAVA 25
# =========================================================

echo "===== Installing Java 25 ====="

apt-get install -y openjdk-25-jdk

echo "===== Java version ====="

java --version
javac --version

echo "===== Java location ====="

readlink -f "$(which java)"

# =========================================================
# JAVA 25 ENVIRONMENT
# =========================================================

JAVA_HOME="/usr/lib/jvm/java-25-openjdk-amd64"

if [ -d "$JAVA_HOME" ]; then

    echo "JAVA_HOME found: $JAVA_HOME"
    # Set system-wide JAVA_HOME
    cat > /etc/profile.d/java.sh <<EOF
export JAVA_HOME=$JAVA_HOME
export PATH=\$JAVA_HOME/bin:\$PATH
EOF

    chmod +x /etc/profile.d/java.sh

else

    echo "ERROR: Java 25 JAVA_HOME not found"
    exit 1
fi

# =========================================================
# JENKINS
# =========================================================

echo "===== Installing Jenkins ====="

mkdir -p /etc/apt/keyrings

wget -O /etc/apt/keyrings/jenkins-keyring.asc \
    https://pkg.jenkins.io/debian-stable/jenkins.io-2026.key

echo "deb [signed-by=/etc/apt/keyrings/jenkins-keyring.asc] https://pkg.jenkins.io/debian-stable binary/" \
    > /etc/apt/sources.list.d/jenkins.list

apt-get update -y

apt-get install -y jenkins

# =========================================================
# JENKINS JAVA 25 CONFIGURATION
# =========================================================

echo "===== Configuring Jenkins to use Java 25 ====="

mkdir -p /etc/systemd/system/jenkins.service.d

cat > /etc/systemd/system/jenkins.service.d/override.conf <<EOF
[Service]
Environment="JAVA_HOME=$JAVA_HOME"
Environment="PATH=$JAVA_HOME/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin"
EOF

# Reload systemd
systemctl daemon-reload

# Enable Jenkins
systemctl enable jenkins

# Restart Jenkins
systemctl restart jenkins

echo "===== Jenkins status ====="

systemctl --no-pager status jenkins || true

# =========================================================
# AWS CLI
# =========================================================

echo "===== Installing AWS CLI ====="

cd /tmp

curl -fsSL \
    "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" \
    -o awscliv2.zip

rm -rf /tmp/aws

unzip -q awscliv2.zip

/tmp/aws/install

echo "===== AWS CLI version ====="

aws --version

# =========================================================
# DOCKER
# =========================================================

echo "===== Installing Docker ====="

apt-get install -y docker.io

systemctl enable docker
systemctl start docker

echo "===== Docker version ====="

docker --version

# =========================================================
# ADD USERS TO DOCKER GROUP
# =========================================================

echo "===== Adding users to docker group ====="

usermod -aG docker jenkins
usermod -aG docker ubuntu

# Restart Docker

systemctl restart docker
sudo chmod 666 /var/run/docker.sock

sudo usermod -aG docker $USER
newgrp docker

# =========================================================
# VERIFY DOCKER
# =========================================================

echo "===== Docker status ====="

systemctl --no-pager status docker || true

echo "===== Docker socket ====="

ls -l /var/run/docker.sock


# =========================================================
# KUBECTL
# =========================================================

echo "===== Installing kubectl ====="

KUBECTL_VERSION=$(curl -L -s https://dl.k8s.io/release/stable.txt)

curl -LO "https://dl.k8s.io/release/${KUBECTL_VERSION}/bin/linux/amd64/kubectl"

chmod +x kubectl

mv kubectl /usr/local/bin/kubectl

echo "===== kubectl version ====="

kubectl version --client


# =========================================================
# HELM
# =========================================================

echo "===== Installing Helm ====="

curl -fsSL https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

echo "===== Helm version ====="

helm version


# =========================================================
# EKSCTL
# =========================================================

echo "===== Installing eksctl ====="

ARCH=amd64
PLATFORM="$(uname -s)_${ARCH}"

cd /tmp

curl -sLO "https://github.com/eksctl-io/eksctl/releases/latest/download/eksctl_${PLATFORM}.tar.gz"

tar -xzf "eksctl_${PLATFORM}.tar.gz"

mv eksctl /usr/local/bin/eksctl

echo "===== eksctl version ====="

eksctl version

# =========================================================
# FINAL VERIFICATION
# =========================================================

echo "================================================="
echo "INSTALLATION VERIFICATION"
echo "================================================="

echo "Tree:"
tree --version || true

echo "Java:"
java --version || true

echo "Javac:"
javac --version || true

echo "Jenkins:"
systemctl is-active jenkins || true

echo "AWS CLI:"
aws --version || true

echo "Docker:"
docker --version || true

echo "kubectl:"
kubectl version --client || true

echo "Helm:"
helm version || true

echo "eksctl:"
eksctl version || true

echo "================================================="
echo "EC2 SOFTWARE INSTALLATION COMPLETED"
echo "Date: $(date)"
echo "================================================="