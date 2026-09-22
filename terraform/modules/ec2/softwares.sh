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
# VARIABLES
# =========================================================

export DEBIAN_FRONTEND=noninteractive

MAVEN_VERSION="3.9.11"
MAVEN_HOME="/opt/maven"

REPO_URL="https://github.com/bootforge-dev/e-com-app.git"
APP_DIR="/opt/e-com-app"

export NEEDRESTART_MODE=a


# =========================================================
# CHECK OS
# =========================================================

echo "===== Operating System ====="

cat /etc/os-release

ARCH=$(dpkg --print-architecture)

echo "Architecture: $ARCH"

if [ "$ARCH" != "amd64" ]; then
    echo "ERROR: This script currently expects amd64 architecture."
    exit 1
fi


# =========================================================
# UPDATE APT
# =========================================================

echo "================================================="
echo "UPDATING APT"
echo "================================================="

apt-get update -y

apt-get upgrade -y


# =========================================================
# BASIC PACKAGES
# =========================================================

echo "================================================="
echo "INSTALLING BASIC PACKAGES"
echo "================================================="

apt-get install -y \
    ca-certificates \
    curl \
    wget \
    unzip \
    tree \
    gnupg \
    lsb-release \
    fontconfig \
    apt-transport-https \
    git \
    tar \
    gzip \
    jq \
    software-properties-common


# =========================================================
# VERIFY BASIC PACKAGES
# =========================================================

echo "===== Verifying basic packages ====="

git --version
curl --version
wget --version
unzip -v | head -n 2
tar --version | head -n 1
jq --version


# =========================================================
# JAVA 25
# =========================================================

echo "================================================="
echo "INSTALLING JAVA 25"
echo "================================================="

apt-get install -y openjdk-25-jdk


echo "===== Java version ====="

java --version
javac --version


# =========================================================
# JAVA_HOME
# =========================================================

echo "================================================="
echo "CONFIGURING JAVA_HOME"
echo "================================================="

JAVA_BIN=$(readlink -f "$(which java)")
JAVAC_BIN=$(readlink -f "$(which javac)")

JAVA_HOME=$(dirname "$(dirname "$JAVA_BIN")")

export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"

echo "Java binary:"
echo "$JAVA_BIN"

echo "Javac binary:"
echo "$JAVAC_BIN"

echo "JAVA_HOME:"
echo "$JAVA_HOME"


if [ ! -d "$JAVA_HOME" ]; then
    echo "ERROR: JAVA_HOME directory does not exist:"
    echo "$JAVA_HOME"
    exit 1
fi


cat > /etc/profile.d/java.sh <<EOF
export JAVA_HOME=$JAVA_HOME
export PATH=\$JAVA_HOME/bin:\$PATH
EOF

chmod 644 /etc/profile.d/java.sh


# =========================================================
# MAVEN
# =========================================================

echo "================================================="
echo "INSTALLING APACHE MAVEN"
echo "================================================="

apt-get update -y

apt-get install -y maven


# =========================================================
# MAVEN ENVIRONMENT
# =========================================================

MAVEN_HOME=$(dirname "$(dirname "$(readlink -f "$(which mvn)")")")

export MAVEN_HOME
export PATH="$MAVEN_HOME/bin:$PATH"

echo "MAVEN_HOME=$MAVEN_HOME"


cat > /etc/profile.d/maven.sh <<EOF
export MAVEN_HOME=$MAVEN_HOME
export PATH=\$MAVEN_HOME/bin:\$PATH
EOF

chmod 644 /etc/profile.d/maven.sh


# =========================================================
# VERIFY MAVEN
# =========================================================

echo "===== Maven version ====="

mvn -version

if ! command -v mvn >/dev/null 2>&1; then
    echo "ERROR: Maven installation failed."
    exit 1
fi


# =========================================================
# JENKINS
# =========================================================

echo "================================================="
echo "INSTALLING JENKINS"
echo "================================================="


# ---------------------------------------------------------
# Jenkins repository key
# ---------------------------------------------------------

install -m 0755 -d /etc/apt/keyrings

echo "Downloading Jenkins repository key..."

wget -O /etc/apt/keyrings/jenkins-keyring.asc \
    https://pkg.jenkins.io/debian-stable/jenkins.io-2026.key

chmod 644 /etc/apt/keyrings/jenkins-keyring.asc


