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

systemctl daemon-reload

systemctl enable jenkins

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
# OFFICIAL DOCKER APT REPOSITORY
# =========================================================

echo "===== Installing latest Docker Engine ====="

# ---------------------------------------------------------
# Remove conflicting Ubuntu Docker packages
# ---------------------------------------------------------

apt-get remove -y \
    docker.io \
    docker-doc \
    docker-compose \
    docker-compose-v2 \
    docker-buildx \
    podman-docker \
    containerd \
    runc \
    || true


# ---------------------------------------------------------
# Add Docker official GPG key
# ---------------------------------------------------------

install -m 0755 -d /etc/apt/keyrings

curl -fsSL \
    https://download.docker.com/linux/ubuntu/gpg \
    -o /etc/apt/keyrings/docker.asc

chmod a+r /etc/apt/keyrings/docker.asc


# ---------------------------------------------------------
# Add Docker official repository
# ---------------------------------------------------------

cat > /etc/apt/sources.list.d/docker.sources <<EOF
Types: deb
URIs: https://download.docker.com/linux/ubuntu
Suites: $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}")
Components: stable
Architectures: $(dpkg --print-architecture)
Signed-By: /etc/apt/keyrings/docker.asc
EOF


# ---------------------------------------------------------
# Update apt
# ---------------------------------------------------------

apt-get update -y


# ---------------------------------------------------------
# Install latest Docker Engine + Compose
# ---------------------------------------------------------

apt-get install -y \
    docker-ce \
    docker-ce-cli \
    containerd.io \
    docker-buildx-plugin \
    docker-compose-plugin


# ---------------------------------------------------------
# Enable Docker
# ---------------------------------------------------------

systemctl enable docker
systemctl start docker


# =========================================================
# DOCKER VERSION
# =========================================================

echo "===== Docker version ====="

docker --version

echo "===== Docker Compose version ====="

docker compose version

echo "===== Docker Buildx version ====="

docker buildx version


# =========================================================
# ADD USERS TO DOCKER GROUP
# =========================================================

echo "===== Adding users to docker group ====="

# Jenkins user
usermod -aG docker jenkins

# Ubuntu user
usermod -aG docker ubuntu


# =========================================================
# DO NOT USE chmod 666 /var/run/docker.sock
# =========================================================

echo "===== Docker socket permissions ====="

ls -l /var/run/docker.sock


# =========================================================
# RESTART DOCKER
# =========================================================

systemctl restart docker


# =========================================================
# DOCKER STATUS
# =========================================================

echo "===== Docker status ====="

systemctl --no-pager status docker || true


# =========================================================
# KUBECTL
# =========================================================

echo "===== Installing kubectl ====="

KUBECTL_VERSION=$(curl -L -s https://dl.k8s.io/release/stable.txt)

curl -LO \
    "https://dl.k8s.io/release/${KUBECTL_VERSION}/bin/linux/amd64/kubectl"

chmod +x kubectl

mv kubectl /usr/local/bin/kubectl

echo "===== kubectl version ====="

kubectl version --client


# =========================================================
# HELM
# =========================================================

echo "===== Installing Helm ====="

curl -fsSL \
    https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

echo "===== Helm version ====="

helm version


# =========================================================
# EKSCTL
# =========================================================

echo "===== Installing eksctl ====="

ARCH=amd64
PLATFORM="$(uname -s)_${ARCH}"

cd /tmp

curl -sLO \
    "https://github.com/eksctl-io/eksctl/releases/latest/download/eksctl_${PLATFORM}.tar.gz"

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

echo "===== Tree ====="
tree --version || true

echo "===== Java ====="
java --version || true

echo "===== Javac ====="
javac --version || true

echo "===== JAVA_HOME ====="
echo "$JAVA_HOME"

echo "===== Jenkins ====="
systemctl is-active jenkins || true

echo "===== AWS CLI ====="
aws --version || true

echo "===== Docker ====="
docker --version || true

echo "===== Docker Compose ====="
docker compose version || true

echo "===== Docker Buildx ====="
docker buildx version || true

echo "===== Docker info ====="
docker info || true

echo "===== Docker socket ====="
ls -l /var/run/docker.sock

echo "===== kubectl ====="
kubectl version --client || true

echo "===== Helm ====="
helm version || true

echo "===== eksctl ====="
eksctl version || true

echo "================================================="
echo "EC2 SOFTWARE INSTALLATION COMPLETED"
echo "Date: $(date)"
echo "================================================="