# ---------------------------------------------------------
# Jenkins repository
# ---------------------------------------------------------

cat > /etc/apt/sources.list.d/jenkins.list <<EOF
deb [signed-by=/etc/apt/keyrings/jenkins-keyring.asc] https://pkg.jenkins.io/debian-stable binary/
EOF


# ---------------------------------------------------------
# Install Jenkins
# ---------------------------------------------------------

apt-get update -y

apt-get install -y jenkins


# =========================================================
# CONFIGURE JENKINS JAVA
# =========================================================

echo "================================================="
echo "CONFIGURING JENKINS JAVA"
echo "================================================="

mkdir -p /etc/systemd/system/jenkins.service.d


cat > /etc/systemd/system/jenkins.service.d/override.conf <<EOF
[Service]
Environment="JAVA_HOME=$JAVA_HOME"
Environment="PATH=$JAVA_HOME/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin"
EOF


systemctl daemon-reload

systemctl enable jenkins

systemctl restart jenkins


# =========================================================
# JENKINS STATUS
# =========================================================

echo "===== Jenkins status ====="

systemctl --no-pager status jenkins || true

echo "===== Jenkins active state ====="

systemctl is-active jenkins || true


# =========================================================
# AWS CLI V2
# =========================================================

echo "================================================="
echo "INSTALLING AWS CLI V2"
echo "================================================="

cd /tmp

rm -f awscliv2.zip

curl -fL \
    "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" \
    -o awscliv2.zip


rm -rf /tmp/aws

unzip -q awscliv2.zip


/tmp/aws/install --update


# =========================================================
# VERIFY AWS CLI
# =========================================================

echo "===== AWS CLI version ====="

aws --version


if ! command -v aws >/dev/null 2>&1; then
    echo "ERROR: AWS CLI installation failed."
    exit 1
fi


# =========================================================
# DOCKER
# =========================================================

echo "================================================="
echo "INSTALLING DOCKER"
echo "================================================="


# ---------------------------------------------------------
# Remove old/conflicting packages
# ---------------------------------------------------------

echo "===== Removing old Docker packages ====="

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
# Docker GPG key
# ---------------------------------------------------------

echo "===== Adding Docker GPG key ====="

install -m 0755 -d /etc/apt/keyrings

curl -fL \
    https://download.docker.com/linux/ubuntu/gpg \
    -o /etc/apt/keyrings/docker.asc

chmod a+r /etc/apt/keyrings/docker.asc


# ---------------------------------------------------------
# Detect Ubuntu codename
# ---------------------------------------------------------

. /etc/os-release

UBUNTU_CODENAME="${UBUNTU_CODENAME:-$VERSION_CODENAME}"

echo "Ubuntu codename: $UBUNTU_CODENAME"


# ---------------------------------------------------------
# Docker repository
# ---------------------------------------------------------

echo "===== Adding Docker repository ====="

cat > /etc/apt/sources.list.d/docker.sources <<EOF
Types: deb
URIs: https://download.docker.com/linux/ubuntu
Suites: $UBUNTU_CODENAME
Components: stable
Architectures: $(dpkg --print-architecture)
Signed-By: /etc/apt/keyrings/docker.asc
EOF


# ---------------------------------------------------------
# Update repository
# ---------------------------------------------------------

apt-get update -y


# ---------------------------------------------------------
# Install Docker
# ---------------------------------------------------------

echo "===== Installing Docker Engine ====="

apt-get install -y \
    docker-ce \
    docker-ce-cli \
    containerd.io \
    docker-buildx-plugin \
    docker-compose-plugin


# =========================================================
# ENABLE DOCKER
# =========================================================

echo "===== Enabling Docker ====="

systemctl enable docker

systemctl start docker

systemctl restart docker


# =========================================================
# DOCKER GROUP
# =========================================================

echo "================================================="
echo "CONFIGURING DOCKER GROUP"
echo "================================================="

if id ubuntu >/dev/null 2>&1; then
    usermod -aG docker ubuntu
fi

if id jenkins >/dev/null 2>&1; then
    usermod -aG docker jenkins
fi


# =========================================================
# VERIFY DOCKER
# =========================================================

echo "===== Docker version ====="

docker --version

echo "===== Docker Compose version ====="

docker compose version

echo "===== Docker Buildx version ====="

docker buildx version


if ! systemctl is-active --quiet docker; then
    echo "ERROR: Docker service is not running."
    exit 1
fi


# =========================================================
# DOCKER SOCKET
# =========================================================

echo "===== Docker socket ====="

ls -l /var/run/docker.sock


# =========================================================
# KUBECTL
# =========================================================

echo "================================================="
echo "INSTALLING KUBECTL"
echo "================================================="

cd /tmp

KUBECTL_VERSION=$(curl -fsSL https://dl.k8s.io/release/stable.txt)

echo "kubectl version: $KUBECTL_VERSION"


curl -fL \
    -o kubectl \
    "https://dl.k8s.io/release/${KUBECTL_VERSION}/bin/linux/amd64/kubectl"


install -m 0755 kubectl /usr/local/bin/kubectl

rm -f kubectl


# =========================================================
# VERIFY KUBECTL
# =========================================================

echo "===== kubectl version ====="

kubectl version --client


if ! command -v kubectl >/dev/null 2>&1; then
    echo "ERROR: kubectl installation failed."
    exit 1
fi


# =========================================================
# HELM
# =========================================================

echo "================================================="
echo "INSTALLING HELM"
echo "================================================="


curl -fsSL \
    https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash


# =========================================================
# VERIFY HELM
# =========================================================

echo "===== Helm version ====="

helm version


if ! command -v helm >/dev/null 2>&1; then
    echo "ERROR: Helm installation failed."
    exit 1
fi


# =========================================================
# EKSCTL
# =========================================================

echo "================================================="
echo "INSTALLING EKSCTL"
echo "================================================="

ARCH=amd64
PLATFORM="$(uname -s)_${ARCH}"

cd /tmp

EKSCTL_TARBALL="eksctl_${PLATFORM}.tar.gz"


rm -f "$EKSCTL_TARBALL"

echo "Downloading eksctl..."

curl -fL \
    -o "$EKSCTL_TARBALL" \
    "https://github.com/eksctl-io/eksctl/releases/latest/download/${EKSCTL_TARBALL}"


tar -xzf "$EKSCTL_TARBALL"


install -m 0755 eksctl /usr/local/bin/eksctl


rm -f eksctl
rm -f "$EKSCTL_TARBALL"


# =========================================================
# VERIFY EKSCTL
# =========================================================

echo "===== eksctl version ====="

eksctl version


if ! command -v eksctl >/dev/null 2>&1; then
    echo "ERROR: eksctl installation failed."
    exit 1
fi


# =========================================================
# GIT CONFIGURATION
# =========================================================

echo "================================================="
echo "CONFIGURING GIT"
echo "================================================="

git --version


# =========================================================
# CREATE APPLICATION DIRECTORY
# =========================================================

echo "================================================="
echo "CREATING APPLICATION DIRECTORY"
echo "================================================="

mkdir -p "$APP_DIR"

chown ubuntu:ubuntu "$APP_DIR"

chmod 755 "$APP_DIR"


# =========================================================
# CLONE E-COM APP
# =========================================================

echo "================================================="
echo "CLONING E-COM APP"
echo "================================================="

echo "Repository:"
echo "$REPO_URL"

echo "Destination:"
echo "$APP_DIR"


if [ -d "$APP_DIR/.git" ]; then

    echo "Repository already exists."

    cd "$APP_DIR"

    git pull

else

    rm -rf "$APP_DIR"

    git clone "$REPO_URL" "$APP_DIR"

fi


# =========================================================
# SET REPOSITORY OWNERSHIP
# =========================================================

chown -R ubuntu:ubuntu "$APP_DIR"


# =========================================================
# VERIFY GIT REPOSITORY
# =========================================================

echo "===== Git repository ====="

cd "$APP_DIR"

git remote -v

git status


# =========================================================
# FINAL VERIFICATION
# =========================================================

echo ""
echo "================================================="
echo "FINAL INSTALLATION VERIFICATION"
echo "================================================="


# ---------------------------------------------------------
# JAVA
# ---------------------------------------------------------

echo ""
echo "===== JAVA ====="

java --version

javac --version


# ---------------------------------------------------------
# JAVA HOME
# ---------------------------------------------------------

echo ""
echo "===== JAVA_HOME ====="

echo "$JAVA_HOME"


# ---------------------------------------------------------
# MAVEN
# ---------------------------------------------------------

echo ""
echo "===== MAVEN ====="

mvn -version


# ---------------------------------------------------------
# GIT
# ---------------------------------------------------------

echo ""
echo "===== GIT ====="

git --version


# ---------------------------------------------------------
# JENKINS
# ---------------------------------------------------------

echo ""
echo "===== JENKINS ====="

systemctl is-active jenkins || true

systemctl is-enabled jenkins || true


# ---------------------------------------------------------
# AWS CLI
# ---------------------------------------------------------

echo ""
echo "===== AWS CLI ====="

aws --version


# ---------------------------------------------------------
# DOCKER
# ---------------------------------------------------------

echo ""
echo "===== DOCKER ====="

docker --version


# ---------------------------------------------------------
# DOCKER COMPOSE
# ---------------------------------------------------------

echo ""
echo "===== DOCKER COMPOSE ====="

docker compose version


# ---------------------------------------------------------
# DOCKER BUILDX
# ---------------------------------------------------------

echo ""
echo "===== DOCKER BUILDX ====="

docker buildx version


# ---------------------------------------------------------
# DOCKER SERVICE
# ---------------------------------------------------------

echo ""
echo "===== DOCKER SERVICE ====="

systemctl is-active docker


# ---------------------------------------------------------
# DOCKER SOCKET
# ---------------------------------------------------------

echo ""
echo "===== DOCKER SOCKET ====="

ls -l /var/run/docker.sock


# ---------------------------------------------------------
# DOCKER GROUP
# ---------------------------------------------------------

echo ""
echo "===== DOCKER GROUP ====="

getent group docker


# ---------------------------------------------------------
# UBUNTU GROUPS
# ---------------------------------------------------------

echo ""
echo "===== UBUNTU GROUPS ====="

id ubuntu


# ---------------------------------------------------------
# JENKINS GROUPS
# ---------------------------------------------------------

echo ""
echo "===== JENKINS GROUPS ====="

id jenkins


# ---------------------------------------------------------
# KUBECTL
# ---------------------------------------------------------

echo ""
echo "===== KUBECTL ====="

kubectl version --client


# ---------------------------------------------------------
# HELM
# ---------------------------------------------------------

echo ""
echo "===== HELM ====="

helm version


# ---------------------------------------------------------
# EKSCTL
# ---------------------------------------------------------

echo ""
echo "===== EKSCTL ====="

eksctl version


# ---------------------------------------------------------
# TREE
# ---------------------------------------------------------

echo ""
echo "===== TREE ====="

tree --version


# ---------------------------------------------------------
# APPLICATION
# ---------------------------------------------------------

echo ""
echo "===== APPLICATION DIRECTORY ====="

ls -la "$APP_DIR"


# =========================================================
# CLEANUP
# =========================================================

echo ""
echo "================================================="
echo "CLEANING TEMPORARY FILES"
echo "================================================="

rm -f /tmp/awscliv2.zip
rm -f /tmp/apache-maven-${MAVEN_VERSION}-bin.tar.gz

rm -rf /tmp/aws


# =========================================================
# FINAL MESSAGE
# =========================================================

echo ""
echo "================================================="
echo "EC2 SOFTWARE INSTALLATION COMPLETED"
echo "================================================="

echo "Date: $(date)"

echo ""
echo "Installed software:"
echo "  Java 25"
echo "  Maven ${MAVEN_VERSION}"
echo "  Git"
echo "  Jenkins"
echo "  AWS CLI V2"
echo "  Docker"
echo "  Docker Compose"
echo "  Docker Buildx"
echo "  kubectl"
echo "  Helm"
echo "  eksctl"
echo "  jq"
echo "  tree"

echo ""
echo "Application repository:"
echo "  $REPO_URL"

echo ""
echo "Application directory:"
echo "  $APP_DIR"

echo ""
echo "IMPORTANT:"
echo "The ubuntu user was added to the docker group."
echo "The jenkins user was added to the docker group."
echo ""
echo "Reconnect to SSH before running Docker commands:"
echo ""
echo "    exit"
echo ""
echo "Then SSH back into the EC2 instance."
echo ""
echo "Verify:"
echo "    java --version"
echo "    mvn -version"
echo "    git --version"
echo "    aws --version"
echo "    docker --version"
echo "    docker compose version"
echo "    kubectl version --client"
echo "    helm version"
echo "    eksctl version"
echo "    docker ps"
echo ""
echo "Application:"
echo "    cd $APP_DIR"
echo "    git status"
echo ""
echo "================================================="